<#
Stop a locally-installed Redis server WITHOUT Docker.
Strategy (in order):
 1) If WSL is available and redis-server is running inside WSL, attempt to stop it via redis-cli or pkill.
 2) Else, try to stop a Windows service named like 'Redis'.

Usage:
  .\scripts\stop-redis-local.ps1

Notes:
 - You may need to run the script as Administrator to stop Windows services.
#>

param(
    [int]$Port = 6379,
    [string]$WslDistro = ''
)

function Write-Info([string]$m){ Write-Host "[INFO] $m" }
function Write-Err([string]$m){ Write-Host "[ERROR] $m" -ForegroundColor Red }

# 1) Try WSL
$wslAvailable = (Get-Command wsl -ErrorAction SilentlyContinue) -ne $null
if ($wslAvailable) {
    Write-Info "WSL detected. Trying to stop redis inside WSL..."
    $distroArg = ''
    if ($WslDistro -ne '') { $distroArg = "-d $WslDistro" }

    # Check if redis-cli exists inside WSL
    $which = wsl $distroArg -- bash -lc "command -v redis-cli || true"
    if ($which -and $which.Trim() -ne '') {
        Write-Info "Found redis-cli in WSL at: $which. Attempting to send SHUTDOWN..."
        try {
            wsl $distroArg -- bash -lc "redis-cli -p $Port shutdown || true"
            Write-Info "Sent shutdown to redis-cli (may take a moment)."
            return
        } catch {
            Write-Err "Failed to shutdown via redis-cli: $_"
        }
    }

    # fallback: pkill redis-server
    try {
        wsl $distroArg -- bash -lc "pkill redis-server || true"
        Write-Info "Attempted to pkill redis-server in WSL."
        return
    } catch {
        Write-Err "Failed to pkill redis-server inside WSL: $_"
    }
}

# 2) Try Windows service
Write-Info "Attempting to detect a Windows Redis service..."
$svc = Get-Service -Name "redis*" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($svc) {
    Write-Info "Found service '$($svc.Name)' (status: $($svc.Status)). Attempting to stop it..."
    try {
        if ($svc.Status -ne 'Stopped') {
            Stop-Service -Name $svc.Name -ErrorAction Stop
            Write-Info "Service '$($svc.Name)' stopped."
        } else {
            Write-Info "Service '$($svc.Name)' is already stopped."
        }
        return
    } catch {
        Write-Err "Failed to stop Windows service '$($svc.Name)': $_"
        Write-Info "You may need to run this script as Administrator."
        return
    }
}

Write-Err "No suitable local Redis installation found to stop. If you started Redis via Docker, use scripts/stop-redis.ps1 instead."
