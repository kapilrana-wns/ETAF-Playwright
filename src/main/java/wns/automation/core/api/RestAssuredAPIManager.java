package wns.automation.core.api;

import java.util.HashMap;
import java.util.List;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RestAssuredAPIManager {

	private  String bearerToken = "";

	public void setBearerToken(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public RestAssuredAPIManager(String baseURI) {
		RestAssured.baseURI = baseURI;
	}

//	public String authenticateAndGetValueFromResponse(RequestSpecification request, String endpoint, String valFrom) {
//		try {
//			Response response = request.when().post(endpoint);
//			System.out.println(response.statusCode());
//			if (response.statusCode() == 200) {
//				String responseBody = response.getBody().asString();
//				String val = new org.json.JSONObject(responseBody).getString(valFrom);
//				// this.bearerToken = token;
//				return val;
//			}
//			return "";
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return "";
//		}
//	}

	public String getBearerToken() {
		return bearerToken;
	}

	public String extractValFromResponseBodyGET(Response response, String valFrom, ResponseFormat format) {
		try {
			String responseBody = response.getBody().asString();
			switch (format) {
			case Json: {
                return new org.json.JSONObject(responseBody).getJSONArray("data").getJSONObject(0).getString(valFrom);
			}
			case Xml: {
				// return new org.json.JSONObject(responseBody).getString(valFrom);
			}
			default: {
				return new org.json.JSONObject(responseBody).getString(valFrom);
			}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

    public String extractValFromResponseBodyPOST(Response response, String valFrom, ResponseFormat format) {
        try {
            String responseBody = response.getBody().asString();
            switch (format) {
                case Json: {
                    return new org.json.JSONObject(responseBody).getString(valFrom);
                }
                case Xml: {
                    // return new org.json.JSONObject(responseBody).getString(valFrom);
                }
                default: {
                    return new org.json.JSONObject(responseBody).getString(valFrom);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public RequestSpecification createAPIRequest() {
        return RestAssured.given().relaxedHTTPSValidation().header("x-api-key", "free_user_3DFKUwB06knvgbzYD40Fw02DPqi");
    }

    public RequestSpecification createAPIPostRequest() {
        String requestBody = "{ \"name\": \"John\", \"job\": \"Tester\" }";
        return RestAssured.given().relaxedHTTPSValidation().header("x-api-key", "free_user_3DFKUwB06knvgbzYD40Fw02DPqi").contentType("application/json").body(requestBody);
    }

    public RequestSpecification createAPIPutRequest() {
        String requestBody = "{ \"name\": \"John Updated\", \"job\": \"Lead\" }";
        return RestAssured.given().relaxedHTTPSValidation().header("x-api-key", "free_user_3DFKUwB06knvgbzYD40Fw02DPqi").contentType("application/json").body(requestBody);
    }

	public void setRequestHeader(RequestSpecification req, RequestParamType paramType,
			HashMap<String, String> parameters) {
		for (HashMap.Entry<String, String> e : parameters.entrySet()) {
			switch (paramType) {
			case Header: {
				req.header(e.getKey(), e.getValue());
				break;
			}
			case FormParam: {
				req.formParam(e.getKey(), e.getValue());
				break;
			}
			default:
				break;
			}
			//System.out.println("Key: " + e.getKey() + " Value: " + e.getValue());
		}
	}

	public Response getResponse(RequestSpecification request, String endpoint, String requestType) {
		RequestType requesttype = RequestType.valueOf(requestType.toUpperCase());
		try {
			switch (requesttype) {
			case POST: {
				return request.when().post(endpoint);
			}
			case GET: {
				return request.when().get(endpoint);
			}
			case PUT:
			{
				return request.when().put(endpoint);
			}
            case DELETE:
            {
                return request.when().delete(endpoint);
            }
			default: {
				return null;
			}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public int getResponseStatusCode(RequestSpecification request, String endpoint) {
		try {
			return request.when().post(endpoint).getStatusCode();
		} catch (Exception ex) {
			ex.printStackTrace();
			return 500; // report server error
		}
	}
	
	public String authenticateAndGetValueFromResponse(RequestSpecification request, String endpoint, String valFrom) {
	try {
		Response response = request.when().post(endpoint);
		System.out.println(response.statusCode());
		if (response.statusCode() == 200) {
			String responseBody = response.getBody().asString();
			String val = new org.json.JSONObject(responseBody).getString(valFrom);
			// this.bearerToken = token;
			return val;
		}
		return "";
	} catch (Exception ex) {
		ex.printStackTrace();
		return "";
	}
	}
	
	public List<String> getAllJSONObjectsFromResponse(Response response)
	{
        List<String> jsonResponse = response.jsonPath().getList("$");
        return jsonResponse;
	}

	public String getSpecificJSONObjectsFromResponse(Response response, int objIndex)
	{
        List<String> jsonResponse = response.jsonPath().getList("$");
        return jsonResponse.get(objIndex);
	}

	public void logResult(String URL, int statusCode, Response response)
	{
		System.out.println("URL : "+ URL + " Status Code :" + response.getStatusLine() + " Response Summary :"+response.asPrettyString());
	}
	
}
