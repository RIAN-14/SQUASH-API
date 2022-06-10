package modele;

import java.util.Map;

public class TestCase {
	
	private String id;
	private String idTestPlan;
	private String name;
	private Status executionStatus;
	

	public TestCase() {
		
	}

	@SuppressWarnings("unchecked")
	public TestCase(Map<String, Object> testCaseJSON) {
		this.idTestPlan = String.valueOf(testCaseJSON.get("id"));
		executionStatus = Status.fromString((String) testCaseJSON.get("execution_status"));
		Map<String, Object> map = (Map<String, Object>) testCaseJSON.get("referenced_test_case");
		this.id = String.valueOf(map.get("id"));
		this.name = (String) map.get("name");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdTestPlan() {
		return idTestPlan;
	}

	public void setIdTestPlan(String idTestPlan) {
		this.idTestPlan = idTestPlan;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(Status executionStatus) {
		this.executionStatus = executionStatus;
	}

	@Override
	public String toString() {
		return "TestCase [id=" + id + ", idTestPlan=" + idTestPlan + ", name=" + name + ", executionStatus="
				+ executionStatus + "]";
	}
}
