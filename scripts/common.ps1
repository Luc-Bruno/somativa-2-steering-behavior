$ErrorActionPreference = 'Stop'
function Get-ProjectJava {
    $taskCandidates = @()
    if ($env:JAVA_HOME) { $taskCandidates += $env:JAVA_HOME }
    $taskLocalJdks = Join-Path $env:USERPROFILE '.jdks'
    if (Test-Path -LiteralPath $taskLocalJdks) {
        $taskCandidates += @(Get-ChildItem -LiteralPath $taskLocalJdks -Directory | Sort-Object Name -Descending | ForEach-Object { $_.FullName })
    }
    foreach ($taskVendorRoot in @("$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Microsoft")) {
        if (Test-Path -LiteralPath $taskVendorRoot) {
            $taskCandidates += @(Get-ChildItem -LiteralPath $taskVendorRoot -Directory | Where-Object { $_.Name -match 'jdk' } | Sort-Object Name -Descending | ForEach-Object { $_.FullName })
        }
    }
    $taskOnPath = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($taskOnPath) { $taskCandidates += Split-Path (Split-Path $taskOnPath.Source -Parent) -Parent }
    foreach ($taskCandidate in $taskCandidates) {
        $taskJavac = Join-Path $taskCandidate 'bin\javac.exe'
        $taskJava = Join-Path $taskCandidate 'bin\java.exe'
        $taskJar = Join-Path $taskCandidate 'bin\jar.exe'
        if ((Test-Path -LiteralPath $taskJavac) -and (Test-Path -LiteralPath $taskJava) -and (Test-Path -LiteralPath $taskJar)) {
            $taskVersion = (& $taskJavac -version 2>&1 | Out-String)
            if ($taskVersion -match 'javac (\d+)' -and [int]$Matches[1] -ge 17) {
                return [PSCustomObject]@{ Java=$taskJava; Javac=$taskJavac; Jar=$taskJar; Version=$taskVersion.Trim() }
            }
        }
    }
    throw 'JDK 17 ou superior nao encontrado. Configure JAVA_HOME para um JDK (nao apenas JRE).'
}
