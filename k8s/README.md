# Terminal 1 — déployer (se termine tout seul)
chmod +x k8s/deploy.sh
./k8s/deploy.sh

# Terminal 2 — garder ouvert pendant le dev
chmod +x k8s/start-portforwards.sh
./k8s/start-portforwards.sh

# Terminal 3 — front
npm run dev