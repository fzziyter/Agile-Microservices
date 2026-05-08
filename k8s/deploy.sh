#!/bin/bash
# =============================================================
#  deploy.sh  –  Build & deploy tous les microservices sur Minikube
#  Usage : chmod +x deploy.sh && ./deploy.sh
# =============================================================

# ---- Couleurs ----
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log()  { echo -e "${GREEN}[✓] $1${NC}"; }
warn() { echo -e "${YELLOW}[!] $1${NC}"; }
err()  { echo -e "${RED}[✗] $1${NC}"; exit 1; }

PROJECTS_ROOT="${1:-.}"
K8S_DIR="$(dirname "$0")"

# ---- 1. Vérifications préalables ----
command -v minikube &>/dev/null || err "minikube non trouvé"
command -v kubectl  &>/dev/null || err "kubectl non trouvé"
command -v mvn      &>/dev/null || err "Maven (mvn) non trouvé"

# ---- 2. Démarrer Minikube si nécessaire ----
if ! minikube status | grep -q "Running"; then
  warn "Minikube n'est pas démarré, démarrage..."
  minikube start --driver=docker --memory=6144 --cpus=4
fi
log "Minikube est Running"

# ---- 3. Pointer Docker vers le daemon Minikube ----
warn "Configuration du daemon Docker Minikube..."
eval $(minikube docker-env)
log "Docker pointe vers Minikube"

# ---- 4. Build Maven + Docker pour chaque service ----
SERVICES=(
  "discovery-server:discovery-server"
  "api-gateway:api-gateway"
  "ms-auth-user:ms-auth-user"
  "ms-projects:ms-projects"
  "ms-backlog:ms-backlog"
  "ms-sprints:ms-sprints"
  "ms-notifications:ms-notifications"
)

for entry in "${SERVICES[@]}"; do
  dir="${entry%%:*}"
  image="${entry##*:}"
  svc_path="$PROJECTS_ROOT/$dir"

  if [ ! -d "$svc_path" ]; then
    warn "Dossier $svc_path introuvable, service $dir ignoré"
    continue
  fi

  log "Build Maven : $dir"
  (cd "$svc_path" && mvn clean package -DskipTests -q) || { warn "Maven échoué pour $dir, on continue"; continue; }

  log "Build image Docker : $image:latest"
  docker build -t "$image:latest" "$svc_path" || warn "Docker build échoué pour $image"
done

# ---- 5. Appliquer les manifests Kubernetes ----
log "Déploiement MySQL..."
kubectl apply -f "$K8S_DIR/mysql/mysql.yaml"

log "Attente MySQL ready (2 min max)..."
kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s 2>/dev/null \
  || kubectl rollout status statefulset/mysql --timeout=120s 2>/dev/null \
  || warn "MySQL pas encore ready, on continue quand même"

log "Déploiement Discovery Server (Eureka)..."
kubectl apply -f "$K8S_DIR/discovery/discovery-server.yaml"

log "Attente Eureka ready (3 min max)..."
kubectl rollout status deployment/discovery-server --timeout=180s \
  || warn "Eureka pas encore ready, on continue quand même"

log "Déploiement API Gateway..."
kubectl apply -f "$K8S_DIR/gateway/api-gateway.yaml"

log "Déploiement microservices..."
kubectl apply -f "$K8S_DIR/ms-auth/ms-auth-user.yaml"
kubectl apply -f "$K8S_DIR/ms-projects/ms-projects.yaml"
kubectl apply -f "$K8S_DIR/ms-backlog/ms-backlog.yaml"
kubectl apply -f "$K8S_DIR/ms-sprints/ms-sprints.yaml"
kubectl apply -f "$K8S_DIR/ms-notifications/ms-notifications.yaml"

# ---- 6. Forcer le redémarrage des pods existants ----
log "Redémarrage des déploiements pour appliquer les changements..."
for deploy in discovery-server api-gateway ms-auth-user ms-projects ms-backlog ms-sprints ms-notifications; do
  kubectl rollout restart deployment/$deploy 2>/dev/null && log "Redémarré : $deploy" || warn "$deploy pas encore déployé"
done

# ---- 7. Résumé final ----
echo ""
log "Attente 30s que les pods démarrent..."
sleep 30

echo ""
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}  Statut des pods :${NC}"
echo -e "${GREEN}======================================${NC}"
kubectl get pods -o wide

echo ""
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}  Services exposés :${NC}"
echo -e "${GREEN}======================================${NC}"
kubectl get services

echo ""
echo "URL de l'API Gateway :"
echo "  Lance dans un autre terminal : kubectl port-forward service/api-gateway 8080:8080"
echo "  Puis accède à : http://localhost:8080"
echo ""
echo "  Ou utilise le script dédié : ./k8s/start-portforwards.sh"

echo ""
echo "Dashboard Kubernetes : minikube dashboard"
