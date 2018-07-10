package eu.fiestaiot.tpi.api.dms.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSchedule {

	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(TimeSchedule.class);

	/**
	 * The start time.
	 */
	Date startTime;

	/**
	 * The the stop time.
	 */
	Date stopTime;

	/**
	 * The the frequency.
	 */
	int frequency;

	/**
	 * The number of rounds
	 */
	int repeatCount;

	/**
	 * The time unit.
	 */
	String timeUnit;

	/**
	 * The unique id.
	 */
	String id;

	/**
	 * The uri of the service provided by the testbed.
	 */
	String testbedURI;

	/**
	 * Constructs a TimeSchedule.
	 * 
	 * @param startTime
	 *            the start time.
	 * @param stopTime
	 *            the stop time.
	 * @param frequency
	 *            the frequency.
	 * @param repeatCount
	 *            the number of rounds.
	 * @param timeUnit
	 *            the time unit.
	 * @param id
	 *            the unique id.
	 * @param testbedURI
	 *            the testbed URI.
	 */
	public TimeSchedule(Date startTime, Date stopTime, int frequency, int repeatCount, String timeUnit, String id,
			String testbedURI) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.frequency = frequency;
		this.repeatCount = repeatCount;
		this.timeUnit = timeUnit;
		this.id = id;
		this.testbedURI = testbedURI;
	}

	/**
	 * Constructs a TimeSchedule.
	 * 
	 * @param startTime
	 *            the start time.
	 * @param frequency
	 *            the frequency.
	 * @param timeUnit
	 *            the time unit.
	 * @param id
	 *            the unique id.
	 * 
	 */
	public TimeSchedule(Date startTime, int frequency, String timeUnit, String id, String testbedURI) {
		this.startTime = startTime;
		this.frequency = frequency;
		this.timeUnit = timeUnit;
		this.id = id;
		this.testbedURI = testbedURI;
	}

	/**
	 * Gets the start time.
	 * 
	 * @return the start time.
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Gets the start time.
	 * 
	 * @return the start time.
	 */
	public String getTestbedURI() {
		return testbedURI;
	}

	/**
	 * Gets the stop time.
	 * 
	 * @return the stop time.
	 */
	public Date getStopTime() {
		return stopTime;
	}

	/**
	 * Gets the frequency.
	 * 
	 * @return the frequency.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Gets the timeout.
	 * 
	 * @return the timeout.
	 */
	public int getRepeatCount() {
		return repeatCount;
	}

	/**
	 * Gets the time unit.
	 * 
	 * @return the time unit.
	 */
	public String getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Gets the stop time.
	 * 
	 * @return the stop time.
	 */
	public boolean hasStopTime() {
		return stopTime != null;
	}

	/**
	 * check fields of scheduled job
	 * 
	 * @return the true if all fields are in correct format, else return false.
	 */
	public boolean checkSchedule() {
		if (stopTime.compareTo(startTime) > 0) {
		    logger.error("[ERROR]: Stop time is after the start time. ");
			return false;
		}

		if (repeatCount < 0) {
		    logger.error("[ERROR]: The repeat count must be larger than or equal to zero. ");
			return false;
		}

		if (frequency < 0) {
		    logger.error("[ERROR]: The frequency must be larger than or equal to zero. ");
			return false;
		}

		if (timeUnit.compareToIgnoreCase("day") != 0 || timeUnit.compareToIgnoreCase("hour") != 0
				|| timeUnit.compareToIgnoreCase("minute") != 0 || timeUnit.compareToIgnoreCase("second") != 0
				|| timeUnit.compareToIgnoreCase("month") != 0) {
			logger.error("[ERROR]: Wrong format of the time. ");
			return false;
		}
		
		logger.debug("Every field is in correct format.");
		return true;
	}

}
