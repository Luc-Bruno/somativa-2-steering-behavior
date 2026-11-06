$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\common.ps1"
$taskProjectRoot = Split-Path $PSScriptRoot -Parent
$taskToolchain = Get-ProjectJava
$taskClasses = Join-Path $taskProjectRoot 'build\classes'
$taskDist = Join-Path $taskProjectRoot 'dist'
$taskTemp = Join-Path $taskProjectRoot 'build\tmp'
if (Test-Path -LiteralPath $taskClasses) {
    $taskResolvedClasses = [IO.Path]::GetFullPath($taskClasses)
    if ([IO.Path]::GetDirectoryName($taskResolvedClasses) -ne (Join-Path $taskProjectRoot 'build')) { throw 'Diretorio de build invalido.' }
    Remove-Item -LiteralPath $taskResolvedClasses -Recurse -Force
}
New-Item -ItemType Directory -Path $taskClasses, $taskDist, $taskTemp -Force | Out-Null
$taskSources = @(Get-ChildItem -LiteralPath (Join-Path $taskProjectRoot 'src\main\java') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& $taskToolchain.Javac -encoding UTF-8 --release 17 -d $taskClasses @taskSources
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao Java.' }
$taskJarPath = Join-Path $taskDist 'ParqueAutonomo.jar'
& $taskToolchain.Jar "-J-Djava.io.tmpdir=$taskTemp" --create --file $taskJarPath --main-class br.edu.steering.App -C $taskClasses .
if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar o JAR.' }
Write-Host "Build concluido: $taskJarPath ($($taskToolchain.Version), alvo Java 17)"
