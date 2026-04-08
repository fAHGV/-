$ErrorActionPreference = 'Stop'

$root = Resolve-Path $PSScriptRoot
$buildScript = Join-Path $root 'scripts/build-launcher.ps1'
$exe = Join-Path $root 'dist/InfernoVisualsLauncher/InfernoVisualsLauncher.exe'
$jar = Join-Path $root 'build/infernovisuals-launcher.jar'

if (-not (Test-Path $exe) -and -not (Test-Path $jar)) {
    Write-Host 'Launcher not built yet. Building first...'
    & powershell -ExecutionPolicy Bypass -File $buildScript
}

if (Test-Path $exe) {
    Write-Host 'Starting InfernoVisuals Launcher...'
    Start-Process -FilePath $exe
    exit 0
}

if (Test-Path $jar) {
    Write-Host 'Starting InfernoVisuals Launcher (jar mode)...'
    Start-Process -FilePath 'javaw' -ArgumentList @('-jar', $jar)
    exit 0
}

Write-Error 'Failed to start launcher: executable not found after build.'
