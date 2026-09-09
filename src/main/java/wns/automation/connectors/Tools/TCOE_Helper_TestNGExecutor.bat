@echo off 
Rem This batch is to run the Selenium Scripts using batch File
Rem It takes two parameter, 
Rem  1.  Provide list of suite xml files separated by comma after -Dsurefire.suiteXmlFiles= parameter
Rem  2.  Provide the list of test script group that is to be executed after -Dgroups=
set message=ETAF Batch command file
echo %message%
cd\
Rem replace below directory path from where test automation pom.xml is stored
cd C:\ProgramData\Jenkins\.jenkins\workspace\SkillMatrix_Testautomation
Rem Provide suite files names and test group to be executed
mvn clean test -Dsurefire.suiteXmlFiles=TestNg1.xml -Dgroups=Positive