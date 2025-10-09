# Build the Docker image and start services via docker-compose (PowerShell)
Set-Location -Path $PSScriptRoot\..\

Write-Host "Building and starting services (docker-compose)..."
docker compose build --pull --no-cache
docker compose up -d

Write-Host "Services started. App should be available at http://localhost:8080"
