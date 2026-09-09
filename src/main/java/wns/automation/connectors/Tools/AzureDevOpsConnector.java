package wns.automation.connectors.Tools;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONObject;

//import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.aventstack.extentreports.Status;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wns.automation.core.api.ResponseFormat;
import wns.automation.core.api.RestAssuredAPIManager;
import wns.automation.utilities.TestUtility;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.AuthenticationSpecification;
import io.restassured.specification.RequestSpecification;

import org.apache.commons.codec.binary.Base64;

public class AzureDevOpsConnector implements IToolsConnector {

	private static AzureDevOpsConnector azuredevopsconnector = null;
	private Properties prop;
	private String coreServer;
	private String organization;
	private String project;
	private String apiversion;
	private String UserId;
	private String workItemType;
	private String accessToken;
	private static RequestSpecification request;

	private String baseURL;
	private RestAssuredAPIManager restAssuredManager;
	private Integer defectID;
	// private AuthenticationSpecification request;

/*	public static void main(String[] argc) {
		Object userdir = System.getProperty("user.dir");
		String propertyFile = userdir + "\\src\\test\\java\\testconfig\\test.properties";
		Properties props = TestUtility.getTestConfig(propertyFile);
		AzureDevOpsConnector azc = AzureDevOpsConnector.getInstance(props);
		System.out.println(azc.verifyConnectivity());
		Defect defect = new Defect();
		defect.setDefectSummary("Manager not able to approve workflow");
		defect.setDefectDescription("Login as Manager and approve the workflow  # 00001");
		azc.createDefect(defect);

	} */

	public static AzureDevOpsConnector getInstance(Properties props) {
		if (azuredevopsconnector == null) {
			synchronized (AzureDevOpsConnector.class) {
				if (azuredevopsconnector == null) {
					azuredevopsconnector = new AzureDevOpsConnector();
					azuredevopsconnector.setProperties(props);
					request = azuredevopsconnector.establishAuthentication();
				}
			}
		}
		return azuredevopsconnector;
	}

	private void setProperties(Properties properties) {
		try {
			this.prop = properties;
			this.coreServer = prop.getProperty("coreServer");
			this.organization = prop.getProperty("organization");
			this.project = prop.getProperty("project");
			this.apiversion = prop.getProperty("apiversion");
			this.UserId = prop.getProperty("UserId");
			this.workItemType = prop.getProperty("workItemType");

			this.accessToken = prop.getProperty("accessToken");
			this.baseURL = prop.getProperty("TestManagementToolURL");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean verifyConnectivity() {
		Response response = restAssuredManager.getResponse(request, "/WTS-ADM/_apis/projects?api-version=6.0", "GET");
		if (response.statusCode() == 200) {
			return true;
		} else {
			System.out.println("Connectivity with the test management tool fail,ensure test.properties has "
					+ "correct details included");
			return false;
		}
	}

	@Override
	public String createDefect(Defect defect) {

		String url = baseURL + "/" + organization + "/" + project + "/_apis/wit/workitems/$" + workItemType
				+ "?api-version=" + apiversion;

		// Post method of
		// https://dev.azure.com/{{organization}}/{{project}}/_apis/wit/workitems/${{type}}?api-version=6.
		// System.out.println(url);
		// System.out.println(getJsonFormatDefect(defect));
		if (!defectNotExists(defect)) {
			establishAuthentication();
			String jsonFormatDefectDetails = getJsonFormatDefect(defect);

			request.header("Content-Type", "application/json-patch+json");
			request.body(jsonFormatDefectDetails);
			Response response = request.post(url);
			if (response.statusCode() == 200) {
				System.out.println("Defect Created ");
				int defectID = new org.json.JSONObject(response.getBody().asString()).getInt("id");
				System.out.println(Integer.toString(defectID));
				// System.out.println(response.asPrettyString());
				// return Integer.toString(defectID);
				return Integer.toString(defectID);
			} else {
				defectID=0;
				return "ERROR occured while creating defect";
				
			}

		} else {
			return Integer.toString(defectID);
		}

//		return s;

	}

	private String getJsonFormatDefect(Defect defect) {
		String ws = "[\r\n" + "    {\r\n" + "        \"op\": \"add\",\r\n"
				+ "        \"path\": \"/fields/System.Title\",\r\n" + "        \"from\": null,\r\n"
				+ "        \"value\":" + "\"" + defect.getDefectSummary() + "\"" + "    },\r\n" + "    {\r\n"
				+ "        \"op\": \"add\",\r\n" + "        \"path\": \"/fields/System.State\",\r\n"
				+ "        \"from\": null,\r\n" + "        \"value\": \"New\"\r\n" + "    },\r\n" + "    {\r\n"
				+ "        \"op\": \"add\",\r\n" + "        \"path\": \"/fields/Microsoft.VSTS.TCM.ReproSteps\",\r\n"
				+ "        \"from\": null,\r\n" + "        \"value\":" + "\"" + defect.getDefectDescription() + "\""
				+ "    }\r\n" + "]";

		// System.out.println("json");
		// System.out.println(ws);

		return ws;
	}

	private boolean defectNotExists(Defect defect) {

		// String url = https://dev.azure.com/WTS-ADM/_apis/wit/wiql?api-version=6
		establishAuthentication();
		String url = baseURL + "/" + organization + "/_apis/wit/wiql" + "?api-version=" + apiversion;
		System.out.println(url);
		String wql = "{\r\n  \"query\": \"Select [System.Id], [System.Title], "
				+ "[System.State] From WorkItems Where [System.Title] = " + "'" + defect.getDefectSummary() + "'"
				+ " AND [System.WorkItemType] = 'Bug' AND [State] <> 'Closed' AND [State] <> 'Removed'  AND [State] <> 'Done'"
				+ "order by [Microsoft.VSTS.Common.Priority] asc, [System.CreatedDate] desc\"\r\n}";
		System.out.println(wql);
		// return wql;
		//request.header("Content-Type", "application/json");
		request.body(wql);
		Response response = request.post(url);
		if (response.statusCode() == 200) {
			System.out.println("Defect Exists ");
			System.out.println(response.asPrettyString());
			List<Integer> jsonResponse = response.jsonPath().getList("workItems.id");
			if (jsonResponse.size() > 0) {
				defectID = jsonResponse.get(0);
				System.out.println("Existing defect is " + defectID);
				return true;
			} else {
				defectID = 0;
				return false;
			}

		}

		else
			System.out.println(response.getStatusCode());
		return false;
	}

	@Override
	public RequestSpecification establishAuthentication() {
        System.out.println(baseURL);
		restAssuredManager = new RestAssuredAPIManager(baseURL);
		RequestSpecification request = restAssuredManager.createAPIRequest();
        request.auth().preemptive().basic("", accessToken);
        request.header("Accept", "application/json");
        //request.header("Authorization", "Basic " + accessToken);
		return request;
	}

	@Override
	public void createTestCycle() {
		// TODO Auto-generated method stub
	}

	@Override
	public void addTestsToCycle(String query) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateTestCaseResult(String testCaseID, int Status, String comment) {
		// TODO Auto-generated method stub
	}

	@Override
	public void linkTestCaseandDefect(String defectID, String testCaseID) {
		// TODO Auto-generated method stub
	}
}