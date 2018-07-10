package ui.tpi.configurator;

public class TestbedURIs {

	/**
	 * The testbed's services URLs
	 */
	String getObservationsURL;

	String getLastObservationsURL;

	String pushObservationsURL;

	String pushLastObservationsURL;

	/**
	 * The testbed's ID
	 */
	String id;

	/**
	 * The default constructor
	 */
	TestbedURIs() {
	}

	/**
	 * Getter method for id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Getter method for getObservationsURL
	 */
	public String getGetObservationsURL() {
		return getObservationsURL;
	}

	/**
	 * Setter method for getObservationsURL
	 */
	public void setGetObservationsURL(String getObservationsURL) {
		this.getObservationsURL = getObservationsURL;
	}

	/**
	 * Getter method for getLastObservationsURL
	 */
	public String getGetLastObservationsURL() {
		return getLastObservationsURL;
	}

	/**
	 * Setter method for getLastObservationsURL
	 */
	public void setGetLastObservationsURL(String getLastObservationsURL) {
		this.getLastObservationsURL = getLastObservationsURL;
	}

	/**
	 * Getter method for pushObservationsURL
	 */
	public String getPushObservationsURL() {
		return pushObservationsURL;
	}

	/**
	 * Setter method for pushObservationsURL
	 */
	public void setPushObservationsURL(String pushObservationsURL) {
		this.pushObservationsURL = pushObservationsURL;
	}

	/**
	 * Getter method for pushLastObservationsURL
	 */
	public String getPushLastObservationsURL() {
		return pushLastObservationsURL;
	}

	/**
	 * Setter method for pushLastObservationsURL
	 */
	public void setPushLastObservationsURL(String pushLastObservationsURL) {
		this.pushLastObservationsURL = pushLastObservationsURL;
	}
}
