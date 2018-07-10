package eu.fiestaiot.tpi.api.dms.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.fiestaiot.commons.util.PropertyManagement;
import eu.fiestaiot.tpi.api.dms.impl.dataservices.SubscribeToObservationImpl;
import eu.fiestaiot.tpi.api.dms.impl.dataservices.SubscribeToObservationStreamImpl;
import eu.fiestaiot.tpi.api.dms.impl.dataservices.SubscribeToObservationStreamWithTopicImpl;
import eu.fiestaiot.tpi.api.dms.service.DatabaseDMS;
import eu.fiestaiot.tpi.api.dms.service.JobScheduler;
import eu.fiestaiot.tpi.api.dms.service.MessageBus;
import eu.fiestaiot.tpi.api.dms.service.TimeSchedule;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * @author Nikos Kefalakis (nkef) e-mail: nkef@ait.edu.gr
 * 
 */
@Path("/dataservices")
// @Consumes({ "application/xml", "application/json" })
// @Produces({ "application/xml", "application/json" })
public class TpiApiDataServicesRsControler {

	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(TpiApiDataServicesRsControler.class);

	/**
	 * The username of the database.
	 */
	String usernameDB;

	/**
	 * The password of the database.
	 */
	String passwordDB;

	/**
	 * The URL of OpenAM.
	 */
	private String USER_INFO_OPENAM;

	/**
	 * The token and cookies name.
	 */
	private final String IPLANETDIRECTORYPRO = "iplanetDirectoryPro";

	/**
	 * The porperty manager's initialization.
	 */
	PropertyManagement propertyManagement = new PropertyManagement();

	/**
	 * Displays the welcome text under tpi.api.dms/rest/dataservices
	 * 
	 * @return the text message that is shown in tpi.api.dms/rest/dataservices
	 * 
	 **/
	@GET()
	@Produces("text/plain")
	public String welcomeMessage() {

		String welcomeText = "Welcome to Testbed Provider Interface Data Services\n"
				+ "======================================================\n\n";
		logger.debug(welcomeText);
		return welcomeText;
	}

	/**
	 * Stop pushing Observations.
	 * 
	 * @param query
	 *            the POST payload that includes the following parameters
	 *            sensorIDs: the list of sensor IDs testbedURI: the URI of the
	 *            testbed
	 *
	 *            The query should look like the following { "sensorIDs": [
	 *            <String> <sensor_id_1>, <String> <sensor_id_2>, ..],
	 *            "testbedURI": <String> <url_of_the_testbed> }
	 *
	 * @return the response
	 * 
	 **/
	@POST
	@SuppressWarnings("unchecked")
	@Path("/stopPushOfObservations")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response stopPushOfObservations(String stopPushOfObservationsPayload) {
		logger.debug("Stop pushing observations.");
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;
		try {
			map = mapper.readValue(stopPushOfObservationsPayload, new TypeReference<HashMap<String, Object>>() {
			});
			final List<String> sensorIDs = (List<String>) map.get("sensorIDs");
			final String testbedURI = (String) map.get("testbedURI");
			final String endpointURI = (String) map.get("endpointURI");
			final String jobID = (String) map.get("jobID");

			if (sensorIDs == null || sensorIDs.isEmpty()) {
				logger.error("Failed to stop the pushing of observations. No sensor IDs found.");
				return Response.status(420).type(MediaType.APPLICATION_JSON)
						.entity("{\"response\" : \"[ERROR] Failed to stop the pushing of observations. No sensor IDs found.\"}")
						.build();
			}

			if (testbedURI == null || testbedURI.compareToIgnoreCase("") == 0) {
				logger.error("Failed to stop the pushing of observations. No testbed URI found.");
				return Response.status(420).type(MediaType.APPLICATION_JSON)
						.entity("{\"response\" : \"[ERROR] Failed to stop the pushing of observations. No testbed URI found.\"}")
						.build();
			}

			if (endpointURI == null || endpointURI.compareToIgnoreCase("") == 0) {
				logger.error("Failed to stop the pushing of observations. No endpoint URI found.");
				return Response.status(420).type(MediaType.APPLICATION_JSON)
						.entity("{\"response\" : \"[ERROR] Failed to stop the pushing of observations. No endpoint URI found.\"}")
						.build();
			}

			if (jobID == null || jobID.compareToIgnoreCase("") == 0) {
				logger.error("Failed to stop the pushing of observations. No job ID found.");
				return Response.status(420).type(MediaType.APPLICATION_JSON)
						.entity("{\"response\" : \"[ERROR] Failed to stop the pushing of observations. No job ID found.\"}")
						.build();
			}

			// TODO: check the functionlity of this method
			HttpClient client = HttpClients.createDefault();
			final HttpPost request = new HttpPost(testbedURI);
			Map<String, Object> payload = new HashMap<String, Object>();

			payload.put("sensorIDs", sensorIDs);
			payload.put("endpointURI", endpointURI);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String params;

			try {
				params = new String(objectMapper.writeValueAsString(payload));
				request.addHeader("content-type", "application/json");
				// request1.addHeader("iPlanetDirectoryPro", ssoToken);
				request.setEntity(new StringEntity(params));
				final RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
						.setConnectionRequestTimeout(30000).build();
				request.setConfig(config);

				final HttpResponse response = client.execute(request);
				final int sc = response.getStatusLine().getStatusCode();

				if (sc != HttpStatus.SC_OK) {
					logger.error("Failed to schedule the task [ status code: " + sc + " ].");
					return Response.status(420).build();
				} else {
					int statusResponse = DatabaseDMS.deleteRow(jobID);

					if (statusResponse != 200) {
						return Response.status(statusResponse).build();
					}

					return Response.ok(
							"{\"response\" : \"Job sucessfully deleted.\" , \n \"jobID\" : \"" + jobID
									+ "\", \n \"stopPushOfObservations\" : \"" + testbedURI + "\"}",
							MediaType.APPLICATION_JSON).build();
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				return Response.status(420).build();
			} catch (JsonProcessingException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				return Response.status(420).build();
			} catch (ClientProtocolException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				return Response.status(420).build();
			} catch (IOException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				return Response.status(420).build();
			}
		} catch (JsonParseException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (JsonMappingException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (IOException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		}

		return null;
	}

	/**
	 * 
	 * @param query
	 *            the POST payload that includes the following parameters
	 *            sensorIDs: the list of sensor IDs testbedURI: the URI of the
	 *            testbed timeSchedule: the time schedule
	 *
	 *            The query should look like the following { "sensorIDs": [
	 *            <String> <sensor_id_1>, <String> <sensor_id_2>, ..],
	 *            "testbedURI": <url_of_the_testbed>, "securityKey":
	 *            <security_key_of_the_schedule>, "ssoToken": <the_sso_token>,
	 *            "timeSchedule": { "startTime" : <String> <start_date>,
	 *            "frequency" : <int> <the_frequency>, "timeUnit" :
	 *            <String> <the_time_unit> }, "endpointURI":
	 *            <String> <url_of_the_endpoint> }
	 *
	 *            The date should be a future date and should have the following
	 *            format: yyyy-MM-dd'T'HH:mm:ss e.g. 2016-04-15T11:40:00
	 *
	 *            Valid time units are: second, minute, hour, day, month
	 * 
	 *            The timeSchedule referes to a job that schould be triggered
	 *            and start at <start_date>, and should be repeated every
	 *            <the_frequency> <the_time_unit> (e.g. every 2 seconds).
	 * 
	 * @return the results
	 * 
	 **/
	@SuppressWarnings("unchecked")
	@POST
	@Path("/subscribeToObservations")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response subscribeToObservation(String subscribeToObservationsPayload) {
		logger.debug("Subscribe to Observation.");

		try {
			final ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(subscribeToObservationsPayload,
					new TypeReference<HashMap<String, Object>>() {
					});
			final List<String> sensorIDs = (List<String>) map.get("sensorIDs");
			logger.debug("sensorIDs " + sensorIDs);

			final String testbedURI = (String) map.get("testbedURI");
			logger.debug("testbedURI " + testbedURI);

			final String endpointURI = (String) map.get("endpointURI");
			logger.debug("endpointURI " + endpointURI);

			String securityKey = (String) map.get("securityKey");
			logger.debug("securityKey " + securityKey);

			final String userID = (String) map.get("userID");
			logger.debug("userID " + userID);

			final String scheduleName = (String) map.get("scheduleName");
			logger.debug("scheduleName " + scheduleName);

			JsonNode node = mapper.readValue(subscribeToObservationsPayload, JsonNode.class);
			// if(node.has("timeSchedule")){
			JsonNode ts = node.get("timeSchedule");

			int frequency = ts.get("frequency").asInt();
			logger.debug("frequency " + frequency);

			String timeUnit = ts.get("timeUnit").asText();
			DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			Date startTime;
			if (ts.has("startTime")) {
				String dateTmp = ts.get("startTime").asText().toString();
				if (!dateTmp.isEmpty()) {
					startTime = DATE_FORMAT.parse(dateTmp);
					// TODO: add functionality for past time
				} else {
					startTime = DATE_FORMAT.parse(DATE_FORMAT.format(new Date()));
				}
			} else {
				startTime = DATE_FORMAT.parse(DATE_FORMAT.format(new Date()));
			}

			if (userID == null) {
				logger.error("No valid user id found.");
				return Response.status(420).build();
			}

			String id = "" + DatabaseDMS.insertRow(userID, sensorIDs, testbedURI, endpointURI, startTime.toString(),
					frequency, timeUnit, securityKey, scheduleName);
			if (id.compareToIgnoreCase("0") == 0) {
				return Response.status(420).build();
			}

			Date stopTime;
			TimeSchedule timeSchedule;
			if (ts.has("stopTime")) {
				int repeatCount = ts.get("repeatCount").asInt();
				stopTime = DATE_FORMAT.parse(ts.get("stopTime").asText().toString());
				timeSchedule = new TimeSchedule(startTime, stopTime, frequency, repeatCount, timeUnit, id, testbedURI);
			} else {
				timeSchedule = new TimeSchedule(startTime, frequency, timeUnit, id, testbedURI);
			}

			SubscribeToObservationImpl subscribeToObservationImpl = new SubscribeToObservationImpl(sensorIDs,
					testbedURI, timeSchedule, securityKey);
			return subscribeToObservationImpl.subscribeToObservation();
			// }
			// else{
			// SubscribeToObservationImpl subscribeToObservationImpl = new
			// SubscribeToObservationImpl(sensorIDs, testbedURI);
			// return subscribeToObservationImpl.subscribeToObservation();
			// }
		} catch (IOException | ParseException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		}
		return Response.status(420).build();
	}

	/**
	 * Gets the user ID from the cookie-token
	 */
	public String getUserIDByToken(String ssoToken) {
		USER_INFO_OPENAM = propertyManagement.getAuthenticationURI();
		Client client = Client.create();
		WebResource webResourceOpenAM = client.resource(USER_INFO_OPENAM);

		ClientResponse responseAuth = webResourceOpenAM.type("application/json").header(IPLANETDIRECTORYPRO, ssoToken)
				.post(ClientResponse.class, "{}");
		if (responseAuth.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + responseAuth.getStatus());
		}
		String userObject = responseAuth.getEntity(String.class);
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;
		String userID = null;
		try {
			map = mapper.readValue(userObject, new TypeReference<HashMap<String, Object>>() {
			});
			userID = (String) map.get("id");
		} catch (JsonParseException e) {
			logger.error("[ERROR]: Failed to get user ID. " + e);
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error("[ERROR]: Failed to get user ID.  " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("[ERROR]: Failed to get user ID.  " + e);
			e.printStackTrace();
		}

		return userID;
	}

	/**
	 * 
	 * Subscribe to Observation Stream.
	 * 
	 * @param query
	 *            the POST payload that includes the following parameters
	 *            sensorIDs: the list of sensor IDs testbedURI: the URI of the
	 *            testbed
	 *
	 *            The query should look like the following { "sensorIDs": [
	 *            <String> <sensor_id_1>, <String> <sensor_id_2>, ..],
	 *            "endpointURI": <String> theEndpointURI, "testbedURI":
	 *            <String> <url_of_the_testbed> }
	 *
	 * @return the response
	 * 
	 **/
	@POST
	@SuppressWarnings("unchecked")
	@Path("/subscribeToObservationStream")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response subscribeToObservationStream(String subscribeToObservationStreamPayload) {
		logger.debug("Subscribe to Observation Stream.");
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;
		try {
			map = mapper.readValue(subscribeToObservationStreamPayload, new TypeReference<HashMap<String, Object>>() {
			});
			final List<String> sensorIDs = (List<String>) map.get("sensorIDs");
			final String endpointURI = (String) map.get("endpointURI");
			final String testbedURI = (String) map.get("testbedURI");
			final String scheduleName = (String) map.get("scheduleName");
			logger.debug("scheduleName " + scheduleName);
			final String userID = (String) map.get("userID");
			logger.debug("userID " + userID);

			if (userID == null) {
				logger.error("No valid user id found.");
				return Response.status(420).build();
			}

			SubscribeToObservationStreamImpl subscribeToObservationStreamImpl = new SubscribeToObservationStreamImpl(
					endpointURI, sensorIDs, testbedURI);
			return subscribeToObservationStreamImpl.subscribeToObservationStream(userID, sensorIDs, testbedURI,
					endpointURI, scheduleName);
		} catch (JsonParseException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (JsonMappingException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (IOException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		}

		return Response.status(420).build();
	}

	/**
	 * Push Observations Stream Proxy
	 * 
	 * @param documentType
	 * @param annotatedMeasurementsDocument
	 * 
	 * @return Success Message
	 */
	@POST
	@Path("/pushObservationsStreamProxy")
	// @Consumes(MediaType.TEXT_PLAIN)
	// @Produces(MediaType.TEXT_PLAIN)
	public Response pushObservationsStreamProxy(@Context HttpHeaders hh, String annotatedMeasurementsDocument) {
		try {
			// logger.debug("Content-Type: " + documentType);
			// hh.getRequestHeader("Content-Type");
			// for(int i=0; i<hh.getRequestHeader("documentType").size(); i++)
			// logger.debug("getRequestHeader: i="+i+" " +
			// hh.getRequestHeader("documentType").get(i));

			MessageBus.pushObservationsToMessageBus(annotatedMeasurementsDocument,
					"Content-Type:" + hh.getRequestHeader("Content-Type").get(0));

			return Response.status(HttpURLConnection.HTTP_OK).entity("Message_Successfully_Pushed").build();

		} catch (JMSException e) {
			logger.error("[ERROR]: Failed to get user ID.  " + e);

			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(e.getMessage()).build();

		}

	}

	/**
	 * 
	 * Subscribe to Observation Stream with Topic.
	 * 
	 * @param query
	 *            the POST payload that includes the following parameters
	 *            sensorIDs: the list of sensor IDs testbedURI: the URI of the
	 *            testbed
	 *
	 *            The query should look like the following { "sensorIDs": [
	 *            <String> <sensor_id_1>, <String> <sensor_id_2>, ..],
	 *            "testbedURI": <String> <url_of_the_testbed>, "topicName":
	 *            <String> <name_of_the_topic> }
	 *
	 * @return the response
	 * 
	 **/
	@POST
	@SuppressWarnings("unchecked")
	@Path("/subscribeToObservationStreamWithTopic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response subscribeToObservationStreamWithTopic(String subscribeToObservationStreamWithTopicPayload) {
		logger.debug("Subscribe to Observation Stream with Topic.");
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;
		try {
			map = mapper.readValue(subscribeToObservationStreamWithTopicPayload,
					new TypeReference<HashMap<String, Object>>() {
					});
			final List<String> sensorIDs = (List<String>) map.get("sensorIDs");
			final String testbedURI = (String) map.get("testbedURI");
			final String topicName = (String) map.get("topicName");

			SubscribeToObservationStreamWithTopicImpl subscribeToObservationStreamWithTopicImpl = new SubscribeToObservationStreamWithTopicImpl(
					testbedURI, sensorIDs, topicName);
			return subscribeToObservationStreamWithTopicImpl.subscribeToObservationStreamWithTopic();
		} catch (JsonParseException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (JsonMappingException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (IOException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		}

		return null; // change return message
	}

	/**
	 * 
	 * Unsubscribe from Observation.
	 * 
	 * @param query
	 *            the POST payload that includes the following parameters
	 *            sensorIDs: the list of sensor IDs testbedURI: the URI of the
	 *            testbed
	 *
	 *            The query should look like the following { "sensorIDs": [
	 *            <String> <sensor_id_1>, <String> <sensor_id_2>, ..],
	 *            "testbedURI": <String> <url_of_the_testbed>, "jobID":
	 *            <String> <ID_of_the_job> }
	 *
	 * @return the response
	 * 
	 **/
	@POST
	@SuppressWarnings("unchecked")
	@Path("/unsubscribeFromObservation")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response unsubscribeFromObservation(String unsubscribeFromObservationPayload) {
		logger.debug("Unsubscribe from Observation.");

		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;

		try {
			map = mapper.readValue(unsubscribeFromObservationPayload, new TypeReference<HashMap<String, Object>>() {
			});
			final List<String> sensorIDs = (List<String>) map.get("sensorIDs");
			final String testbedURI = (String) map.get("testbedURI");
			final String jobID = (String) map.get("jobID");

			// String jobID = "" + DatabaseDMS.getJobID(sensorIDs, testbedURI);
			Response r = JobScheduler.deleteScheduledJob(jobID);
			if (r.getStatus() != 200) {
				return Response.status(r.getStatus()).build();
			}

			int statusResponse = DatabaseDMS.deleteRow(jobID);
			if (statusResponse != 200) {
				return Response.status(statusResponse).build();
			}

			// return Response.ok("{\"response\" : \"Job deleted.\" , \n
			// \"jobID\" : \"" + jobID +
			// "\"}",MediaType.APPLICATION_JSON).build();
			return r;

		} catch (JsonParseException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (JsonMappingException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		} catch (IOException e) {
			logger.error("[ERROR]: Failed to get measurements. " + e);
		}
		// return null;
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}

	/**
	 * 
	 * Resumes a scheduled job.
	 * 
	 * @param id
	 *            the id of the scheduled job to be resumed.
	 *
	 * @return the response
	 * 
	 **/
	@GET
	@Path("/getAllScheduledJobs/{userID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllScheduledJobs(@PathParam("userID") String userID) {
		return (Response) DatabaseDMS.getAllScheduledJobsInDB(userID);
	}

	/**
	 * 
	 * Deletes a scheduled job.
	 * 
	 * @param id
	 *            the id of the scheduled job to be deleted.
	 *
	 * @return the response
	 * 
	 **/
	@GET
	@Path("/deleteScheduledJob/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeScheduledJob(@PathParam("id") String id) {
		logger.debug("Delete Scheduled Job from JobScheduler.");
		return JobScheduler.deleteScheduledJob(id);
	}

	/**
	 * 
	 * Pauses a scheduled job.
	 * 
	 * @param id
	 *            the id of the scheduled job to be paused.
	 *
	 * @return the response
	 * 
	 **/
	@GET
	@Path("/pauseScheduledJob/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response pausecheduledJob(@PathParam("id") String id) {
		logger.debug("Pause scheduled job.");
		return JobScheduler.pauseScheduledJob(id);
	}

	/**
	 * 
	 * Resumes a scheduled job.
	 * 
	 * @param id
	 *            the id of the scheduled job to be resumed.
	 *
	 * @return the response
	 * 
	 **/
	@GET
	@Path("/resumeScheduledJob/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resumecheduledJob(@PathParam("id") String id) {
		logger.debug("Resume scheduled job");
		return JobScheduler.resumeScheduledJob(id);
	}

	/**
	 * 
	 * Returns all the scheduled jobs.
	 * 
	 * @return the response
	 * 
	 **/
	@GET
	@Path("/getScheduledJobs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduledJobs() {
		logger.debug("Get Scheduled Job Details.");
		return (Response) JobScheduler.getScheduledJobs();
	}

	/**
	 * 
	 * Gets metadata of the scheduled job with the given id.
	 * 
	 * @param id
	 *            the id of the scheduled job to be deleted.
	 *
	 * @return the metadata of the scheduled job with the given id.
	 * 
	 **/
	@GET
	@Path("/metadata/scheduledJob/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getMetadataForScheduledJob(@PathParam("id") String id) {
		logger.debug("Get Scheduled Job Details.");
		return JobScheduler.getScheduledJobMetadata(id);
	}

	/**
	 * 
	 * Deletes all scheduled jobs.
	 * 
	 * @return the results
	 * 
	 **/
	@GET
	@Path("/deleteAllScheduledJobs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAllScheduledJobs() {
		logger.debug("Delete all scheduled jobs.");
		return JobScheduler.deleteAllScheduledJobs();
	}

	/**
	 * 
	 * Gets all currently executing jobs. NOTE: This method returns all jobs
	 * that are executed at the exatct point when the rest service is requested.
	 * 
	 * @return the results
	 * 
	 **/
	@GET
	@Path("/getCurrentlyExecutingJobs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrentlyExecutingJobs() {
		logger.debug("Get currently executing Job Details.");
		return JobScheduler.currentlyExecutingJobs();
	}

	/**
	 * 
	 * Gets user specific configurations for nodered. NOTE: This method returns
	 * the flows, the state of each flow and the scheduled job ids for a
	 * specific user in json format
	 * 
	 * @return the results
	 * 
	 **/
	@GET
	@Path("/getSettings/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSettings(@PathParam("username") String username) {
		logger.debug("Get configurations for user [" + username + "]");

		// TODO: add functionality here

		return null;
	}

	/**
	 * 
	 * Saves user specific configurations for nodered. NOTE: This method saves
	 * the flows, the state of each flow and the scheduled job ids for a
	 * specific user.
	 * 
	 * The payload should look like the following { "flows":
	 * <String> <the_flows>, "state": List<Map<String, String>>
	 * <the_state_list>, }
	 * 
	 * Examples: e.g.1 the_state_list = { [<String> <scheduled_job_1>,
	 * <String> <state_of_scheduled_job_1>], [<String> <scheduled_job_2>,
	 * <String> <state_of_scheduled_job_2>], ..}
	 * 
	 * @return the message
	 * 
	 **/
	@POST
	@Path("/saveSettings/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveSettings(@PathParam("username") String username, String saveSettingsPayload) {
		logger.debug("Save configurations for user [" + username + "]");

		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;

		try {
			map = mapper.readValue(saveSettingsPayload, new TypeReference<HashMap<String, Object>>() {
			});

			final String flows = (String) map.get("flows");
			@SuppressWarnings("unchecked")
			final List<Map<String, Object>> state = (List<Map<String, Object>>) map.get("state");

			logger.debug("[flows]: " + flows);
			logger.debug("[state]: " + state);

			// TODO: add functionality here

		} catch (JsonParseException e) {
			logger.error("[ERROR]: Failed to read settings. " + e);
		} catch (JsonMappingException e) {
			logger.error("[ERROR]: Failed to read settings. " + e);
		} catch (IOException e) {
			logger.error("[ERROR]: Failed to read settings. " + e);
		}

		return null; // change return message
	}

	public boolean isSecurityEnabled() {
		return true;
	}

}
