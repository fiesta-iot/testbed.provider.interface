package ui.tpi.configurator;

import java.util.HashMap;
import java.util.Set;

public class Testbed {
	
	/**
	 * The testbed's name
	 */
	private String testbedName;
	
	/**
	 * The list of the testbed's resources
	 */
	private Set<String> resources;
	
	/**
	 * The list of the testbed's security keys
	 */
	private Set<String> securityKeys;
	
	/**
	 * The list of the testbed's testbedURIs
	 */
	private Set<String> testbedURIs;
	
	/**
	 * The testbed's id
	 */
	private String id;
	
	/**
	 * The default constructor
	 */
	public Testbed() {
	}

	/**
	 * Getter method for testbedName
	 */
	public String getTestbedName() {
		return testbedName;
	}

	/**
	 * Setter method for testbedName
	 */
	public void setTestbedName(String testbedName) {
		this.testbedName = testbedName;
	}

	/**
	 * Getter method for resources
	 */
	public Set<String> getResources() {
		return resources;
	}
	
	/**
	 * Getter method for testbedURIs
	 */
	public Set<String> getTestbedURIs() {
		return testbedURIs;
	}
	
	/**
	 * Getter method for security keys
	 */
	public Set<String> getSecurityKeys() {
		return securityKeys;
	}

	/**
	 * Setter method for resources
	 */
	public void setResources(Set<String> resources) {
		this.resources = resources;
	}
	
	/**
	 * Setter method for testbedURIs
	 */
	public void setTestbedURIs(Set<String> testbedURIs) {
		this.testbedURIs = testbedURIs;
	}
	
	/**
	 * Setter method for security keys
	 */
	public void setSecurityKeys(Set<String> securityKeys) {
		this.securityKeys = securityKeys;
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
}