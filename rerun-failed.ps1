$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$suitePath = Join-Path $projectRoot "src\test\resources\testng-failedcases.xml"
if (-not (Test-Path -LiteralPath $suitePath -PathType Leaf)) {
    Write-Error "Failed-test suite file not found: $suitePath"
    exit 2
}

$suitePath = (Resolve-Path -LiteralPath $suitePath).Path
& mvn test "-Dsurefire.suiteXmlFiles=$suitePath"
exit $LASTEXITCODE
