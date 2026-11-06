$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build.ps1"
. "$PSScriptRoot\common.ps1"
$taskToolchain = Get-ProjectJava
$taskJarPath = Join-Path (Split-Path $PSScriptRoot -Parent) 'dist\ParqueAutonomo.jar'
& $taskToolchain.Java -jar $taskJarPath
if ($LASTEXITCODE -ne 0) { throw 'A aplicacao terminou com erro.' }
