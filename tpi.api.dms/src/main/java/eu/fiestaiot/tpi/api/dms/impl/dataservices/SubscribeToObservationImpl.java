package eu.fiestaiot.tpi.api.dms.impl.dataservices;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
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
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.fiestaiot.commons.util.PropertyManagement;
import eu.fiestaiot.tpi.api.dms.service.JobScheduler;
import eu.fiestaiot.tpi.api.dms.service.MessageBus;
import eu.fiestaiot.tpi.api.dms.service.TimeSchedule;

/**
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * 
 */
public class SubscribeToObservationImpl extends Thread implements Job {

	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(SubscribeToObservationImpl.class);

	/**
	 * The sensor IDs list.
	 */
	private List<String> sensorIDs = new ArrayList<>();

	/**
	 * The endpoint URI.
	 */
	private String endpointURI;

	/**
	 * The Time Schedule.
	 */
	private TimeSchedule timeSchedule;

	/**
	 * The testbed endpoint for the getObservations service.
	 */
	private String restEndpoint;
	
	/**
	 * The testbed's security key.
	 */
	private String securityKey;

	/**
	 * The testbed's URI.
	 */
	private String testbedURI;
	
	/**
	 * Default connection timeout
	 */
	int defaultConnectionTimeout = 30000;
	
	/**
	 * 
	 * @param sensorIDs
	 *            the list of sensor IDs
	 * 
	 * @param endpointURI
	 *            the URI of the endpoint
	 * 
	 * @param testbedURI
	 *            the URI of the testbed
	 * 
	 */
	public SubscribeToObservationImpl(List<String> sensorIDs, String endpointURI, String testbedURI) {
		this.sensorIDs = sensorIDs;
		this.endpointURI = endpointURI;
		this.testbedURI = testbedURI;
	}

	/**
	 * 
	 * @param sensorIDs
	 *            the list of sensor IDs
	 * 
	 * @param testbedURI
	 *            the URI of the testbed
	 * 
	 * @param timeSchedule
	 *            time schedule
	 * 
	 * @param securityKey
	 *            security key
	 * 
	 */
	public SubscribeToObservationImpl(List<String> sensorIDs, String endpointURI, TimeSchedule timeSchedule, String securityKey) {
		this.sensorIDs = sensorIDs;
		this.endpointURI = endpointURI;
		this.timeSchedule = timeSchedule;
		this.securityKey = securityKey;
	}

	/**
	 * The default constructor. NOTE: it is used to get an instance of the class
	 * used by the JobScheduler class
	 */
	public SubscribeToObservationImpl() {
	}

	/**
	 * Executes the scheduled getObservationResultSet().
	 * 
	 * @param context
	 *            the job context.
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("Execute scheduled job.");
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		List<String> sensorIDs = (List<String>) dataMap.get("sensorIDs");
		String securityKey = (String) dataMap.get("securityKey");
		TimeSchedule timeSchedule = (TimeSchedule) dataMap.get("timeSchedule");

		List<Date> timePeriod = new ArrayList<>();
		timePeriod.add(context.getScheduledFireTime());
		timePeriod.add(context.getNextFireTime());

		final HttpClient client = HttpClients.createDefault();
		final HttpPost request = new HttpPost(timeSchedule.getTestbedURI());
		final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		Map<String, Object> payload = new HashMap<>();		
		PropertyManagement propertyManagement = new PropertyManagement();
		
		int connectionTimeoutForScheduledJob = defaultConnectionTimeout;
		int getLastObservationConnectionTimeOut;
		int getObservationConnectionTimeOut;
		
		if (timeSchedule.getTestbedURI().endsWith("getLastObservations")) {
			logger.debug("Get last observations.");
			if(propertyManagement.getLastObservationConnectionTimeout()!=null)
			{
				getLastObservationConnectionTimeOut = Integer.parseInt(propertyManagement.getLastObservationConnectionTimeout());
			}
			else{
				getLastObservationConnectionTimeOut = defaultConnectionTimeout;
			}
			connectionTimeoutForScheduledJob = getLastObservationConnectionTimeOut;
			
		} else if (timeSchedule.getTestbedURI().endsWith("getObservations")) {
			logger.debug("Get observations for a specific time period.");
			
			if(propertyManagement.getObservationsConnectionTimeout()!=null)
			{
				getObservationConnectionTimeOut = Integer.parseInt(propertyManagement.getObservationsConnectionTimeout());
			}
			else{
				getObservationConnectionTimeOut = defaultConnectionTimeout;
			}
			connectionTimeoutForScheduledJob = getObservationConnectionTimeOut;
			
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			Date stopDateTimePeriod;
			try {
				stopDateTimePeriod = dt.parse(DATE_FORMAT.format(context.getScheduledFireTime()));
				Date d1 = stopDateTimePeriod;
				Date d2 = dt.parse(DATE_FORMAT.format(context.getNextFireTime()));
				long diff = d2.getTime() - d1.getTime();
				long diffSeconds = diff / 1000 % 60;
				Calendar cal = Calendar.getInstance();
				cal.setTime(d1);
				cal.add(Calendar.SECOND, (int) -diffSeconds);
				Date startDateTimePeriod = cal.getTime();
				payload.put("startDate", startDateTimePeriod);
				payload.put("stopDate", stopDateTimePeriod);
			} catch (ParseException e) {
				logger.error("[ERROR]: Could not parse the given Dates. ");
				logger.error("" + e);
			}
			
		} else {
		}

		payload.put("sensorIDs", sensorIDs);
		
		logger.debug("sensorIDs: "+sensorIDs);
		logger.debug("payload: "+payload);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		StringEntity params;
		try {			
			params = new StringEntity(objectMapper.writeValueAsString(payload));
			request.addHeader("content-type", "application/json");			
			request.setEntity(params);

			logger.debug("params: "+params);
			logger.debug("request: "+request.getEntity().toString());
			logger.debug("connectionTimeoutForScheduledJob: " + connectionTimeoutForScheduledJob);
			
			final RequestConfig config = RequestConfig.custom().setSocketTimeout(connectionTimeoutForScheduledJob).setConnectTimeout(connectionTimeoutForScheduledJob)
					.setConnectionRequestTimeout(connectionTimeoutForScheduledJob).build();
			request.setConfig(config);
			if(securityKey.compareToIgnoreCase("none")!=0){
				request.addHeader("Authorization", "Basic " + securityKey);
			}
			
			final HttpResponse response = client.execute(request);
			final int sc = response.getStatusLine().getStatusCode();

			if (sc != HttpStatus.SC_OK) {
				logger.debug("Failed to get data [ status code: " + sc + " ].");
			} else {
				logger.debug("[ status code: " + sc + " ].");
				logger.debug("Data retrieved.");
				ResponseHandler<String> handler = new BasicResponseHandler(); 
				String contentType = response.getFirstHeader("Content-Type").toString();				
				String body = handler.handleResponse(response);
				ObjectMapper mapper = new ObjectMapper();
				String jsonInString = mapper.writeValueAsString(body);
				MessageBus.pushObservationsToMessageBus(jsonInString, contentType);
//				logger.debug("message received from testbed: " + jsonInString);

				logger.debug("Messages pushed to Meesagebus.");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("[ERROR]: Could not retrieve data or send it to the messagebus. ");
			logger.error("" + e);
		} catch (JsonProcessingException e) {
			logger.error("[ERROR]: Could not retrieve data or send it to the messagebus. ");
			logger.error("" + e);
		} catch (ClientProtocolException e) {
			logger.error("[ERROR]: Could not retrieve data or send it to the messagebus. ");
			logger.error("" + e);
		} catch (IOException e) {
			logger.error("[ERROR]: Could not retrieve data or send it to the messagebus. ");
			logger.error("" + e);
		} catch (JMSException e) {
			logger.error("[ERROR]: Could not retrieve data or send it to the messagebus. ");
			logger.error("" + e);
		}
	}

	/**
	 * Implements the subscribe to observation
	 * 
	 * @return the response returned in json format.
	 */
	public Response subscribeToObservation() {
		logger.debug("Subscribe to Observation");

		if (timeSchedule != null) {
			JobScheduler js = new JobScheduler(timeSchedule, timeSchedule.getID(), sensorIDs, securityKey);
			js.scheduleJob();

			return Response.ok("{\"response\" : \"Job scheduled.\" , \n \"jobID\" : \"" + timeSchedule.getID() + "\"}",
					MediaType.APPLICATION_JSON).build();
		} else {
			logger.error("No time schedule for this Job");
		}
		return Response.serverError().build();
	}

	/**
	 * Gets the uri of the endpoint.
	 * 
	 * @return the uri of the endpoint.
	 */
	public String getEndpointURI() {
		return endpointURI;
	}

	/**
	 * Gets the sensor IDs list.
	 * 
	 * @return the sensor IDs list.
	 */
	public List<String> getSensorIDs() {
		return sensorIDs;
	}

	/**
	 * Gets the Time Schedule.
	 * 
	 * @return the Time Schedule.
	 */
	public TimeSchedule getTimeSchedule() {
		return timeSchedule;
	}

	/**
	 * Gets the rest endpoint.
	 * 
	 * @return the rest endpoint.
	 */
	public String getRestEndpoint() {
		return restEndpoint;
	}
	
	/**
	 * Gets the security key.
	 * 
	 * @return the security key.
	 */
	public String getSecurityKey() {
		return securityKey;
	}

	/**
	 * Sets the security key.
	 */
	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}


}
