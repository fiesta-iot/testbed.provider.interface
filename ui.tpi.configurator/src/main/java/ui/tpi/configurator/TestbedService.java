package ui.tpi.configurator;

import java.util.List;

public interface TestbedService {

	/**
	 * Retrieve all testbeds
	 * 
	 * @return 
	 * 		the list containing all testbeds
	 */
	public List<Testbed> findAll();
	

	/**
	 * Adds a resource to the testbed's
	 * resources
	 */
	public void addResource(String resource);
	
	/**
	 * Retrieve all ids of all testbeds
	 * 
	 * @return 
	 * 		the list containing lists of 
	 * 		all ids of all testbeds
	 */
	public List<List<String>> getIdsOfTestbeds();
	
	
	/**
	 * Returns all IDs of all available testbeds
	 */
	public void getAdditionalInformation(String fiestaUserID);
}
