package ui.tpi.configurator;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.util.GenericForwardComposer;



@SuppressWarnings("serial")
public class MyViewModel extends GenericForwardComposer {

	/**
	 * A counter
	 */
	private int count;

	/**
	 * The name/path of the current page
	 */
	private String currentPage;

	/**
	 * The token
	 */
	private String token;

	/**
	 * The init function for the portal
	 */
	@Init
	public void init() {
	}

	/**
	 * Listener for the count
	 */
	@Command
	@NotifyChange("count")
	public void cmd() {
		++count;
	}

	/**
	 * Getter method for the variable count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Getter method for currentPage
	 */
	public String getCurrentPage() {
		return currentPage;
	}

	/**
	 * Getter method for token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Setter method for token
	 */
	public void setToken(String token) {
		this.token = token;
	}


}
