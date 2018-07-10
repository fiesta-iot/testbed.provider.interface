package ui.tpi.configurator;

public class TestbedIDs {

	/**
	 * The registered ID of the Testbed
	 */
	String registerID;

	/**
	 * The ID of the Testbed
	 */
	String id;

	/**
	 * The IRI of the Testbed
	 */
	String iri;

	/**
	 * The default constructor
	 */
	TestbedIDs() {
	}

	/**
	 * Getter method for the iri
	 */
	public String getIri() {
		return iri;
	}

	/**
	 * Setter method for the iri
	 */
	public void setIri(String iri) {
		this.iri = iri;
	}

	/**
	 * Setter method for the registerID
	 */
	public void setRealID(String registerID) {
		this.registerID = registerID;
	}

	/**
	 * Getter method for the registerID
	 */
	public String getRegisterID() {
		return registerID;
	}

	/**
	 * Setter method for the id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Getter method for the id
	 */
	public String getId() {
		return id;
	}

}
