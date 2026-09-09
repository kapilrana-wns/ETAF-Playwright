package wns.automation.connectors.Tools;
public interface IToolsConnector {

	 public String createDefect(Defect defect);
	 public Object establishAuthentication();
	 public void createTestCycle();
	 public void addTestsToCycle(String query);
	 public void updateTestCaseResult(String testCaseID, int Status, String comment);
	 public void linkTestCaseandDefect(String defectID, String testCaseID);
	 
	

}
