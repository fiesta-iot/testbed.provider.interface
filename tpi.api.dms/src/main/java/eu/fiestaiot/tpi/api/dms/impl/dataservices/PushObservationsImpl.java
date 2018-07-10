package eu.fiestaiot.tpi.api.dms.impl.dataservices;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Katerina Pechlivanidou (kape) e-mail: kape@ait.gr
 * 
 */
public class PushObservationsImpl {
	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(PushObservationsImpl.class);

	/**
	 * The endpoint URI.
	 */
	private String testbedURI;

	/**
	 * The sensors ID list.
	 */
	private List<String> sensorIDs;

	/**
	 * The periodicity.
	 */
	private int periodicity;

	/**
	 * Initializes class variables.
	 *
	 * @param testbedURI
	 *            the testbed URI.
	 * 
	 * @param sensorID
	 *            the list of sensor IDs.
	 * 
	 * @param periodicity
	 *            the periodicity (in seconds).
	 */
	public PushObservationsImpl(String testbedURI, List<String> sensorIDs, int periodicity) {
		this.testbedURI = testbedURI;
		this.sensorIDs = sensorIDs;
		this.periodicity = periodicity;
	}

	/**
	 * Implementation of pushObservations
	 */
	public void pushObservations() {
	}

	/**
	 * Gets the uri of the testbed.
	 * 
	 * @return the uri of the testbed.
	 */
	public String getTestbedURI() {
		return testbedURI;
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
	 * Gets the periodicity.
	 * 
	 * @return the periodicity
	 * 
	 */
	public int getPeriodicity() {
		return periodicity;
	}
}
