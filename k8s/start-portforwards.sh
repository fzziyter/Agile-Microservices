#!/bin/bash
# =============================================================
#  start-portforwards.sh  –  Lance tous les port-forwards
#  Laisser ce terminal ouvert pendant le développement
# =============================================================

GREEN='\033[0;32m'
NC='\033[0m'
log() { echo -e "${GREEN}[✓] $1${NC}"; }

log "Démarrage des port-forwards..."
log "Ctrl+C pour tout arrêter"
echo ""

# Tuer les éventuels port-forwards existants
pkill -f "kubectl port-forward" 2>/dev/null || true
sleep 1

# Lancer tous les port-forwards en arrière-plan
kubectl port-forward service/api-gateway      8080:8080 &
kubectl port-forward service/discovery-server 8761:8761 &

log "API Gateway  → http://localhost:8080"
log "Eureka       → http://localhost:8761"
echo ""
log "Front-end    → http://localhost:3000  (lancer npm run dev séparément)"
echo ""
echo "En attente... (Ctrl+C pour arrêter)"

# Attendre que tous les jobs se terminent (reste ouvert)
wait
