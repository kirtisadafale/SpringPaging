#!/usr/bin/env bash
# Build Docker image and start services with docker-compose (bash)
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Building and starting services (docker-compose)..."
docker compose build --pull --no-cache
docker compose up -d

echo "Services started. App should be available at http://localhost:8080"
