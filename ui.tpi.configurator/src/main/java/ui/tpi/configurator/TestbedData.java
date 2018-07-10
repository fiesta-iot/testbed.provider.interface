package ui.tpi.configurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.fiestaiot.commons.util.PropertyManagement;

public class TestbedData {

	/**
	 * The selected testbed
	 */
	static String selectedTestbed;

	/**
	 * The URL to retrieve the testbeds
	 */
	static String TESTBED_INFO_RETRIEVE;

	/**
	 * The URL to retrieve the testbed information by user id
	 */
	static String TESTBED_BY_USER_ID;

	/**
	 * The string to search for (security keys)
	 */
	static String securityKeyJsonObj = "getApiKey";

	/**
	 * The URL to retrieve the available testbeds
	 */
	static String AVAILABLE_TESTBEDS_ENDPOINT;

	/**
	 * The URL to retrieve the testbeds
	 */
	static String TESTBEDS_RETRIEVE;

	/**
	 * The URL to retrieve the available resources
	 */
	static String RESOURCES_RETRIEVE;

	/**
	 * The string to search for (Testbed's endpoints)
	 */
	static String testbedEndpointsJsonObj = "Testbed Endpoints";

	/**
	 * The property management instance
	 */
	static PropertyManagement propertyManagement = new PropertyManagement();

	/**
	 * List of all testbed uri mappings
	 */
	static HashMap<String, String> allTestbedUriMappings = new HashMap<>(); 
	
	/**
	 * The logger
	 */
	final static Logger logger = LoggerFactory.getLogger(TestbedData.class);

	/**
	 * Get the testbed names (fiesta ids) and their real ids
	 * 
	 * @param userID
	 *            the user id
	 * @param SSOtoken
	 *            the token
	 * @return A List including three lists List 1: includes the real IDs of the
	 *         testbeds List 2: includes the names of the testbeds List 3:
	 *         includes the IRIs of the testbeds
	 */
	public static List<List<String>> getTestbeds(String userID, String SSOtoken) {
		AVAILABLE_TESTBEDS_ENDPOINT = propertyManagement.getAvailableTestbedsEndpointURI();

		List<String> realIDs = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		List<String> iris = new ArrayList<String>();

		HttpResponse response = null;
		try {
			HttpClient client = HttpClients.createDefault();
			final HttpPost request = new HttpPost(AVAILABLE_TESTBEDS_ENDPOINT);
			request.addHeader("iPlanetDirectoryPro", SSOtoken);

			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("userID", userID);
			List<String> expectedFieldsAsResult = new ArrayList<String>();
			expectedFieldsAsResult.add("registerID");
			expectedFieldsAsResult.add("iri");
			payload.put("expectedFieldsAsResult", expectedFieldsAsResult);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String params = new String(objectMapper.writeValueAsString(payload));
			request.addHeader("content-type", "application/json");
			request.setEntity(new StringEntity(params));
			RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
					.setConnectionRequestTimeout(30000).build();
			request.setConfig(config);
			response = client.execute(request);

		} catch (ClientProtocolException e1) {
			logger.error("Could not retreive the available testbeds. ");
			logger.error("" + e1);
		} catch (IOException e1) {
			logger.error("Could not retreive the available testbeds. ");
			logger.error("" + e1);
		}
		final int sc = response.getStatusLine().getStatusCode();

		if (sc != HttpStatus.SC_OK) {
			logger.error("Could not retreive the available testbeds. HTTP status [ " + sc + " ]");
			logger.error("" + sc);
		} else {
			try {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);

				ObjectMapper objectMapper = new ObjectMapper();
				List<TestbedIDs> listOfTestbedIds = objectMapper.readValue(body,
						objectMapper.getTypeFactory().constructCollectionType(List.class, TestbedIDs.class));

				for (TestbedIDs ti : listOfTestbedIds) {
					realIDs.add(ti.getId());
					names.add(ti.getRegisterID());
					iris.add(ti.getIri());
				}

			} catch (JsonParseException e) {
				logger.error("Could not retreive the available testbeds. ");
				logger.error("" + e);
			} catch (JsonMappingException e) {
				logger.error("Could not retreive the available testbeds. ");
				logger.error("" + e);
			} catch (IOException e) {
				logger.error("Could not retreive the available testbeds. ");
				logger.error("" + e);
			}
		}
		List<List<String>> pair = new ArrayList<List<String>>();
		pair.add(names);
		pair.add(realIDs);
		pair.add(iris);
		logger.info("Successfully retreived the available testbeds. ");
		return pair;
	}

	/**
	 * Get the testbed's available resources
	 * 
	 * @param testbed
	 *            the testbeds id (fiesta id)
	 * @param SSOtoken
	 *            the token
	 * @return the list of the available resources for the specific testbed
	 */
	public static List<String> getAvailResources(String testbed, String SSOtoken) {
		TESTBEDS_RETRIEVE = propertyManagement.getTestbedsRetrieveURI();
		RESOURCES_RETRIEVE = propertyManagement.getResourcesRetrieveURI();

		String id = null;

		HttpResponse response = null;
		try {
			HttpClient client = HttpClients.createDefault();
			final HttpPost request = new HttpPost(TESTBEDS_RETRIEVE);
			Map<String, Object> payload = new HashMap<String, Object>();
			List<String> IRIList = new ArrayList<String>();
			IRIList.add(testbed);
			payload.put("", IRIList);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String params = new String(objectMapper.writeValueAsString(IRIList));
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "application/json");
			request.addHeader("iPlanetDirectoryPro", SSOtoken);
			request.setEntity(new StringEntity(params));
			RequestConfig config = RequestConfig.custom().setSocketTimeout(100000).setConnectTimeout(100000)
					.setConnectionRequestTimeout(100000).build();
			request.setConfig(config);
			response = client.execute(request);

		} catch (ClientProtocolException e1) {
			logger.error("Could not retreive the available resources. ");
			logger.error("" + e1);
		} catch (IOException e1) {
			logger.error("Could not retreive the available resources. ");
			logger.error("" + e1);
		}
		final int sc = response.getStatusLine().getStatusCode();
		if (sc != HttpStatus.SC_OK) {
		} else {
			try {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);

				ObjectMapper objectMapper = new ObjectMapper();

				List<String> tst = objectMapper.readValue(body, new TypeReference<List<String>>() {
				});
				id = tst.get(0);
			} catch (JsonParseException e) {
				logger.error("Could not retreive the available resources. ");
				logger.error("" + e);
			} catch (JsonMappingException e) {
				logger.error("Could not retreive the available resources. ");
				logger.error("" + e);
			} catch (IOException e) {
				logger.error("Could not retreive the available resources. ");
				logger.error("" + e);
			}
		}

		List<String> availResources = new ArrayList<String>();
		List<String> resourceIDs = new ArrayList<String>();

		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(RESOURCES_RETRIEVE + id + "/resources");
		request.addHeader("content-type", "application/json");
		request.addHeader("Accept", "application/json");
		request.addHeader("iPlanetDirectoryPro", SSOtoken);

		final RequestConfig config = RequestConfig.custom().setSocketTimeout(100000).setConnectTimeout(100000)
				.setConnectionRequestTimeout(100000).build();
		request.setConfig(config);
		HttpResponse response1;

		try {
			response1 = client.execute(request);

			final int sc1 = response1.getStatusLine().getStatusCode();

			if (sc1 != HttpStatus.SC_OK) {
				System.out.println("Could not retreive the available resources. HTTP status [ " + sc1 + " ]");
				logger.error("Could not retreive the available resources. HTTP status [ " + sc1 + " ]");
			} else {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response1);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode node = objectMapper.readValue(body, JsonNode.class);
				JsonNode array = node.get("resources");

				for (JsonNode jn : array) {
					resourceIDs.add(jn.asText());
				}
			}
		} catch (ClientProtocolException e1) {
			logger.error("Could not retreive the available resources. ");
			logger.error("" + e1);
		} catch (IOException e1) {
			logger.error("Could not retreive the available resources. ");
			logger.error("" + e1);
		}

		for (String resourceID : resourceIDs) {
			if (!EditController.resourcesInUse.contains(resourceID)) {
				availResources.add(resourceID);
			} else {
			}
		}

		return availResources;
	}

	/**
	 * Get the testbed's available security keys
	 * 
	 * @param testbed
	 *            the testbeds id (fiesta id)
	 * @param SSOtoken
	 *            the token
	 * @return the list of the available security keys for the specific testbed
	 */
	public static List<String> getSecurityKeys(String testbed, String userID, String SSOtoken) {
		TESTBED_INFO_RETRIEVE = propertyManagement.getTestbedInfoRetrieveURI();
		TESTBED_BY_USER_ID = propertyManagement.getAvailableTestbedsEndpointURI();

		HttpResponse response = null;
		try {
			HttpClient client = HttpClients.createDefault();
			final HttpPost request = new HttpPost(TESTBED_BY_USER_ID);
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("userID", userID);
			List<String> expectedFieldsAsResult = new ArrayList<String>();
			expectedFieldsAsResult.add("id");
			expectedFieldsAsResult.add("iri");
			expectedFieldsAsResult.add(securityKeyJsonObj);
			payload.put("expectedFieldsAsResult", expectedFieldsAsResult);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String params = new String(objectMapper.writeValueAsString(payload));
			request.addHeader("content-type", "application/json");
			request.addHeader("iPlanetDirectoryPro", SSOtoken);

			request.setEntity(new StringEntity(params));
			RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
					.setConnectionRequestTimeout(30000).build();
			request.setConfig(config);
			response = client.execute(request);
		} catch (ClientProtocolException e1) {
			logger.error("Could not retreive the available security keys. ");
			logger.error("" + e1);
		} catch (IOException e1) {
			logger.error("Could not retreive the available security keys. ");
			logger.error("" + e1);
		}

		final int sc = response.getStatusLine().getStatusCode();
		String securityKey = null;
		List<String> securityKeys = new ArrayList<String>();

		if (sc != HttpStatus.SC_OK) {
		} else {

			try {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);
				ObjectMapper mapper = new ObjectMapper();				
			
					ArrayList<SecurityKeysDataModel> skd = mapper.readValue(body, new TypeReference<List<SecurityKeysDataModel>>(){});
					
					for(SecurityKeysDataModel securityKeyData : skd){
						String iriOfObj = securityKeyData.getIri();
						if (iriOfObj.compareToIgnoreCase(testbed) == 0) {
							
							if(securityKeyData.getGetApiKey() != null){
							
								String securityKeyOfObj = securityKeyData.getGetApiKey();
								
								if (securityKeyOfObj != null) {
									if (securityKeyOfObj.contains("Basic ")) {
										securityKeys.add(securityKeyOfObj.split("Basic ")[1]);
									} else {
										securityKeys.add(securityKeyOfObj);
									}
								}
							}
							break;
						}
					}
			} catch (JsonParseException e) {
				logger.error("Could not retreive the available security keys. ");
				logger.error("" + e);
			} catch (JsonMappingException e) {
				logger.error("Could not retreive the available security keys. ");
				logger.error("" + e);
			} catch (IOException e) {
				logger.error("Could not retreive the available security keys. ");
				logger.error("" + e);
			}
		}

		return securityKeys;
	}

	/**
	 * Get the testbed's available testbedURIs
	 * 
	 * @param testbed
	 *            the testbeds id (fiesta id)
	 * @param SSOtoken
	 *            the token
	 * @return the list of the available testbed URIs for the specific testbed
	 */
	public static List<String> getTestbedURIs(String testbed, String SSOtoken) {
		TESTBED_INFO_RETRIEVE = propertyManagement.getTestbedInfoRetrieveURI();
		//testbedUriMapping = new HashMap<>();
		
		List<String> testbedURIs = new ArrayList<String>();
		HttpResponse response = null;
		try {
			HttpClient client = HttpClients.createDefault();
			final HttpPost request = new HttpPost(TESTBED_INFO_RETRIEVE);
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("registerID", testbed);
			List<String> expectedFieldsAsResult = new ArrayList<String>();
			expectedFieldsAsResult.add("getObservationsURL");
			expectedFieldsAsResult.add("getLastObservationsURL");
			expectedFieldsAsResult.add("pushObservationsURL");
			expectedFieldsAsResult.add("pushLastObservationsURL");
			payload.put("expectedFieldsAsResult", expectedFieldsAsResult);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String params = new String(objectMapper.writeValueAsString(payload));

			request.addHeader("content-type", "application/json");
			request.addHeader("iPlanetDirectoryPro", SSOtoken);

			request.setEntity(new StringEntity(params));
			RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
					.setConnectionRequestTimeout(30000).build();
			request.setConfig(config);
			response = client.execute(request);
		} catch (ClientProtocolException e1) {
			logger.error("Could not retreive the available testbed URIs. ");
			logger.error("" + e1);
		} catch (IOException e1) {
			logger.error("Could not retreive the available testbed URIs. ");
			logger.error("" + e1);
		}
		final int sc = response.getStatusLine().getStatusCode();

		if (sc != HttpStatus.SC_OK) {
			System.out.println("Something went wrong: HTTP status " + sc);
		} else {
			try {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);

				ObjectMapper objectMapper = new ObjectMapper();
				TestbedURIs testbedURLs = objectMapper.readValue(body, TestbedURIs.class);

				if (testbedURLs.getLastObservationsURL != null){
					testbedURIs.add(testbedURLs.getLastObservationsURL);
					allTestbedUriMappings.put(testbedURLs.getLastObservationsURL, "getLastObservations");
				}
				if (testbedURLs.getObservationsURL != null){
					testbedURIs.add(testbedURLs.getObservationsURL);
					allTestbedUriMappings.put(testbedURLs.getObservationsURL,"getObservations");
				}
				if (testbedURLs.pushLastObservationsURL != null){
					testbedURIs.add(testbedURLs.pushLastObservationsURL);
					allTestbedUriMappings.put(testbedURLs.pushLastObservationsURL, "pushLastObservations");
				}
				if (testbedURLs.pushObservationsURL != null){
					testbedURIs.add(testbedURLs.pushObservationsURL);
					allTestbedUriMappings.put(testbedURLs.pushObservationsURL, "pushObservations");
				}

			} catch (JsonParseException e) {
				logger.error("Could not retreive the available testbed URIs. ");
				logger.error("" + e);
			} catch (JsonMappingException e) {
				logger.error("Could not retreive the available testbed URIs. ");
				logger.error("" + e);
			} catch (IOException e) {
				logger.error("Could not retreive the available testbed URIs. ");
				logger.error("" + e);
			}
		}
		
		return testbedURIs;
	}
	
	/**
	 * Getter for the selectedTestbed
	 */
	public static String getSelectedTestbed() {
		return selectedTestbed;
	}

	/**
	 * Setter for the selectedTestbed
	 */
	public static void setSelectedTestbed(String selectedTestbed) {
		TestbedData.selectedTestbed = selectedTestbed;
	}

	// Used just to initialize the resources
	public static List<String> getResources() {
		return Arrays.asList(new String[] { "", "" });
	}

	// Used just to initialize the security keys
	public static List<String> getSecurityKeys() {
		return Arrays.asList(new String[] { "", "" });
	}

	// Used just to initialize the testbedURIs
	public static List<String> getTestbedURIs() {
		return Arrays.asList(new String[] { "", "" });
	}
	
	public static void initializeAllTestbedUriMappings(){
		allTestbedUriMappings = new HashMap<>(); 
	}
	
}
