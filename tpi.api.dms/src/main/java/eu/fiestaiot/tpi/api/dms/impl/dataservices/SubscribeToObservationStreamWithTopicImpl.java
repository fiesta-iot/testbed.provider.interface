package eu.fiestaiot.tpi.api.dms.impl.dataservices;

import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * 
 */
public class SubscribeToObservationStreamWithTopicImpl {
	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(SubscribeToObservationStreamWithTopicImpl.class);

	/**
	 * The endpoint URI.
	 */
	private String endpointURI;

	/**
	 * The sensors ID list.
	 */
	private List<String> sensorIDs;

	/**
	 * The topic name.
	 */
	private String topicName;

	/**
	 * Initializes class variables.
	 *
	 * @param endpointURI
	 *            the endpoint URI.
	 * 
	 * @param sensorID
	 *            the list of sensor IDs.
	 */
	public SubscribeToObservationStreamWithTopicImpl(String endpointURI, List<String> sensorIDs, String topicName) {
		this.endpointURI = endpointURI;
		this.sensorIDs = sensorIDs;
		this.topicName = topicName;
	}

	/**
	 * Implementation of subscribeToObservationStreamWithTopic
	 * 
	 * @return
	 */
	public Response subscribeToObservationStreamWithTopic() {
		// TODO: implement this method
		return null;
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
	 * Gets the name of the Topic.
	 * 
	 * @return the name of the Topic.
	 */
	public String getTopicName() {
		return topicName;
	}
}
