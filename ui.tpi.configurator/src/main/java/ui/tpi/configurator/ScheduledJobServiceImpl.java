package ui.tpi.configurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.fiestaiot.commons.util.PropertyManagement;

public class ScheduledJobServiceImpl implements ScheduledJobService {

	/**
	 * The list of scheduled jobs
	 */
	private List<ScheduledJob> scheduledJobs = new ArrayList<ScheduledJob>();

	/**
	 * The properties
	 */
	Properties properties = new Properties();

	/**
	 * The user ID
	 */
	private final String userID;

	/**
	 * The token name
	 */
	private final static String IPLANETDIRECTORYPRO = "iPlanetDirectoryPro";

	/**
	 * The authentication URL
	 */
	private String AUTHENTICATION_URL;

	/**
	 * The logger
	 */
	final static Logger logger = LoggerFactory.getLogger(ScheduledJobServiceImpl.class);

	/**
	 * The default constructor
	 * 
	 * Retrieves all scheduled jobs
	 */
	public ScheduledJobServiceImpl() {
		PropertyManagement propertyManagement = new PropertyManagement();
		String getAllScheduledJobsFromDB = propertyManagement.getGetAllScheduledJobsFromDB();
		AUTHENTICATION_URL = propertyManagement.getAuthenticationURI();

		String ssoToken = getSSOTokenFromCookie();
		userID = getUserByCookie(ssoToken);

		HttpClient client = HttpClients.createDefault();
		final HttpGet request = new HttpGet(getAllScheduledJobsFromDB + userID);
		request.addHeader("content-type", "application/json");
		request.addHeader("iPlanetDirectoryPro", ssoToken);
		final RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
				.setConnectionRequestTimeout(30000).build();
		request.setConfig(config);

		HttpResponse response;
		try {
			response = client.execute(request);
			final int sc = response.getStatusLine().getStatusCode();

			if (sc != HttpStatus.SC_OK) {
				System.out.println("Something went wrong: HTTP status " + sc);
			} else {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);
				ObjectMapper mapper = new ObjectMapper();
				List<ScheduledJob> scheduledJobsList = null;
				scheduledJobsList = mapper.readValue(body, new TypeReference<List<ScheduledJob>>() {
				});
				EditController.resourcesInUse = new ArrayList<String>();

				for (Iterator<ScheduledJob> iterator = scheduledJobsList.iterator(); iterator.hasNext(); ) {
					ScheduledJob sj = iterator.next();
					if(sj.getEndpointURI().endsWith("pushObservationsStreamProxy")){
						TestbedData.allTestbedUriMappings.put(sj.getTestbedURI(),"stopPushOfObservations");
					}
					
					int lengthOfResources = sj.getSensorIDs().size();
					sj.setEndpointURI(lengthOfResources + " resources in use");
					scheduledJobs.add(sj);
					EditController.resourcesInUse.addAll(sj.getSensorIDs());
					//logger.error("resourcesInUse" + sj.getSensorIDs()+"     sj name" + sj.getScheduleName());
				}
			}
		} catch (ClientProtocolException e1) {
			logger.error("Failed to retreive the scheduled jobs.");
			logger.error("" + e1);
		} catch (IOException e1) {
			logger.error("Failed to retreive the scheduled jobs.");
			logger.error("" + e1);
		}
	}

	/**
	 * Gets the authentication token
	 */
	public String getSSOTokenFromCookie() {
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		String token = request.getParameter(IPLANETDIRECTORYPRO);

		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase(IPLANETDIRECTORYPRO)) {
				token = cookie.getValue();
			}
		}
		return token;
	}

	/**
	 * Gets the user ID from the cookie-token
	 * @param SSOtoken
	 * 		the authentication token
	 */
	private String getUserByCookie(String SSOtoken) {
		Client client = Client.create();
		WebResource webResourceOpenAM = client.resource(AUTHENTICATION_URL);

		ClientResponse responseAuth = webResourceOpenAM.type("application/json").header(IPLANETDIRECTORYPRO, SSOtoken)
				.post(ClientResponse.class, "{}");
		if (responseAuth.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + responseAuth.getStatus());
		}
		String userObject = responseAuth.getEntity(String.class);
		ObjectMapper objectUser = new ObjectMapper();
		JsonNode userNode;
		String id = null;
		try {
			userNode = objectUser.readValue(userObject, JsonNode.class);
			id = userNode.get("id").asText();
		} catch (JsonParseException e) {
			logger.error("Failed to retreive the user ID by cookie.");
			logger.error("" + e);
		} catch (JsonMappingException e) {
			logger.error("Failed to retreive the user ID by cookie.");
			logger.error("" + e);
		} catch (IOException e) {
			logger.error("Failed to retreive the user ID by cookie.");
			logger.error("" + e);
		}

		return id;
	}

	/**
	 * Retrieve all scheduled jobs
	 * 
	 * @return the list of scheduled jobs.
	 */
	public List<ScheduledJob> findAll() {
		return scheduledJobs;
	}

	/**
	 * Adds a scheduled job
	 */
	public void addScheduledJob(ScheduledJob scheduledJob) {
		scheduledJobs.add(scheduledJob);
	}

	/**
	 * Removes a scheduled job
	 */
	public void removeScheduledJob(ScheduledJob scheduledJob) {
		scheduledJobs.remove(scheduledJob);
	}

}