package squashConnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import modele.Status;
import modele.TestCase;

public class SquashAPI {

	private String host;
	private String port;
	private String user;
	private String password;
	
	public SquashAPI(String host, String port, String user, String password) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
	}
	
	/**
	 * Establishes a connection to the SQUASH TM API with the identifiers defined in the constructor
	 */
	public void connect() {
		RestAssured.baseURI = "http://"+this.host;
		RestAssured.port = Integer.parseInt(this.port);
		RestAssured.authentication = RestAssured.basic(this.user, this.password);
		RestAssured.useRelaxedHTTPSValidation();
	}
	
	private String getProjectId(String projectName) throws SquashAPIException {
		connect();
		Response response = RestAssured.get("http://"+host+"/squash/api/rest/latest/projects?projectName="+projectName);
		int codeResponse = response.getStatusCode();
		if(codeResponse != 200) {
			throw new SquashAPIException("Unknown project name");
		}
		JsonPath path = response.jsonPath();
		return path.getString("id");
	}
	
	private String getCampaignId(String projectId, String campaignName) throws SquashAPIException {
		Response response = RestAssured.get("http://"+host+"/squash/api/rest/latest/projects/"+projectId+"/campaigns");
		int codeResponse = response.getStatusCode();
		if(codeResponse != 200) {
			throw new SquashAPIException("Unknown campaign name");
		}
		JsonPath path = response.jsonPath();
		List<Map<String, Object>> campaigns = path.getJsonObject("_embedded.campaigns");
		// find testCase with name corresponding to parameter testCaseName
		for (Map<String, Object> project : campaigns) {
			if(project.get("name").equals(campaignName)) {
				return (String) String.valueOf(project.get("id"));
			}
		}
		return "";
	}
	
	private String getIterationId(String campaignId, String IterationName) throws SquashAPIException {
		Response response = RestAssured.get("http://"+host+"/squash/api/rest/latest/campaigns/"+campaignId+"/iterations");
		int codeResponse = response.getStatusCode();
		if(codeResponse != 200) {
			throw new SquashAPIException("Unknown iteration name");
		}
		JsonPath path = response.jsonPath();
		List<Map<String, Object>> campaigns = path.getJsonObject("_embedded.iterations");
		// find testCase with name corresponding to parameter testCaseName
		for (Map<String, Object> project : campaigns) {
			if(project.get("name").equals(IterationName)) {
				return (String) String.valueOf(project.get("id"));
			}
		}
		return "";
	}
	
	/**
	 * Retrieve all test cases by name of project/campaign/iteration
	 * <pre>
	 * 
	 * tree structure : Project1
	 * 			|__ Campaign1
	 * 				|__ iteration1
	 * 					|__ TestCase1
	 * 					|__ TestCase2
	 * 					|__ TestCaseX
	 * </pre>
	 * 
	 * @param projectName
	 * @param campaignName
	 * @param iterationName
	 * @throws SquashAPIException
	 */
	public ArrayList<TestCase> getAllTestsInIteration(String projectName,String campaignName, String iterationName) throws SquashAPIException {
		String projectId = getProjectId(projectName);
		if(projectId.isBlank())
			throw new SquashAPIException("No identifier find for project name "+projectName);
		String campaignId = getCampaignId(projectId, campaignName);
		if(campaignId.isBlank())
			throw new SquashAPIException("No identifier find for campaign name "+campaignName);
		String iterationId = getIterationId(campaignId, iterationName);
		if(iterationId.isBlank())
			throw new SquashAPIException("No identifier find for iteration name "+iterationName);
		
		return getAllTestsInIteration(iterationId);
	}
	
	/**
	 * Retrieve all test cases by iteration identifier
	 * 
	 * @param projectName
	 * @param campaignName
	 * @param iterationName
	 * @throws SquashAPIException
	 */
	public ArrayList<TestCase>  getAllTestsInIteration(String iterationId) throws SquashAPIException {
		Response response = RestAssured.get("http://"+host+"/squash/api/rest/latest/iterations/"+iterationId+"/test-plan");
		int codeResponse = response.getStatusCode();
		if(codeResponse != 200) {
			throw new SquashAPIException("Error with iteration identifier: "+iterationId);
		}
		JsonPath path = response.jsonPath();
		ArrayList<TestCase> testCases = new ArrayList<>();
		List<Map<String, Object>> testCasesJSON = path.getJsonObject("_embedded.test-plan");
		for (Map<String, Object> testCaseJSON : testCasesJSON) {
			testCases.add(new TestCase(testCaseJSON));
		}
		if(testCases.isEmpty())
			throw new SquashAPIException("No testCase for iteration identifier "+iterationId);
		return testCases;
	}

	/**
	 * Create a new execution in the test case with the status 
	 * that will be applied to the test case
	 * @param testCase
	 * @param status
	 * @throws SquashAPIException
	 */
	public void setTestStatus(TestCase testCase, Status status) throws SquashAPIException {
		//create an execution in the testCase
		int idExecution = createExecutionForTestplanItem(testCase);
		//edit the new execution with status 
		editExecutionStatus(idExecution,status);
	}
	
	private int createExecutionForTestplanItem(TestCase testCase) throws SquashAPIException {
		if(testCase.getIdTestPlan().isEmpty())
			throw new SquashAPIException("The TestPlan identifier is empty");
		Response response = RestAssured.post("http://"+host+"/squash/api/rest/latest/iteration-test-plan-items/"+testCase.getIdTestPlan()+"/executions");
		int codeResponse = response.getStatusCode();
		if(codeResponse != 200) {
			throw new SquashAPIException("Error with identifier TestPlan "+testCase.getIdTestPlan());
		}
		JsonPath path = response.jsonPath();
		int idExecution = path.get("id");
		return idExecution;
	}
	
	
	private void editExecutionStatus(int idExecution, Status status) throws SquashAPIException {
		String body = "{\"_type\" : \"execution\",\"execution_status\" : \""+status.getValue()+"\"}";
		Response response = RestAssured.given()
				.header("Content-type", "application/json")
				.and()
				.body(body)
				.when()
				.patch("/squash/api/rest/latest/executions/"+idExecution+"?fields=execution_status")
				.then()
				.extract()
				.response();
		int codeResponse = response.getStatusCode();
		if(codeResponse != 200) {
			throw new SquashAPIException("Error when editing status of execution with the identifier "+idExecution);
		}
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
