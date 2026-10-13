param(
    [string]$Suite = "testng.xml",
    [string]$Groups
)

$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$suitePath = $Suite
if (-not (Test-Path -LiteralPath $suitePath)) {
    $suitePath = Join-Path $projectRoot "src\test\resources\$Suite"
}

if (-not (Test-Path -LiteralPath $suitePath -PathType Leaf)) {
    Write-Error "TestNG suite file not found: $Suite"
    exit 2
}

$suitePath = (Resolve-Path -LiteralPath $suitePath).Path
$mavenArgs = @("clean", "test", "-Dsurefire.suiteXmlFiles=$suitePath")
if (-not [string]::IsNullOrWhiteSpace($Groups)) {
    $mavenArgs += "-Dgroups=$Groups"
}

& mvn @mavenArgs
exit $LASTEXITCODE