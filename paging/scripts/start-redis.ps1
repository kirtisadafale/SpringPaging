param(
    [string]$Name = 'redis-local',
    [int]$Port = 6379,
    [string]$Image = 'redis:7.2.2'
)

Write-Host "Starting Redis container '$Name' (image=$Image) on port $Port..."

# If a container with that name exists and is running, do nothing
$existing = docker ps --format '{{.Names}}' | Where-Object { $_ -eq $Name }
if ($existing) {
    Write-Host "Container '$Name' is already running."
    exit 0
}

# If a container exists but is stopped, remove it first
$stopped = docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $Name }
if ($stopped) {
    Write-Host "Removing existing stopped container '$Name'..."
    docker rm $Name | Out-Null
}

# Run the container
Write-Host "Running: docker run --rm -d --name $Name -p $Port:6379 $Image"
$run = docker run --rm -d --name $Name -p "$Port`:6379" $Image
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to start Redis container. Please ensure Docker is running and you have permission to run containers."
    exit $LASTEXITCODE
}

Write-Host "Redis started as container id: $run"
Write-Host "You can verify with: docker logs -f $Name"