param(
    [string]$Name = 'redis-local'
)

Write-Host "Stopping Redis container '$Name'..."

$running = docker ps --format '{{.Names}}' | Where-Object { $_ -eq $Name }
if (-not $running) {
    Write-Host "No running container named '$Name' found."
    exit 0
}

docker stop $Name | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to stop container '$Name'."
    exit $LASTEXITCODE
}

Write-Host "Container '$Name' stopped and removed (started with --rm)."