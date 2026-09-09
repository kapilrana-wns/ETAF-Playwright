package testscripts.RestAssuredAPI;

import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import testconfig.APITestManager;
import wns.automation.core.api.ResponseFormat;

public class testAPIRestAssured extends APITestManager{

    // GET Request (List Users)
	@Test(description = "Verify Get Request - List Users", priority=1, dataProvider = "CsvDataProvider")
	public void verifyGetRequestListUsers(String requestURL, String requestType, String verifyTag, String verifyText)
	{
		System.out.println("TC: Verify Get Request - List Users");
		RequestSpecification request = restAssuredManager.createAPIRequest();
		Reporter.log(Status.INFO, "API Request to verify the user info is created");
        Response response  = restAssuredManager.getResponse(request, requestURL, requestType);
		restAssuredManager.logResult(RestAssured.baseURI + requestURL, response.statusCode(), response);
        Reporter.log(Status.INFO, "Reponse code received - " + response.statusCode());
        System.out.println("Reponse code = " + response.statusCode());
        System.out.println("Value inside Response for page = " + restAssuredManager.extractValFromResponseBodyGET(response, verifyTag, ResponseFormat.Json));
        System.out.println("Text to verify for page = " + verifyText);
        Assert.assertEquals(restAssuredManager.extractValFromResponseBodyGET(response, verifyTag, ResponseFormat.Json), verifyText);
		String responseBody = response.getBody().asString();
		Reporter.log(Status.INFO, "Json data received : " + restAssuredManager.extractValFromResponseBodyGET(response, verifyTag, ResponseFormat.Json));
		Assert.assertEquals(response.statusCode(), 200);
        Reporter.log(Status.INFO, "Response Summary :" + response.asPrettyString());
	}

    // POST Request (Create User)
    @Test(description = "Verify Post Request - Create User", priority=2, dataProvider = "CsvDataProvider")
    public void verifyPostRequestCreateUser(String requestURL, String requestType, String verifyTag, String verifyText)
    {
        System.out.println("TC: Verify Post Request - Create User");
        RequestSpecification request = restAssuredManager.createAPIPostRequest();
        Reporter.log(Status.INFO, "API Request to verify the user info is created");
        Response response  = restAssuredManager.getResponse(request, requestURL, requestType);
        restAssuredManager.logResult(RestAssured.baseURI + requestURL, response.statusCode(), response);
        Reporter.log(Status.INFO, "Reponse code received - " + response.statusCode());
        System.out.println("Reponse code = " + response.statusCode());
        System.out.println("Value inside Response for first name = " + restAssuredManager.extractValFromResponseBodyPOST(response, verifyTag, ResponseFormat.Json));
        System.out.println("Text to verify for first name = " + verifyText);
        Assert.assertEquals(restAssuredManager.extractValFromResponseBodyPOST(response, verifyTag, ResponseFormat.Json), verifyText);
        String responseBody = response.getBody().asString();
        Reporter.log(Status.INFO, "Json data received : " + restAssuredManager.extractValFromResponseBodyPOST(response, verifyTag, ResponseFormat.Json));
        Assert.assertEquals(response.statusCode(), 201);
        Reporter.log(Status.INFO, "Response Summary :" + response.asPrettyString());
    }

    // PUT Request (Update User)
    @Test(description = "Verify PUT Request - Update User", priority=3, dataProvider = "CsvDataProvider")
    public void verifyPutRequestUpdateUser(String requestURL, String requestType, String verifyTag, String verifyText)
    {
        System.out.println("TC: Verify PUT Request - Update User");
        RequestSpecification request = restAssuredManager.createAPIPutRequest();
        Reporter.log(Status.INFO, "API Request to verify the user info is created");
        Response response  = restAssuredManager.getResponse(request, requestURL, requestType);
        restAssuredManager.logResult(RestAssured.baseURI + requestURL, response.statusCode(), response);
        Reporter.log(Status.INFO, "Reponse code received - " + response.statusCode());
        System.out.println("Reponse code = " + response.statusCode());
        System.out.println("Value inside Response for first name = " + restAssuredManager.extractValFromResponseBodyPOST(response, verifyTag, ResponseFormat.Json));
        System.out.println("Text to verify for first name = " + verifyText);
        Assert.assertEquals(restAssuredManager.extractValFromResponseBodyPOST(response, verifyTag, ResponseFormat.Json), verifyText);
        String responseBody = response.getBody().asString();
        Reporter.log(Status.INFO, "Json data received : " + restAssuredManager.extractValFromResponseBodyPOST(response, verifyTag, ResponseFormat.Json));
        Assert.assertEquals(response.statusCode(), 200);
        Reporter.log(Status.INFO, "Response Summary :" + response.asPrettyString());
    }

    // DELETE Request (Delete User)
    @Test(description = "Verify DELETE Request - Delete User", priority=4, dataProvider = "CsvDataProvider")
    public void verifyDeleteRequestDeleteUser(String requestURL, String requestType)
    {
        System.out.println("TC: Verify Delete Request - Delete User");
        RequestSpecification request = restAssuredManager.createAPIRequest();
        Reporter.log(Status.INFO, "API Request to verify the user info is created");
        Response response  = restAssuredManager.getResponse(request, requestURL, requestType);
        restAssuredManager.logResult(RestAssured.baseURI + requestURL, response.statusCode(), response);
        Reporter.log(Status.INFO, "Reponse code received - " + response.statusCode());
        System.out.println("Reponse code = " + response.statusCode());
        Assert.assertEquals(response.statusCode(), 204);
    }
}