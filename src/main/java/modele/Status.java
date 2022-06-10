package modele;

public enum Status {

	TO_EXECUTE("READY"),
	IN_PROGRESS("RUNNING"),
	SUCCESS("SUCCESS"),
	FAILURE("FAILURE"),
	BLOQUED("BLOCKED"),
	UNTESTABLE("UNTESTABLE");
	
	private String value;
	
	private Status(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	

    public static Status fromString(String value) {
        for (Status status : Status.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
