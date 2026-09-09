Set-Location -LiteralPath $PSScriptRoot
mvn test "-Dsurefire.suiteXmlFiles=src\test\resources\testng-failedcases.xml"
