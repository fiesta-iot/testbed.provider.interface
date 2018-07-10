package ui.tpi.configurator;

import java.util.Set;

public class SecurityKeysDataModel {
	
	/**
	 * The testbed's id
	 */
	private int id;
	
	/**
	 * The testbed's keys
	 */
	private String getApiKey;
	
	/**
	 * The testbed's iri
	 */
	private String iri;

	public SecurityKeysDataModel(){}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGetApiKey() {
		return getApiKey;
	}

	public void setGetApiKey(String getApiKey) {
		this.getApiKey = getApiKey;
	}

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}
}