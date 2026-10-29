param([switch]$Run, [switch]$Test)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$classesDirectory = Join-Path $projectRoot 'build/classes'
New-Item -ItemType Directory -Force -Path $classesDirectory | Out-Null
$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/main/java') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
if ($Test) {
    $sourceFiles += @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/test/java') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
}
& javac --release 17 -Xlint:all -d $classesDirectory $sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
Write-Host 'Compilation passed.'
if ($Test) {
    & java -cp $classesDirectory restaurantrush.SkeletonTest
    if ($LASTEXITCODE -ne 0) { throw 'Skeleton tests failed.' }
}
if ($Run) {
    & java -cp $classesDirectory restaurantrush.Main
    if ($LASTEXITCODE -ne 0) { throw 'Restaurant Rush failed.' }
}
