$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build.ps1"
. "$PSScriptRoot\common.ps1"
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskToolchain = Get-ProjectJava
$taskMainClasses = Join-Path $taskRoot 'build\classes'
$taskTestClasses = Join-Path $taskRoot 'build\test-classes'
$taskOutput = Join-Path $taskRoot 'output'
New-Item -ItemType Directory -Path $taskTestClasses, $taskOutput -Force | Out-Null
$taskSources = @(Get-ChildItem -LiteralPath (Join-Path $taskRoot 'src\test\java') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& $taskToolchain.Javac -encoding UTF-8 --release 17 -cp $taskMainClasses -d $taskTestClasses @taskSources
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao das verificacoes.' }
& $taskToolchain.Java '-Djava.awt.headless=true' -cp "$taskMainClasses;$taskTestClasses" br.edu.steering.Verification $taskOutput
if ($LASTEXITCODE -ne 0) { throw 'Uma ou mais verificacoes falharam.' }
