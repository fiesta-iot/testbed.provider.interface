package eu.fiestaiot.tpi.api.dms.rest;

import java.util.Set;
import java.util.HashSet;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiestaiot.commons.util.PropertyManagement;
import eu.fiestaiot.tpi.api.dms.service.DatabaseDMS;
import eu.fiestaiot.tpi.api.dms.service.MessageBus;

/**
 * A class extending {@link Application} and annotated with @ApplicationPath is the Java EE 6 "no XML" approach to activating
 * JAX-RS.
 * 
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath} annotation.
 * </p>
 */
/**
 * @author Nikos Kefalakis (nkef) e-mail: nkef@ait.edu.gr
 * 
 */
@ApplicationPath("/rest")
public class JaxRsActivator extends Application {

	// Initialize the Logger
	final static Logger logger = LoggerFactory.getLogger(JaxRsActivator.class.getName());

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();

	public JaxRsActivator() {
		singletons.add(new TpiApiDataServicesRsControler());

		PropertyManagement propertyManagement = new PropertyManagement();

		String endpointURI = propertyManagement.getTpiApiMessagebusEndpointURI();
		String queueName = propertyManagement.getTpiApiMessagebusQueue();
		new MessageBus(endpointURI, queueName);

		new DatabaseDMS();
		DatabaseDMS.tableExists();
		if (DatabaseDMS.checkForJobsInDB() != 0) {
			DatabaseDMS.resumeScheduledJobs();
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		return empty;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

}
