package ui.tpi.configurator;

public class User {

	/**
	 * The firstname of the user
	 */
	private String firstname;

	/**
	 * The lastname of the user
	 */
	private String lastname;

	/**
	 * The id of the user
	 */
	private String id;

	/**
	 * The default constructor
	 */
	public User() {
	}

	/**
	 * The constructor
	 * 
	 * @param firstname The first name of the user
	 * 
	 * @param lastname The first name of the user
	 * 
	 * @param id The id of the user
	 */
	public User(String firstname, String lastname, String id) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.setId(id);
	}

	/**
	 * Getter method for lastname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * Getter method for lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * Getter method for id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Getter method for id
	 */
	public void setId(String id) {
		this.id = id;
	}

}