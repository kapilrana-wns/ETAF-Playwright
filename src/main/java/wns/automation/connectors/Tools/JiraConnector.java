
/**
 *
 */
package wns.automation.connectors.Tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Base64;

/**
 * @author PIM_TCOE_ADM
 *
 */
public class JiraConnector implements IToolsConnector {

	private URI jiraUrl;
	private String jiraApiKey;
	private String userName;
	private String ProjectKey;
	private long issueTypeID;

	private String cycleId;
	private String projectId;
	private String versionId;
	private Properties prop;
	//private SearchResult searchResult = null;

	private String getJWTToken(String methodType, String pathUrl) throws URISyntaxException {
		String baseURI = getProperties().getProperty("baseURI");
		String accessKey = getProperties().getProperty("accessKey");
		String secretKey = getProperties().getProperty("secretKey");
		String accountId = getProperties().getProperty("accountId");
		ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(baseURI, accessKey, secretKey, accountId).build();
		JwtGenerator jwtGenerator = client.getJwtGenerator();
		String finaluri = baseURI + pathUrl;
		System.out.println(finaluri);
		URI uri = new URI(finaluri);
		int expirationInSec = 360;
		String jwt = jwtGenerator.generateJWT(methodType, uri, expirationInSec);
		return jwt;
	}

	private static JiraConnector jconnector = null;

	public static JiraConnector getInstance(Properties props) {
		if (jconnector == null) {
			synchronized (JiraConnector.class) {
				if (jconnector == null) {
					jconnector = new JiraConnector();
					jconnector.setProperties(props);
					jconnector.establishAuthentication();
				}
			}
		}
		return jconnector;
	}


	public void createTestCycle() {

		try {
			String pathParam = "/public/rest/api/3.0/cycle";
			String token = getJWTToken("POST", pathParam);
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
			// System.out.println(formatter.format(date));
			String cycleName = getProperties().getProperty("testCycleName") + formatter.format(date);
			String cycleDesc = getProperties().getProperty("cycleDesc");

			RestAssured.baseURI = getProperties().getProperty("baseURI");
			Response response1 = RestAssured.given().relaxedHTTPSValidation().header("Authorization", token)
					.header("zapiAccessKey", getProperties().getProperty("accessKey"))
					.header("Content-Type", "application/json")
					.body("{\r\n" + "  \"name\": \"" + cycleName + "\",\r\n" + "  \"description\": \"" + cycleDesc
							+ "\",\r\n" + "  \"versionId\": " + getVersionId() + ",\r\n" + "  \"projectId\": "
							+ getProjectId() + "\r\n" + "}")
					.when().post(pathParam).then().extract().response();
			String response = response1.asString();
			System.out.println(response);
			JsonPath js = new JsonPath(response);

			cycleId = js.get("id");
		} catch (Exception ex) {
			System.out.println("createTestCycle method failed..." + ex.getCause());
			ex.printStackTrace();
		}

	}

	public boolean isDefectExist(Defect defect) {

		try {

			String auth = Base64.getEncoder()
					.encodeToString((userName + ":" + jiraApiKey).getBytes());

			String jql = "project = " + ProjectKey +
					" AND issuetype = Bug AND summary ~ \"" +
					defect.getDefectSummary().replace("\"", "'") + "\"";

			Response response = RestAssured
					.given()
					.header("Authorization", "Basic " + auth)
					.header("Content-Type", "application/json")
					.queryParam("jql", jql)
					.get(jiraUrl + "/rest/api/3/search");

			if (response.statusCode() == 200) {

				int total = response.jsonPath().getInt("total");

				if (total > 0) {
					System.out.println("Defect already exists in Jira");
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public String getCycleID(String cyclename) throws URISyntaxException {

		String pathParam = "/public/rest/api/3.0/cycle"; // release url

		String token = getJWTToken("POST", pathParam);
		String cycleDesc = getProperties().getProperty("cycleDesc");

		RestAssured.baseURI = getProperties().getProperty("baseURI");
		String response = RestAssured.given().relaxedHTTPSValidation().header("Authorization", token)
				.header("zapiAccessKey", getProperties().getProperty("accessKey"))
				.header("Content-Type", "application/json")
				.body("{\r\n" + "  \"name\": \"" + cyclename + "\",\r\n" + "  \"description\": \"" + cycleDesc
						+ "\",\r\n" + "  \"versionId\": " + getVersionId() + ",\r\n" + "  \"projectId\": "
						+ getProjectId() + "\r\n" + "}")
				.when().post(pathParam).then().extract().response().asString();

		JsonPath js = new JsonPath(response);
		cycleId = js.get("id");

		System.out.println(response);

		return cycleId;
	}

	/**
	 * Method is used to add tests to cycle using the valid JQL query.
	 */
	@Override
	public void addTestsToCycle(String Jql) {
		try {
			String pathParam = "/public/rest/api/3.0/executions/add/cycle/" + cycleId;
			String token = getJWTToken("POST", pathParam);
			String jqlQuery = Jql; // prop.getProperty("jqlQuery");

			RestAssured.baseURI = getProperties().getProperty("baseURI");
			String response = RestAssured.given().relaxedHTTPSValidation().header("Authorization", token)
					.header("zapiAccessKey", getProperties().getProperty("accessKey"))
					.header("Content-Type", "application/json")
					.body("{\r\n" + "\"projectId\": \"" + getProjectId() + "\",\r\n" + "\"versionId\": "
							+ getVersionId() + ",\r\n" + "\"jql\": \"" + jqlQuery + "\",\r\n" + "\"method\":\"2\"\r\n"
							+ "}")
					.when().post(pathParam).then().extract().response().asString();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Adding Test to test cycle failed : addTestsToCycle .." + ex.getCause());
		}

		// System.out.println(response);
	}

	public String getExecutionId(String issuekey) throws Exception {
		String queryParam = "?projectId=" + getProjectId() + "&versionId=" + getVersionId();
		String pathParam = "/public/rest/api/3.0/executions/search/cycle/" + cycleId + queryParam;
		String pathParam1 = "/public/rest/api/3.0/executions/search/cycle/" + cycleId;
		String token = getJWTToken("GET", pathParam);
		String executionId = "";

		RestAssured.baseURI = getProperties().getProperty("baseURI");
		String response = RestAssured.given().queryParam("projectId", getProjectId())
				.queryParam("versionId", getVersionId()).relaxedHTTPSValidation().header("Authorization", token)
				.header("zapiAccessKey", getProperties().getProperty("accessKey"))
				.header("Content-Type", "application/json").when().get(pathParam1

				).then().extract().response().asString();

		JsonPath js = new JsonPath(response);
		int executionCount = js.getInt("searchObjectList.size()");
		for (int i = 0; i < executionCount; i++) {
			String issueKey = js.get("searchObjectList[" + i + "].issueKey").toString();
			if (issueKey.equalsIgnoreCase(issuekey)) {
				executionId = js.get("searchObjectList[" + i + "].execution.id");
				break;
			}

		}
		return executionId;

	}

	public String updateTestExecution_passingString(String issuekey, int status, String log) throws Exception {

		String pathParam = "/public/rest/api/3.0/executions";
		String token = getJWTToken("POST", pathParam);
		String executionId = getExecutionId(issuekey);

		RestAssured.baseURI = getProperties().getProperty("baseURI");
		RestAssured.given()

				.relaxedHTTPSValidation().header("Authorization", token)
				.header("zapiAccessKey", getProperties().getProperty("accessKey"))
				.header("Content-Type", "application/json")
				.body("{\"executions\":[\"" + executionId + "\"],\"status\":" + status
						+ ",\"clearDefectMappingFlag\":false,\"testStepStatusChangeFlag\":true,\"stepStatus\":-1}")
				.when().post("public/rest/api/3.0/executions").then().extract().response().asString();

		return log;
	}

	public void updateTestResults() throws Exception {

		try {
			setProperties(new Properties());
			FileInputStream fis = new FileInputStream(
					System.getProperty("user.dir") + "/src/test/resources/jira.properties");
			getProperties().load(fis);
			setProjectId(getProperties().getProperty("projectId"));
			setVersionId(getProperties().getProperty("versionId"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		createTestCycle();
		addTestsToCycle("project = SV AND Key = SV-571");

		System.out.println(getProjectId());
		System.out.println(getVersionId());
	}

	public String getProjectKey() {
		return ProjectKey;
	}

	public void setProjectKey(String projectKey) {
		this.ProjectKey = projectKey;
	}

	public long getIssueTypeID() {
		return issueTypeID;
	}

	public void setIssueTypeID(long issueTypeID) {
		this.issueTypeID = issueTypeID;
	}



	public URI getJiraUrl() {
		return jiraUrl;
	}

	public void setJiraUrl(URI jiraUrl) {
		try {
			this.jiraUrl = jiraUrl;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public String getJiraApiKey() {
		return jiraApiKey;
	}

	public void setJiraApiKey(String jiraApiKey) {
		this.jiraApiKey = jiraApiKey;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public Object establishAuthentication() {
		System.out.println("Jira REST v3 Authentication will be handled per request.");
		return null;
	}


	//	@Override
//	public String createDefect(Defect defect) {
//		String defectID="";
//		if (!isDefectExist(defect)) {
//			IssueInput issueInput = new IssueInputBuilder(ProjectKey, issueTypeID).setSummary(defect.getDefectSummary())
//					.setDescription(defect.getDefectDescription())
//					.setFieldInput(new FieldInput("labels", defect.getTags()))
//					// .setAssigneeName(defect.getAssigneeName())
//					// .setReporterName(defect.getReporterName())
//					.build();
//
//			try {
//				BasicIssue jiraoutput = restClient.getIssueClient().createIssue(issueInput).claim();
//				defectID = jiraoutput.getKey();
//			//	restClient.close();
//				return defectID;
//
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				return null;
//			}
//		}
//		else
//		{
//
//
//			try {
//				for (BasicIssue issue : searchResult.getIssues()) {
//					System.out.println(issue.getKey() + "---" + issue.getSelf());
//					Issue issue1 = restClient.getIssueClient().getIssue(issue.getKey()).get();
//					System.out.println(issue1.getSummary());
//					defectID = issue1.getKey();
//					break;  // Take only the first defect in the list
//				}
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				return null;
//			}
//		}
//		return defectID;
//
//
//
//	}
	@Override
	public String createDefect(Defect defect) {

		try {

			String auth = Base64.getEncoder()
					.encodeToString((userName + ":" + jiraApiKey).getBytes());

			// ---------- ADF DESCRIPTION (JAVA 8 SAFE) ----------

			Map<String, Object> textNode = new HashMap<>();
			textNode.put("type", "text");
			textNode.put("text", defect.getDefectDescription());

			Map<String, String> assignee = new HashMap<>();
			assignee.put("id", "5cbab09955193a11cd0ac0a6");

			Map<String, Object> paragraphNode = new HashMap<>();
			paragraphNode.put("type", "paragraph");

			List<Map<String, Object>> paragraphContent = new ArrayList<>();
			paragraphContent.add(textNode);
			paragraphNode.put("content", paragraphContent);

			Map<String, Object> description = new HashMap<>();
			description.put("type", "doc");
			description.put("version", 1);

			List<Map<String, Object>> descriptionContent = new ArrayList<>();
			descriptionContent.add(paragraphNode);
			description.put("content", descriptionContent);

			// ----------------------------------------------------

			Map<String, String> project = new HashMap<>();
			project.put("key", ProjectKey);

			Map<String, String> issueType = new HashMap<>();
			issueType.put("name", "Bug");

			List<String> labels = new ArrayList<>();
			labels.add("AutomationBug");

			Map<String, Object> fields = new HashMap<>();
			fields.put("project", project);
			fields.put("summary", defect.getDefectSummary());
			fields.put("description", description);
			fields.put("issuetype", issueType);
			fields.put("labels", labels);
			fields.put("assignee", assignee);

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("fields", fields);

			Response response = RestAssured
					.given()
					.header("Authorization", "Basic " + auth)
					.header("Content-Type", "application/json")
					.body(requestBody)
					.post(jiraUrl + "/rest/api/3/issue");

			if (response.statusCode() == 201) {

				String issueKey = response.jsonPath().getString("key");
				System.out.println("Defect Created: " + issueKey);
				return issueKey;

			} else {

				System.out.println("Defect Creation Failed:");
				System.out.println(response.asPrettyString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return the versionId
	 */
	public String getVersionId() {
		return versionId;
	}

	/**
	 * @param versionId the versionId to set
	 */
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	/**
	 * @return the prop
	 */
	public Properties getProperties() {
		return prop;
	}

	/**
	 * @param prop the prop to set
	 */
//	@Override
	public void setProperties(Properties prop) {

		try {

			this.prop = prop;

			this.projectId = prop.getProperty("projectId");
			this.versionId = prop.getProperty("versionId");

			this.jiraApiKey = prop.getProperty("TestManagementToolApiKey");
			this.jiraUrl = new URI(prop.getProperty("TestManagementToolURL"));
			this.userName = prop.getProperty("TestManagementProjectUserName");
			this.ProjectKey = prop.getProperty("TestManagementProjectKey");

			System.out.println("Jira Connector Initialized Successfully");
			System.out.println("Project Key: " + ProjectKey);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public void updateTestCaseResult(String testCaseID, int Status, String comment) {
		try {
			this.updateTestExecution_passingString(testCaseID, Status, comment);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			System.out.println("Update test results in Jira failed " + ex.getCause());
			ex.printStackTrace();
		}

	}

	@Override
	public void linkTestCaseandDefect(String defectID, String testCaseID) {

		try {

			String auth = Base64.getEncoder()
					.encodeToString((userName + ":" + jiraApiKey).getBytes());

			String body = "{\n" +
					"  \"type\": {\"name\": \"Relates\"},\n" +
					"  \"inwardIssue\": {\"key\": \"" + defectID + "\"},\n" +
					"  \"outwardIssue\": {\"key\": \"" + testCaseID + "\"}\n" +
					"}";

			Response response = RestAssured
					.given()
					.header("Authorization", "Basic " + auth)
					.header("Content-Type", "application/json")
					.body(body)
					.post(jiraUrl + "/rest/api/3/issueLink");
			System.out.println("Jira Response:");
			System.out.println(response.asPrettyString());

			if (response.statusCode() == 201) {
				System.out.println("Linked " + defectID + " with " + testCaseID);
			} else {
				System.out.println("Linking failed: " + response.asString());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
