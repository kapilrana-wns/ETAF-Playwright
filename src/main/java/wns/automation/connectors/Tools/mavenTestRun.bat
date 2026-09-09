mvn clean test -Dsurefire.suiteXmlFiles=TestNg.xml -Dsurefire.includeGroups=Authentication
mvn clean test -Dsurefire.suiteXmlFiles=TestNg.xml -Dgroups=Negative
-- C:\ProgramData\Jenkins\.jenkins\workspace\SkillMatrix_Testautomation\pom.xml clean test -Dsurefire.suiteXmlFiles=TestNg.xml -Dgroups=Negative
-- Remote Execution
Jenkin Api key : 111acf22e73815ac6c31aa6facebc71e6a
: http://localhost:8080/job/SkillMatrix_CodeDeployment/build?token=111acf22e73815ac6c31aa6facebc71e6a

http://localhost:8080/JENKINS_URL/job/SkillMatrix_Testautomation/build?token=TOKEN_NAME 
-------------------



mvn clean test -DsuiteXMLFile=TestNG.xml
mvn clean test -DsuiteXMLFile=TestNG.xml -Dgroups=Authentication,Regression
mvn clean test -D "testngxmlfile" =TestNG.xml -D "groups"= Authentication,Regression

mvn clean test -DexcludeGroups=TestGroup3,TestGroup4
mvn clean test -DincludeGroups=TestGroup1,TestGroup2
mvn test -Dgroups=group1,group2
mvn clean test -D"testngxmlfile"=SmokeTestNG.xml -D"groups"=logintest,Regression


https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#groups
https://www.seleniumeasy.com/maven-tutorials/choose-selected-testng-xml-files-to-execute-using-maven

mvn clean test -DsuiteXmlFile=mytestng.xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.19.1</version>
		<configuration>
			<suiteXmlFiles>
				<suiteXmlFile>mytestng.xml</suiteXmlFile>
			</suiteXmlFiles>
			<parallel>method</parallel>
                        <threadCount>2</threadCount
		</configuration>
</plugin>

mvn clean test -Dsurefire.suiteXmlFiles=TestNG.xml -DgroupToRun=Authentication
