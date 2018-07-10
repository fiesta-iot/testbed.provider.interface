package eu.fiestaiot.tpi.api.dms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.*;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.fiestaiot.tpi.api.dms.impl.dataservices.SubscribeToObservationImpl;

public class JobScheduler {

	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	/**
	 * The time schedule
	 */
	TimeSchedule timeSchedule;

	/**
	 * The stop time.
	 */
	String stopTime;

	/**
	 * The id
	 */
	String id;

	/**
	 * The list with the Sensor IDs.
	 */
	List<String> sensorIDs;
	
	/**
	 * The security key.
	 */
	String securityKey;
	

	/**
	 * Constructs a JobScheduler.
	 * 
	 * @param timeSchedule
	 *            the time schedule.
	 * @param id
	 *            the id.
	 * @param sensorIDs
	 *            the sensor IDs list.
	 */
	public JobScheduler(TimeSchedule timeSchedule, String id, List<String> sensorIDs, String securityKey) {
		logger.debug("Job Scheduler initializing.");
		this.timeSchedule = timeSchedule;
		this.id = id;
		this.sensorIDs = sensorIDs;
		this.securityKey = securityKey;
	}

	/**
	 * Deletes the given scheduled Job.
	 * 
	 * @param id
	 *            the id of the scheduled job that should get deleted.
	 */
	public static Response deleteScheduledJob(String id) {
		SchedulerFactory schFactory = new StdSchedulerFactory();

		try {
			Scheduler scheduler = schFactory.getScheduler();
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					if (jobKey.toString().compareToIgnoreCase(group + "." + id) == 0) {
						logger.debug("");
						logger.debug("");
						logger.debug("");
						logger.debug("------------------Job details-------------------");
						logger.debug("");
						logger.debug("[Job id   ]: " + id);
						logger.debug("[Job group]: " + group);
						scheduler.deleteJob(jobKey);
						logger.debug("");
						logger.debug("------------------- deleted --------------------");
						logger.debug("DONE.");
						logger.debug("");
						logger.debug("");
						logger.debug("");
						return Response.ok("{\"response\" : \"Job deleted successfully.\", \n\"Job id\" : \"" + id + "\"}",	MediaType.APPLICATION_JSON).build();
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not delete scheduled job" + e);
			return Response.serverError().build();
		}
		logger.error("[ERROR] Job with id " + id + " does not exist.");
		return Response.status(420).type(MediaType.APPLICATION_JSON).entity("{\"response\" : \"[ERROR] Job with id " + id + " does not exist.\"}").build();		
		//return Response.ok("{\"response\" : \"[ERROR] Job with id " + id + " does not exist.\"}", MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Pauses the given scheduled Job.
	 * 
	 * @param id
	 *            the id of the scheduled job that should get paused.
	 */
	public static Response pauseScheduledJob(String id) {
		SchedulerFactory schFactory = new StdSchedulerFactory();

		try {
			Scheduler scheduler = schFactory.getScheduler();
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					if (jobKey.toString().compareToIgnoreCase(group + "." + id) == 0) {
						logger.debug("");
						logger.debug("");
						logger.debug("");
						logger.debug("------------------Job details-------------------");
						logger.debug("");
						logger.debug("[Job id   ]: " + id);
						logger.debug("[Job group]: " + group);
						scheduler.pauseJob(jobKey);
						logger.debug("");
						logger.debug("------------------- paused --------------------");
						logger.debug("DONE.");
						logger.debug("");
						logger.debug("");
						logger.debug("");
						return Response
								.ok("{\"response\" : \"Job paused successfully.\", \n\"Job id\" : \"" + id + "\"}",
										MediaType.APPLICATION_JSON)
								.build();
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not pause scheduled job" + e);
			return Response.serverError().build();
		}
		logger.debug("[ERROR] Job with id " + id + " does not exist.");
		return Response
				.ok("{\"response\" : \"[ERROR] Job with id " + id + " does not exist.\"}", MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Resumes the given scheduled Job.
	 * 
	 * @param id
	 *            the id of the scheduled job that should get resumed.
	 */
	public static Response resumeScheduledJob(String id) {
		SchedulerFactory schFactory = new StdSchedulerFactory();

		try {
			Scheduler scheduler = schFactory.getScheduler();
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					if (jobKey.toString().compareToIgnoreCase(group + "." + id) == 0) {
						logger.debug("");
						logger.debug("");
						logger.debug("");
						logger.debug("------------------Job details-------------------");
						logger.debug("");
						logger.debug("[Job id   ]: " + id);
						logger.debug("[Job group]: " + group);
						scheduler.resumeJob(jobKey);
						logger.debug("");
						logger.debug("------------------- resumed --------------------");
						logger.debug("DONE.");
						logger.debug("");
						logger.debug("");
						logger.debug("");
						return Response
								.ok("{\"response\" : \"Job resumed successfully.\", \n\"Job id\" : \"" + id + "\"}",
										MediaType.APPLICATION_JSON)
								.build();
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not resume scheduled job" + e);
			return Response.serverError().build();
		}
		logger.debug("[ERROR] Job with id " + id + " does not exist.");
		return Response
				.ok("{\"response\" : \"[ERROR] Job with id " + id + " does not exist.\"}", MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Returns a list containing all currently executing jobs, if any.
	 * 
	 * @return a list containing all currently executing jobs.
	 */
	public static Response currentlyExecutingJobs() {
		SchedulerFactory schFactory = new StdSchedulerFactory();

		Scheduler scheduler = null;
		try {
			scheduler = schFactory.getScheduler();
			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("------------------ Currently Executing Jobs -------------------");
			logger.debug("");
			logger.debug("[Job group]: " + scheduler.getCurrentlyExecutingJobs().toString());
			logger.debug("");
			logger.debug("---------------------------- END ------------------------------");
			logger.debug("DONE.");
			logger.debug("");
			logger.debug("");
			logger.debug("");
			return Response
					.ok("{\"response\" : \"Currently Executing Jobs.\", \n\"Jobs\" : \""
							+ scheduler.getCurrentlyExecutingJobs().toString() + "\"}", MediaType.APPLICATION_JSON)
					.build();
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not retrieve currently running scheduled jobs" + e);
		}
		return null;
	}

	/**
	 * Checks if the scheduler has any scheduled job.
	 * 
	 * @return true if the scheduler has at least one scheduled job.
	 */
	@SuppressWarnings("unused")
	private static boolean hasScheduledJobs() {
		logger.debug("Checking if there are scheduled jobs.");
		SchedulerFactory schFactory = new StdSchedulerFactory();
		try {
			Scheduler scheduler = schFactory.getScheduler();
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					return true;
				}
			}
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not check if there are any scheduled jobs. " + e);
		}
		return false;
	}

	/**
	 * Gets details for the scheduled jobs.
	 * 
	 * @return the details for the scheduled jobs.
	 */
	public static Response getScheduledJobs() {
		boolean scheduledJobsExist = false;
		Map<String, Object> map = new HashMap<>();
		if (!hasScheduledJobs()) {
			map.put("Status", "No Scheduled Jobs.");
			return Response.ok(map, MediaType.APPLICATION_JSON).build();
		}
		final List<Object> objectJson = new ArrayList<>();

		SchedulerFactory schFactory = new StdSchedulerFactory();
		try {
			Scheduler scheduler = schFactory.getScheduler();
			final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					logger.debug("[Job key]: " + jobKey);
					TimeSchedule tmp = (TimeSchedule) scheduler.getJobDetail(jobKey).getJobDataMap()
							.get("timeSchedule");
					map = new HashMap<>();
					map.put("jobID", jobKey.getName());
					map.put("jobGroup", jobKey.getGroup());
					map.put("startTime", DATE_FORMAT.format(tmp.getStartTime()));
					map.put("frequency", tmp.getFrequency());
					map.put("timeUnit", tmp.getTimeUnit());
					objectJson.add(map);
					scheduledJobsExist = true;
				}
			}
			if (scheduledJobsExist)
				logger.debug("Scheduled Jobs exist: " + objectJson);
			else
				logger.debug("No Scheduled Jobs. ");

			return Response.ok(objectJson, MediaType.APPLICATION_JSON).build();
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not get scheduled jobs. " + e);
		}
		map.put("Status", "No Scheduled Jobs.");
		return Response.ok(map, MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Gets details for the scheduled job with the given id.
	 * 
	 * @return the details for the scheduled job with the given id.
	 */
	public static Map<String, Object> getScheduledJobMetadata(String id) {
		logger.debug("Get details for job with id " + id);
		final Map<String, Object> map = new HashMap<>();
		SchedulerFactory schFactory = new StdSchedulerFactory();
		try {
			Scheduler scheduler = schFactory.getScheduler();
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					if (jobKey.toString().compareToIgnoreCase(group + "." + id) == 0) {
						final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						TimeSchedule tmp = (TimeSchedule) scheduler.getJobDetail(jobKey).getJobDataMap()
								.get("timeSchedule");
						map.put("Job id", id);
						map.put("Job group", group);
						final Map<String, Object> ts = new HashMap<>();
						ts.put("startTime", DATE_FORMAT.format(tmp.getStartTime()));
						ts.put("frequency", tmp.getFrequency());
						ts.put("timeUnit", tmp.getTimeUnit());
						map.put("timeSchedule", ts);
						return map;
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not get scheduled job's metadata. " + e);
		}
		map.put("Status", "ERROR");
		map.put("Details", "Job with id " + id + " does not exist.");
		return map;
	}

	/**
	 * Deletes all the scheduled Jobs.
	 * 
	 */
	public static Response deleteAllScheduledJobs() {
		logger.debug("Shutting down all running scheduled jobs.");
		SchedulerFactory schFactory = new StdSchedulerFactory();
		try {
			Scheduler scheduler = schFactory.getScheduler();
			for (String group : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
					scheduler.deleteJob(jobKey);
				}
			}
			scheduler.shutdown();
		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not delete all scheduled jobs. " + e);
			return Response.serverError().build();
		}
		logger.debug("All Jobs successfully deleted.");
		logger.debug("DONE.");
		return Response.ok("{\"response\" : \"All Jobs deleted successfully.\"}", MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Initializes the scheduled Job and schedules its trigger event.
	 */
	public void scheduleJob() {
		try {
			JobDetail job = newJob(SubscribeToObservationImpl.class).withIdentity(id).build();
			job.getJobDataMap().put("sensorIDs", sensorIDs);
			job.getJobDataMap().put("timeSchedule", timeSchedule);
			job.getJobDataMap().put("securityKey", securityKey);

			Trigger trigger = buildTrigger(job);

			SchedulerFactory schFactory = new StdSchedulerFactory();
			Scheduler sch = schFactory.getScheduler();
			sch.scheduleJob(job, trigger);
			sch.start();

		} catch (SchedulerException e) {
			logger.error("[ERROR]: Could not initialize the scheduled job. " + e);
		}
	}

	/**
	 * Builds a Trigger for the scheduled job.
	 * 
	 * @param job
	 *            the scheduled job.
	 * @return returns a Trigger for the scheduled job
	 */
	private Trigger buildTrigger(JobDetail job) {
		// NOTE: Calendar's January is set to zero
		String schedule = null;

		Date startDate = timeSchedule.getStartTime();
		Calendar calStart = Calendar.getInstance();
		calStart.setTime(startDate);
		int startSec = calStart.get(Calendar.SECOND);
		int startMin = calStart.get(Calendar.MINUTE);
		int startHour = calStart.get(Calendar.HOUR_OF_DAY);
		int startDay = calStart.get(Calendar.DATE);
		int startMonth = calStart.get(Calendar.MONTH) + 1;
		int startYear = calStart.get(Calendar.YEAR);

		if (timeSchedule.hasStopTime()) {

			Date stopDate = timeSchedule.getStopTime();
			Calendar calStop = Calendar.getInstance();
			calStop.setTime(stopDate);
			int stopSec = calStop.get(Calendar.SECOND);
			int stopMin = calStop.get(Calendar.MINUTE);
			int stopHour = calStop.get(Calendar.HOUR_OF_DAY);
			int stopDay = calStop.get(Calendar.DATE);
			int stopMonth = calStop.get(Calendar.MONTH) + 1;
			int stopYear = calStop.get(Calendar.YEAR);

			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("------------------RUN SCHEDULED TASK------------------");
			logger.debug("");
			logger.debug("[Job id      ] :  " + id);
			logger.debug("[Start on    ] :  " + startDate);
			logger.debug("[Stop on     ] :  " + stopDate);
			logger.debug("[Fire trigger] :  " + "every " + timeSchedule.frequency + timeSchedule.timeUnit + ", repeat "
					+ timeSchedule.repeatCount + " times.");
			logger.debug("");
			logger.debug("----------------------END-----------------------------");
			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("");

			if (timeSchedule.timeUnit.compareToIgnoreCase("second") == 0) {
				return TriggerBuilder.newTrigger().withIdentity(id).startAt(timeSchedule.getStartTime())
						.withSchedule(
								SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(timeSchedule.frequency)
										.withRepeatCount(timeSchedule.repeatCount - 1))
						.endAt(timeSchedule.getStopTime()).build();
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("minute") == 0) {
				return TriggerBuilder.newTrigger().withIdentity(id).startAt(timeSchedule.getStartTime())
						.withSchedule(
								SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(timeSchedule.frequency)
										.withRepeatCount(timeSchedule.repeatCount))
						.endAt(timeSchedule.getStopTime()).build();
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("hour") == 0) {
				return TriggerBuilder.newTrigger().withIdentity(id)
						.startAt(timeSchedule.getStartTime()).withSchedule(SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInHours(timeSchedule.frequency).withRepeatCount(timeSchedule.repeatCount))
						.endAt(timeSchedule.getStopTime()).build();
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("day") == 0) {
				schedule = startSec + "-" + stopSec + " " + startMin + "-" + stopMin + " " + startHour + "-" + stopHour
						+ " " + startDay + "/" + timeSchedule.frequency + " " + startMonth + "-" + stopMonth + " ? "
						+ startYear + "-" + stopYear;
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("month") == 0) {
				schedule = startSec + "-" + stopSec + " " + startMin + "-" + stopMin + " " + startHour + "-" + stopHour
						+ " " + startDay + "-" + stopDay + " " + startMonth + "/" + timeSchedule.frequency + " ? "
						+ startYear + "-" + stopYear;
			} else {
				logger.error("[ERROR]: Failed to schedule the job.");
				return null;
			}

			return TriggerBuilder.newTrigger().forJob(job).startAt(timeSchedule.getStartTime())
					.withSchedule(CronScheduleBuilder.cronSchedule(schedule).withMisfireHandlingInstructionDoNothing())
					.endAt(timeSchedule.getStopTime()).build();
		} else {
			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("------------------RUN SCHEDULED TASK------------------");
			logger.debug("");
			logger.debug("[Job id      ] :  " + id);
			logger.debug("[Start on    ] :  " + startDate);
			logger.debug("[Fire trigger] :  " + "every " + timeSchedule.frequency + " " + timeSchedule.timeUnit + "s");
			logger.debug("");
			logger.debug("----------------------END-----------------------------");
			logger.debug("");
			logger.debug("");
			logger.debug("");
			logger.debug("");

			if (timeSchedule.timeUnit.compareToIgnoreCase("second") == 0) {
				return TriggerBuilder.newTrigger()
						.withIdentity(id).startAt(timeSchedule.getStartTime()).withSchedule(SimpleScheduleBuilder
								.simpleSchedule().withIntervalInSeconds(timeSchedule.frequency).repeatForever())
						.build();
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("minute") == 0) {
				return TriggerBuilder.newTrigger()
						.withIdentity(id).startAt(timeSchedule.getStartTime()).withSchedule(SimpleScheduleBuilder
								.simpleSchedule().withIntervalInMinutes(timeSchedule.frequency).repeatForever())
						.build();
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("hour") == 0) {
				return TriggerBuilder.newTrigger().withIdentity(id).startAt(timeSchedule.getStartTime())
						.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(timeSchedule.frequency)
								.repeatForever())
						.build();
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("day") == 0) {
				schedule = startSec + " " + startMin + " " + startHour + " " + startDay + "/" + timeSchedule.frequency
						+ " " + startMonth + " ? " + startYear;
			} else if (timeSchedule.timeUnit.compareToIgnoreCase("month") == 0) {
				schedule = startSec + " " + startMin + " " + startHour + " " + startDay + " " + startMonth + "/"
						+ timeSchedule.frequency + " ? " + startYear;
			} else {
				logger.error("[ERROR]: Failed to schedule the job.");
				return null;
			}

			return TriggerBuilder.newTrigger().forJob(job).startAt(timeSchedule.getStartTime())
					.withSchedule(CronScheduleBuilder.cronSchedule(schedule).withMisfireHandlingInstructionDoNothing())
					.build();
		}
	}
}
