package eu.fiestaiot.tpi.api.dms.impl.dataservices;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.fiestaiot.tpi.api.dms.service.DatabaseDMS;

/**
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * 
 */
public class SubscribeToObservationStreamImpl {
	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(SubscribeToObservationStreamImpl.class);

	/**
	 * The endpoint URI.
	 */
	private String endpointURI;

	/**
	 * The testbed URI.
	 */
	private String testbedURI;

	/**
	 * The sensors ID list.
	 */
	private List<String> sensorIDs;

	/**
	 * Initializes class variables.
	 *
	 * @param endpointURI
	 *            the endpoint URI.
	 * 
	 * @param sensorID
	 *            the list of sensor IDs.
	 * 
	 */
	public SubscribeToObservationStreamImpl(String endpointURI, List<String> sensorIDs, String testbedURI) {
		this.endpointURI = endpointURI;
		this.sensorIDs = sensorIDs;
		this.testbedURI = testbedURI;
	}

	/**
	 * Implementation of subscribeToObservationStream
	 * 
	 * @return
	 */
	public Response subscribeToObservationStream(String userID, List<String> sensorIDs, String testbedURI,
			String endpointURI, String scheduleName) {
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
			final RequestConfig config = RequestConfig.custom().setSocketTimeout(90000).setConnectTimeout(90000)
					.setConnectionRequestTimeout(90000).build();
			request.setConfig(config);

			final HttpResponse response = client.execute(request);
			final int sc = response.getStatusLine().getStatusCode();

			if (sc != HttpStatus.SC_OK) {
				logger.error("Failed to schedule the task [ status code: " + sc + " ].");
				return Response.status(420).build();
			} else {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> map = mapper.readValue(body, new TypeReference<HashMap<String, Object>>() {
				});
				String stopPushOfObservationsURI = (String) map.get("stopPushOfObservations");
				if (stopPushOfObservationsURI != null && stopPushOfObservationsURI.compareToIgnoreCase("") != 0) {
					String id = "" + DatabaseDMS.insertRow(userID, sensorIDs, stopPushOfObservationsURI, endpointURI,
							"", 0, "", "", scheduleName);
					// resume all scheduled jobs should check if push is done
					// and do nothing for this job

					if (id.compareToIgnoreCase("0") == 0) {
						return Response.status(420).build();
					}

					return Response.ok(
							"{\"response\" : \"Job scheduled.\" , \n \"jobID\" : \"" + id
									+ "\", \n \"stopPushOfObservations\" : \"" + stopPushOfObservationsURI + "\"}",
							MediaType.APPLICATION_JSON).build();
				} else {
					return Response.status(420).build();
				}
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

}
