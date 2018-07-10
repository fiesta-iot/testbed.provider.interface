package ui.tpi.configurator;

import java.util.List;

public interface ScheduledJobService {

	/**
	 * Retrieve all scheduled jobs
	 * 
	 * @return 
	 * 		the list of scheduled jobs.
	 */
	public List<ScheduledJob> findAll();
	
	/**
	 * Adds a scheduled job
	 */
	public void addScheduledJob(ScheduledJob scheduledJob);
	
	/**
	 * Removes a scheduled job
	 */
	public void removeScheduledJob(ScheduledJob scheduledJob);
}
