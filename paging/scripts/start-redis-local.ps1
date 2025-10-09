<#
Starts a locally-installed Redis server WITHOUT Docker.
Strategy (in order):
 1) If WSL is available, attempt to run redis-server inside WSL (daemonized).
 2) Else, try to start a Windows service named like 'Redis' if present.

Usage examples:
  # default port, automatic detection
  .\scripts\start-redis-local.ps1

  # explicit port
  .\scripts\start-redis-local.ps1 -Port 6379

Notes:
 - WSL path: this assumes you have installed Redis inside WSL (e.g., Ubuntu: `sudo apt update; sudo apt install redis-server`).
 - Windows service: this assumes you installed a native Redis Windows service (via chocolatey or manual installer).
 - You may need to run the script as Administrator to start Windows services.
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
    Write-Info "WSL detected. Trying to start redis inside WSL..."
    $distroArg = ''
    if ($WslDistro -ne '') { $distroArg = "-d $WslDistro" }

    # Check if redis-server exists inside WSL
    $which = wsl $distroArg -- bash -lc "command -v redis-server || true"
    if ($which -and $which.Trim() -ne '') {
        Write-Info "Found redis-server in WSL at: $which"
        # Try to run as daemon
        try {
            wsl $distroArg -- bash -lc "nohup redis-server --port $Port --daemonize yes >/dev/null 2>&1 &"
            Write-Info "redis-server started in WSL (port $Port). Use 'wsl $distroArg -- redis-cli -p $Port ping' to verify (should reply PONG)."
            return
        } catch {
            Write-Err "Failed to start redis-server inside WSL: $_"
        }
    } else {
        Write-Info "No redis-server binary found inside WSL. You can install it in your WSL distro: 'sudo apt update; sudo apt install redis-server' (Ubuntu)."
    }
}

# 2) Try Windows service named like Redis
Write-Info "Attempting to detect a Windows Redis service..."
$svc = Get-Service -Name "redis*" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($svc) {
    Write-Info "Found service '$($svc.Name)' (status: $($svc.Status)). Attempting to start it..."
    try {
        if ($svc.Status -ne 'Running') {
            Start-Service -Name $svc.Name -ErrorAction Stop
            Write-Info "Service '$($svc.Name)' started."
        } else {
            Write-Info "Service '$($svc.Name)' is already running."
        }
        return
    } catch {
        Write-Err "Failed to start Windows service '$($svc.Name)': $_"
        Write-Info "You may need to run this script as Administrator."
        return
    }
}

Write-Err "No suitable local Redis installation found (WSL redis-server or Windows Redis service)."
Write-Host "Options:"
Write-Host "  - Install Redis in WSL (recommended on Windows):"
Write-Host "      Open WSL (Ubuntu) and run: sudo apt update; sudo apt install redis-server"
Write-Host "      Then re-run this script (or start via: wsl -- redis-server --daemonize yes)
"
Write-Host "  - Install Redis natively on Windows via Chocolatey (not officially supported upstream):"
Write-Host "      choco install redis-64 -y   # requires Chocolatey and admin rights"
Write-Host "  - Or use the Docker scripts (scripts/start-redis.ps1) which start Redis in Docker."
