package eu.fiestaiot.tpi.api.dms.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.fiestaiot.commons.util.PropertyManagement;
import eu.fiestaiot.tpi.api.dms.impl.dataservices.SubscribeToObservationImpl;

public class DatabaseDMS {

	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(DatabaseDMS.class);

	/**
	 * The username for the DataBase
	 */
	private static String usernameDB;

	/**
	 * The password for the DataBase
	 */
	private static String passwordDB;

	/**
	 * The JDBC Driver
	 */
	private static String jdbcDriver;

	/**
	 * The database URL
	 */
	private static String dbURL;

	/**
	 * The database table name
	 */
	private static String tableName;

	/**
	 * Initializes the Database parameters.
	 */
	public DatabaseDMS() {
		logger.debug("Initialize database.");
		PropertyManagement propertyManagement = new PropertyManagement();
		DatabaseDMS.usernameDB = propertyManagement.getUsernameDB();
		DatabaseDMS.passwordDB = propertyManagement.getPasswordDB();
		DatabaseDMS.jdbcDriver = propertyManagement.getJDBCDriver();
		DatabaseDMS.dbURL = propertyManagement.getDBURL();
		DatabaseDMS.tableName = propertyManagement.getTableName();
		logger.debug("DONE: Initialize database.");
	}

	/**
	 * Prints all IDs inside the database.
	 */
	public static void getjobIDsFromDB() {
		Connection conn = null;
		Statement stmt = null;

		try {
			logger.debug("Get job IDs from database.");
			Class.forName(jdbcDriver);
			logger.debug("Connecting to database...");
			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);
			logger.debug("Creating statement...");
			stmt = conn.createStatement();
			String sql = "SELECT jobID FROM " + tableName;
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int id = rs.getInt("jobID");
				logger.debug("ID: " + id);
			}

			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to get jobs from DB.");
			logger.error("Connection to database closed.");
			se.printStackTrace();
		} catch (Exception e) {
			logger.error("Failed to get jobs from DB.");
			logger.error("Connection to database closed.");
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to get jobs from DB.");
				logger.error("Connection to database closed.");
				se2.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to get jobs from DB.");
				logger.error("Connection to database closed.");
				se.printStackTrace();
			}
		}
		logger.debug("Connection to database closed.");
		logger.debug("DONE: Get job IDs from database.");
	}

	/**
	 * Inserts a whole row from the database (i.e. a scheduled job) This is
	 * performed when the subscribeToObservations service is called. It checks
	 * first for duplications.
	 * 
	 * @param sensorIDs
	 *            the sensor ids.
	 * @param testbedURI
	 *            the testbed URI.
	 * @param endpointURI
	 *            the endpoint URI.
	 * @param startDate
	 *            the start date.
	 * @param frequency
	 *            the frequency.
	 * @param timeUnit
	 *            the time unit.
	 * 
	 * @return the id of the newly inserted job
	 */
	public static int insertRow(String userID, List<String> sensorIDs, String testbedURI, String endpointURI,
			String startDate, int frequency, String timeUnit, String securityKey, String scheduleName) {
		logger.debug("Insert row to database.");

		Connection conn = null;
		Statement stmt = null;
		int jobID = 0;

		try {
			logger.debug("Registering JDBC Driver...");
			Class.forName(jdbcDriver);

			logger.debug("Connecting to database...");
			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);

			logger.debug("Creating statement...");
			stmt = conn.createStatement();
			int exists = getJobID(sensorIDs, testbedURI);

			if (exists != 0) {
				logger.error("Failed to insert job to DB. Job already exists.");
				logger.error("Connection to database closed.");
				return 0;
			}

			String sql = "INSERT INTO " + tableName
					+ " (userID, sensorIDs, testbedURI, endpointURI, startDate, frequency, timeUnit, securityKey, scheduleName) "
					+ "VALUES ('" + userID + "', '" + sensorIDs.toString() + "', '" + testbedURI + "', '" + endpointURI
					+ "', '" + startDate + "', '" + frequency + "', '" + timeUnit + "', '" + securityKey + "', '"
					+ scheduleName + "')";
			stmt.executeUpdate(sql);

			sql = "SELECT jobID FROM " + tableName + " ORDER BY jobID DESC LIMIT 1;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				jobID = rs.getInt("jobID");
			}
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to insert job to DB.");
			logger.error("Connection to database closed.");
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to insert job to DB.");
			logger.error("Connection to database closed.");
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to insert job to DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to insert job to DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se);
			}
		}
		logger.debug("DONE: Insert row to database.");
		return jobID;
	}

	/**
	 * Checks if the table exists in the database If the table does not exist, a
	 * new one is created Else, do nothing
	 */
	public static void tableExists() {
		logger.debug("Check if table exists.");

		Connection conn = null;
		Statement stmt = null;

		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);
			stmt = conn.createStatement();
			String sql;
			sql = "CREATE TABLE IF NOT EXISTS  " + tableName + " (" + "JobID int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "userID varchar(255)," + "sensorIDs longblob," + "testbedURI varchar(255),"
					+ "endpointURI varchar(255)," + "startDate varchar(255)," + "frequency int,"
					+ "timeUnit varchar(255)," + "securityKey varchar(255)" + "," + "scheduleName varchar(255)" + ");";
			stmt.executeUpdate(sql);
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to check if table exists in DB.");
			logger.error("Connection to database closed.");
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to check if table exists in DB.");
			logger.error("Connection to database closed.");
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to check if table exists in DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to check if table exists in DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se);
				se.printStackTrace();
			}
		}
		logger.debug("DONE: Check if table exists.");
	}

	/**
	 * Searches for the ID of a specific scheduled job in the database.
	 * 
	 * @return the ID of a specific scheduled job in the database.
	 */
	public static int getJobID(List<String> sensorIDs, String testbedURI) {
		logger.debug("Get job ID.");

		int id = 0;
		Connection conn = null;
		Statement stmt = null;

		try {
			Class.forName(jdbcDriver);

			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);

			stmt = conn.createStatement();
			String sql = "SELECT jobID FROM " + tableName + " WHERE testbedURI='" + testbedURI + "' AND sensorIDs='"
					+ sensorIDs.toString() + "'";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				id = rs.getInt("jobID");
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to get Job ID from DB.");
			logger.error("Connection to database closed.");
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to get Job ID from DB.");
			logger.error("Connection to database closed.");
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to get Job ID from DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to get Job ID from DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se);
			}
		}
		logger.debug("DONE: Get job ID.");
		return id;
	}

	/**
	 * Searches for the ID of a specific scheduled job in the database.
	 * 
	 * @return the ID of a specific scheduled job in the database.
	 */
	public static Response getAllScheduledJobsInDB(String userID) {
		logger.debug("Get all scheduled jobs.");

		Connection conn = null;
		Statement stmt = null;
		Map<String, Object> map = new HashMap<>();
		List<Object> objectJson = new ArrayList<>();

		try {
			Class.forName(jdbcDriver);
			logger.debug("Get Job ID");

			logger.debug("Connecting to database...");
			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);

			logger.debug("Creating statement...");
			stmt = conn.createStatement();
			String sql = "SELECT JobID, sensorIDs, testbedURI, endpointURI, scheduleName FROM " + tableName
					+ " WHERE userID='" + userID + "'";

			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				map = new HashMap<>();
				String sensorIDs = rs.getString("sensorIDs");
				sensorIDs = sensorIDs.replace("[", "");
				sensorIDs = sensorIDs.replace("]", "");

				map.put("sensorIDs", Arrays.asList((sensorIDs.split(", "))));
				map.put("jobID", rs.getString("JobID"));
				map.put("testbedURI", rs.getString("testbedURI"));
				map.put("endpointURI", rs.getString("endpointURI"));
				map.put("scheduleName", rs.getString("scheduleName"));
				objectJson.add(map);
			}

			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to get all scheduled Jobs from DB.");
			logger.error("Connection to database closed.");
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to get all scheduled Jobs from DB.");
			logger.error("Connection to database closed.");
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to get all scheduled Jobs from DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to get all scheduled Jobs from DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se);
			}
		}
		logger.debug("DONE: Get all scheduled jobs.");
		return Response.ok(objectJson, MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Deletes a whole row from the database (i.e. a scheduled job) This is
	 * performed when the unsubscribeFromObservations service is called.
	 */
	public static int deleteRow(String jobID) {
		logger.debug("Delete a row from database.");

		int returnValue = 200;

		Connection conn = null;
		Statement stmt = null;

		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);
			stmt = conn.createStatement();
			String sql = "DELETE FROM " + tableName + " WHERE jobID='" + jobID + "'";
			stmt.executeUpdate(sql);
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to delete scheduled job (row) from DB.");
			logger.error("Connection to database closed.");
			returnValue = 420;
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to delete scheduled job (row) from DB.");
			logger.error("Connection to database closed.");
			returnValue = 420;
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to delete scheduled job (row) from DB.");
				logger.error("Connection to database closed.");
				returnValue = 420;
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to delete scheduled job (row) from DB.");
				logger.error("Connection to database closed.");
				returnValue = 420;
				logger.error("" + se);
			}
		}
		logger.debug("DONE: Delete a row from database.");
		return returnValue;
	}

	/**
	 * Checks for any scheduled job in the database to resume them after the
	 * server recovers from an unexpected shut down.
	 */
	public static int checkForJobsInDB() {
		logger.debug("Check for jobs in database.");

		Connection conn = null;
		Statement stmt = null;
		int countJobs = 0;

		try {
			Class.forName(jdbcDriver);
			logger.debug("Connecting to database...");
			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);

			logger.debug("Creating sql statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT COUNT(*) FROM " + tableName;
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				countJobs = rs.getInt("COUNT(*)");
			}
			logger.debug("Found " + countJobs + " in the database.");

			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to check for scheduled jobs in DB.");
			logger.error("Connection to database closed.");
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to check for scheduled jobs in DB.");
			logger.error("Connection to database closed.");
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to check for scheduled jobs in DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to check for scheduled jobs in DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se);
			}
		}
		logger.debug("Connection to database closed.");
		logger.debug("DONE: Check for jobs in database.");
		return countJobs;
	}

	/**
	 * Resumes the scheduled jobs if the server shuts down unexpectedly. It
	 * checks if the table in the database has any entries. If so, it starts all
	 * scheduled jobs immediately (the startTime is set to the current time).
	 * 
	 */
	public static void resumeScheduledJobs() {
		logger.debug("Resume all existing scheduled jobs.");

		Connection conn = null;
		Statement stmt = null;
		String startDate = null;
		String sensorIDsStr = "";
		String testbedURI = "";
		String endpointURI = "";
		String timeUnit = "";
		String securityKey = "";
		int jobID = 0;
		int frequency;

		try {
			Class.forName(jdbcDriver);
			logger.debug("Resume");

			conn = DriverManager.getConnection(dbURL, usernameDB, passwordDB);

			stmt = conn.createStatement();
			String sql = "SELECT jobID, sensorIDs, testbedURI, endpointURI, startDate, frequency, timeUnit, securityKey FROM "
					+ tableName;
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				jobID = rs.getInt("jobID");
				sensorIDsStr = rs.getString("sensorIDs");
				sensorIDsStr = sensorIDsStr.substring(1, sensorIDsStr.length() - 1);
				ArrayList<String> sensorIDs = new ArrayList<>(Arrays.asList(sensorIDsStr.split("\\s*,\\s*")));
				testbedURI = rs.getString("testbedURI");

				if (testbedURI.endsWith("getLastObservations") || testbedURI.endsWith("getObservations")) {
					endpointURI = rs.getString("endpointURI");
					startDate = rs.getString("startDate");
					frequency = rs.getInt("frequency");
					timeUnit = rs.getString("timeUnit");
					securityKey = rs.getString("securityKey");
					final SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy",
							Locale.US);
					final Date startTime = dateParser.parse(startDate);
					final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					dateFormatter.format(startTime);

					TimeSchedule timeSchedule = new TimeSchedule(startTime, frequency, timeUnit, jobID + "",
							testbedURI);

					SubscribeToObservationImpl subscribeToObservationImpl = new SubscribeToObservationImpl(sensorIDs,
							testbedURI, timeSchedule, securityKey);
					subscribeToObservationImpl.subscribeToObservation();
				} else if (testbedURI.endsWith("pushLastObservations") || testbedURI.endsWith("stopPushOfObservations")) {
					/*TODO: check functionality */				
					/*Do nothing. The job is already running on the testbed side.*/
				} else {
					/*ToDo: add functionality */ 
				}
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			logger.error("Failed to resume scheduled jobs from DB.");
			logger.error("Connection to database closed.");
			logger.error("" + se);
		} catch (Exception e) {
			logger.error("Failed to resume scheduled jobs from DB.");
			logger.error("Connection to database closed.");
			logger.error("" + e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.error("Failed to resume scheduled jobs from DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.error("Failed to resume scheduled jobs from DB.");
				logger.error("Connection to database closed.");
				logger.error("" + se);
			}
		}
		logger.debug("DONE: Resume all existing scheduled jobs.");

	}

	/**
	 * Gets the username.
	 * 
	 * @return the username.
	 */
	public String getUsernameDB() {
		return usernameDB;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password.
	 */
	public String getPasswordDB() {
		return passwordDB;
	}

	/**
	 * Gets the jdbc driver.
	 * 
	 * @return the jdbc driver.
	 */
	public String getjdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Gets database URL.
	 * 
	 * @return database URL.
	 */
	public String getDBURL() {
		return dbURL;
	}

}
