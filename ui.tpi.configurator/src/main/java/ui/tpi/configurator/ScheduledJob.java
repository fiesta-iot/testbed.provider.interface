package ui.tpi.configurator;

import java.util.List;

public class ScheduledJob {
	
	/**
	 * The endpoint URI
	 */
	private String endpointURI;
	
	/**
	 * The testbed's URI
	 */
	private String testbedURI;
	
	/**
	 * The list of sensor IDs
	 */
	private List<String> sensorIDs;
	
	/**
	 * The scheduled job's id
	 */
	private String jobID;
	
	/**
	 * The scheduled job's name
	 */
	private String scheduleName;
		
	/**
	 * The default constructor
	 */
	public ScheduledJob(){	
	}
	
	/**
	 * @param endpointURI
	 * 		the endpoint URI
	 * @param testbedURI
	 * 		the testbed's URI
	 * @param sensorIDs
	 * 		the list of sensor IDs
	 */
	public ScheduledJob(String endpointURI, String testbedURI, List<String> sensorIDs ) {
		this.sensorIDs = sensorIDs;
		this.endpointURI = endpointURI;
		this.testbedURI = testbedURI;
	}
	
	/**
	 * @param endpointURI
	 * 		the endpoint URI
	 * @param testbedURI
	 * 		the testbed's URI
	 * @param sensorIDs
	 * 		the list of sensor IDs
	 * @param jobID
	 * 		the job's ID
	 */
	public ScheduledJob(String endpointURI, String testbedURI, List<String> sensorIDs, String jobID, String scheduleName) {
		this.sensorIDs = sensorIDs;
		this.endpointURI = endpointURI;
		this.testbedURI = testbedURI;
		this.jobID = jobID;
		this.scheduleName = scheduleName;
	}

	/**
	 * Getter method for jobID
	 */
	public String getJobID() {
		return jobID;
	}

	/**
	 * Setter method for jobID
	 */
	public void setJobID(String jobID) {
		this.jobID = jobID;
	}

	/**
	 * Getter method for endpointURI
	 */
	public String getEndpointURI() {
		return endpointURI;
	}

	/**
	 * Setter method for endpointURI
	 */
	public void setEndpointURI(String endpointURI) {
		this.endpointURI = endpointURI;
	}

	/**
	 * Getter method for testbedURI
	 */
	public String getTestbedURI() {
		return testbedURI;
	}

	/**
	 * Setter method for testbedURI
	 */
	public void setTestbedURI(String testbedURI) {
		this.testbedURI = testbedURI;
	}

	/**
	 * Getter method for sensorIDs
	 */
	public List<String> getSensorIDs() {
		return sensorIDs;
	}

	/**
	 * Setter method for sensorIDs
	 */
	public void setSensorIDs(List<String> sensorIDs) {
		this.sensorIDs = sensorIDs;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}


}