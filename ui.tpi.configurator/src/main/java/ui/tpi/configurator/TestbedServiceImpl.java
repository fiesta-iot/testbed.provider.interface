package ui.tpi.configurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.fiestaiot.commons.util.PropertyManagement;

public class TestbedServiceImpl implements TestbedService {
	/**
	 * The property management instance
	 */
	PropertyManagement propertyManagement = new PropertyManagement();

	/**
	 * A list of testbeds
	 */
	private List<Testbed> testbeds = new ArrayList<Testbed>();

	/**
	 * The name of the cookie
	 */
	private final static String IPLANETDIRECTORYPRO = "iPlanetDirectoryPro";

	/**
	 * The endpoint used to get user authentication
	 */
	private static String AUTHENTICATION_URL;

	/**
	 * The authentication token
	 */
	public String token;

	/**
	 * The ids of the testbeds
	 */
	public List<List<String>> idsOfTestbeds;

	/**
	 * The user ID
	 */
	String fiestaUserID;

	/**
	 * The logger
	 */
	final static Logger logger = LoggerFactory.getLogger(TestbedServiceImpl.class);

	/**
	 * Default constructor
	 */
	public TestbedServiceImpl() {
		fiestaUserID = getFiestaUserIDFromCookie();
		idsOfTestbeds = TestbedData.getTestbeds(fiestaUserID, token);
		List<String> testbedIDs = idsOfTestbeds.get(0); // contains the realIDs
		List<String> availTestbeds = idsOfTestbeds.get(1); // contains the names
		List<String> iris = idsOfTestbeds.get(2);// contains the IRIs
		
		for (int i = 0; i < availTestbeds.size(); i++) {
			//if (i == 0) {
				Testbed testbed = new Testbed();
				testbed.setTestbedName(iris.get(i));
				testbed.setId(testbedIDs.get(i));

				List<String> resources = new ArrayList<String>(TestbedData.getAvailResources(iris.get(i), token));

				List<String> securityKeys = new ArrayList<String>(
						TestbedData.getSecurityKeys(testbedIDs.get(i), fiestaUserID, token));
				if (securityKeys.size() == 0)
					securityKeys.add("none");
				else
					securityKeys.add("none");

				List<String> testbedURIs2 = TestbedData.getTestbedURIs(testbedIDs.get(i), token);
				List<String> testbedURIs = new ArrayList<String>(testbedURIs2);

				if (testbedURIs.size() == 0)
					testbedURIs.add("none");

				testbed.setTestbedURIs(new HashSet<String>(testbedURIs));
				testbed.setResources(new HashSet<String>(resources));

				testbed.setSecurityKeys(new HashSet<String>(securityKeys));
				testbeds.add(testbed);
			//}
		}				
	}

	/**
	 * Returns all IDs of all available testbeds
	 */
	public List<List<String>> getIdsOfTestbeds() {
		return idsOfTestbeds;
	}

	/**
	 * Gets all necessary information about the testbeds, except from the first,
	 * for a specific user ID
	 * 
	 * @param fiestaUserID
	 *            the user ID
	 */
	public void getAdditionalInformation(String fiestaUserID) {
		// do this for the sets except from the first

		List<String> testbedIDs = idsOfTestbeds.get(0); // contains the realIDs
		List<String> availTestbeds = idsOfTestbeds.get(1); // contains the names
		List<String> iris = idsOfTestbeds.get(2);// contains the IRIs

		for (int i = 0; i < availTestbeds.size(); i++) {
			if (i != 0) {
				Testbed testbed = new Testbed();
				testbed.setTestbedName(iris.get(i));
				testbed.setId(testbedIDs.get(i));

				List<String> securityKeys = new ArrayList<String>(
						TestbedData.getSecurityKeys(testbedIDs.get(i), fiestaUserID, token));
				if (securityKeys.size() == 0)
					securityKeys.add("none");
				else
					securityKeys.add("none");

				List<String> testbedURIs2 = TestbedData.getTestbedURIs(testbedIDs.get(i), token);
				List<String> testbedURIs = new ArrayList<String>(testbedURIs2);

				if (testbedURIs.size() == 0)
					testbedURIs.add("none");

				testbed.setTestbedURIs(new HashSet<String>(testbedURIs));

				testbed.setSecurityKeys(new HashSet<String>(securityKeys));
				testbeds.add(testbed);
			}
		}

	}

	/**
	 * Gets the user ID from the Cookie
	 * 
	 * @return the fiesta username
	 */
	public String getFiestaUserIDFromCookie() {
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		token = request.getParameter(IPLANETDIRECTORYPRO);
		String userID = null;
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase(IPLANETDIRECTORYPRO)) {
				token = cookie.getValue();
			}
		}

		AUTHENTICATION_URL = propertyManagement.getAuthenticationURI();
		Client client = Client.create();
		WebResource webResourceOpenAM = client.resource(AUTHENTICATION_URL);

		ClientResponse responseAuth = webResourceOpenAM.type("application/json").header(IPLANETDIRECTORYPRO, token)
				.post(ClientResponse.class, "{}");
		if (responseAuth.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + responseAuth.getStatus());
		}
		String userObject = responseAuth.getEntity(String.class);
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;
		try {
			map = mapper.readValue(userObject, new TypeReference<HashMap<String, Object>>() {
			});
			userID = (String) map.get("id");
		} catch (JsonParseException e) {
			logger.error("Could not get user ID from cookie. ");
			logger.error("" + e);
		} catch (JsonMappingException e) {
			logger.error("Could not get user ID from cookie. ");
			logger.error("" + e);
		} catch (IOException e) {
			logger.error("Could not get user ID from cookie. ");
			logger.error("" + e);
		}

		return userID;

	}

	/**
	 * Retrieve all testbeds
	 * 
	 * @return the list containing all testbeds
	 */
	public List<Testbed> findAll() {
		return testbeds;
	}

	/**
	 * Adds a resource to the testbed's resources
	 */
	public void addResource(String resource) {
	}
}