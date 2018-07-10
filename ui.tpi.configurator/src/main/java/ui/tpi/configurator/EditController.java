package ui.tpi.configurator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.MaximizeEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.fiestaiot.commons.util.PropertyManagement;

public class EditController extends SelectorComposer<Component> {

	/**
	 * Random Serial Version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Wired variables with front-end
	 */
	@Wire
	private Window win;

	@Wire
	private Combobox testbedsCombobox;

	@Wire
	private Listbox resourcesListbox;

	@Wire
	private Listbox femosListbox;

	@Wire
	private Listbox selectedResources;

	@Wire
	private Combobox timeUnitCombobox;

	@Wire
	private Combobox securityKeyCombobox;

	@Wire
	private Textbox scheduleNameTextbox;

	@Wire
	private Combobox testbedURICombobox;

	@Wire
	private Intbox frequencyIntbox;

	@Wire
	private Textbox fileNameListbox;

	@Wire
	private Textbox startTimeTextbox;

	@Wire
	private Listbox scheduledJobsListbox;

	@Wire
	Tabs tbs;

	@Wire
	Tabpanels tps;

	/**
	 * The testbed
	 */
	private Testbed testbed;

	/**
	 * The schedule's name: input from textbox
	 */
	private String scheduleName;

	/**
	 * The schedule's timeunit: input from selection
	 */
	private String timeUnit;

	/**
	 * The schedule's security key: input from selection
	 */
	private String securityKey;

	/**
	 * The schedule's testbedURI: input from selection
	 */
	private String testbedURI;

	/**
	 * The schedule's endpointURI
	 */
	private String endpointURI;

	/**
	 * The schedule's startTime: input from textbox
	 */
	private String startTime;

	/**
	 * The schedule's frequency: input from intbox
	 */
	private int frequency = 5;

	/**
	 * The subscribeToObservationsURI service: read from configuration file
	 */
	private String subscribeToObservationsURI;

	/**
	 * The unsubscribeFromObservationsURI service: read from configuration file
	 */
	private String unsubscribeFromObservationsURI;
	
	/**
	 * The subscribeToObservationStreamURI service: read from configuration file
	 */
	private String subscribeToObservationStreamURI;
	
	/**
	 * The stopPushOfObservationsURI service: read from configuration file
	 */
	private String stopPushOfObservationsURI;
	
	/**
	 * The pushObservationsStreamProxyURI service: read from configuration file
	 */
	private String pushObservationsStreamProxyURI;

	/**
	 * The authentication token
	 */
	private String ssoToken;

	/**
	 * The testbed service
	 */
	private TestbedService testbedService = new TestbedServiceImpl();

	/**
	 * The list containing all scheduled jobs
	 */
	List<ScheduledJob> scheduledJobsAll = new ArrayList<ScheduledJob>();

	/**
	 * The scheduled job service
	 */
	private ScheduledJobService scheduledJobService = new ScheduledJobServiceImpl();

	/**
	 * The testbeds model
	 */
	private ListModel<String> testbedsModel;

	/**
	 * The resources model
	 */
	private ListModel<String> resourcesModel = new ListModelList<String>(TestbedData.getResources());

	/**
	 * The securityKey model
	 */
	private ListModel<String> securityKeyModel = new ListModelList<String>(TestbedData.getSecurityKeys());

	/**
	 * The testbedURI model
	 */
	private ListModel<String> testbedURIModel = new ListModelList<String>(TestbedData.getTestbedURIs());

	/**
	 * The timeunit model Note: the Fiesta portal will use the predefined values
	 * below
	 */
	private ListModel<String> timeUnitModel = new ListModelList<String>(
			Arrays.asList(new String[] { "second", "minute", "hour", "day", "month" }));

	/**
	 * The scheduled job model
	 */
	private ListModel<ScheduledJob> scheduledJobsModel;

	/**
	 * The resources in use model
	 */
	public static ArrayList<String> resourcesInUse = new ArrayList<String>();

	/**
	 * The tabs of the window
	 */
	List<?> tabs = new ArrayList<Object>();

	/**
	 * The ERM Client instance
	 */
	ErmClient ermClient = new ErmClient();

	/**
	 * The cookie's name
	 */
	private final static String IPLANETDIRECTORYPRO = "iPlanetDirectoryPro";

	/**
	 * The authenticationURL
	 */
	private String AUTHENTICATION_URL;

	/**
	 * The available Testbeds
	 */
	private List<Testbed> availableTestbeds;

	/**
	 * The user ID
	 */
	String fiestaUserID;

	/**
	 * The logger
	 */
	final static Logger logger = LoggerFactory.getLogger(EditController.class);
	
	int defaultConnectionTimeout = 30000;
	
	int minimumFrequencyInMinutes = 5;
	
	/**
	 * Default constructor
	 * 
	 * Initializes all variables need for the tpi configurator
	 */
	public EditController() {
		PropertyManagement propertyManagement = new PropertyManagement();
		endpointURI = propertyManagement.getMessagebusDispatcherBrokerURI();
		subscribeToObservationsURI = propertyManagement.getSubscribeToObservationsURI();
		pushObservationsStreamProxyURI = propertyManagement.getPushObservationsStreamProxyURI(); 
		unsubscribeFromObservationsURI = propertyManagement.getUnsubscribeFromObservationsURI();
		subscribeToObservationStreamURI = propertyManagement.getSubscribeToObservationStreamURI();
		stopPushOfObservationsURI = propertyManagement.getStopPushOfObservationsURI();
		minimumFrequencyInMinutes = Integer.parseInt(propertyManagement.getMinimumFrequency());		
		AUTHENTICATION_URL = propertyManagement.getAuthenticationURI();
		
		// Get userID
		ssoToken = getSSOTokenFromCookie();
		fiestaUserID = getUserByCookie(ssoToken);

		testbedsModel = new ListModelList<String>(testbedService.getIdsOfTestbeds().get(2));
		scheduledJobsModel = new ListModelList<ScheduledJob>(scheduledJobService.findAll());

		if (testbedService.findAll().size() != 0) {
			availableTestbeds = testbedService.findAll();

			testbed = new Testbed();
			copyTestbed(availableTestbeds.get(0), testbed);
			
			((ListModelList<String>) testbedsModel).addToSelection(testbed.getTestbedName());
			((ListModelList<String>) timeUnitModel).addToSelection(timeUnitModel.getElementAt(0));
			resourcesModel = new ListModelList<String>(testbed.getResources());
			securityKeyModel = new ListModelList<String>(testbed.getSecurityKeys());
			testbedURIModel = new ListModelList<String>(testbed.getTestbedURIs());
			((ListModelList<String>) testbedURIModel).addToSelection(testbedURIModel.getElementAt(0));	
					
			((ListModelList<String>) securityKeyModel).addToSelection("none");
			((ListModelList<String>) resourcesModel).setMultiple(true);
			
			
		} else {
			alert("No testbed found. Please try again.");
		}
	}

	@Override
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);
			testbedService.getAdditionalInformation(fiestaUserID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Copies variable values of testbeds
	 * 
	 * @param copyFrom
	 *            source testbed
	 * @param copyTo
	 *            destination testbed
	 */
	public void copyTestbed(Testbed copyFrom, Testbed copyTo) {
		copyTo.setId(copyFrom.getId());
		copyTo.setResources(copyFrom.getResources());
		copyTo.setSecurityKeys(copyFrom.getSecurityKeys());
		copyTo.setTestbedName(copyFrom.getTestbedName());
		copyTo.setTestbedURIs(copyFrom.getTestbedURIs());
	}

	/**
	 * Gets authentication token
	 */
	public String getSSOTokenFromCookie() {
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		String token = request.getParameter(IPLANETDIRECTORYPRO);

		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase(IPLANETDIRECTORYPRO)) {
				token = cookie.getValue();
			}
		}
		return token;
	}

	/**
	 * Listener that handles the widow's minimization functionality
	 */
	@Listen("onMinimize = #win")
	public void OnMinimizeWindow() {
		win.setWidth("20%");
		win.setHeight("45px");
		win.setMinimizable(false);
		win.setMaximizable(true);
		win.setVisible(true);
		win.invalidate();
	}

	/**
	 * Listener that handles the widow's "full screen" functionality
	 */
	@Listen("onMaximize = #win")
	public void OnMaximizeWindow(MaximizeEvent event) {
		event.stopPropagation();
		win.setMaximized(false);
		win.setWidth("99.4%");
		win.setHeight("100%");
		win.setMinimizable(true);
		win.setMaximizable(false);
		win.setVisible(true);
		win.invalidate();
	}

	/**
	 * Displays notifications
	 */
	private void showNotify(String msg, Component ref) {
		Clients.showNotification(msg, "info", ref, "end_center", 2000);
	}

	/**
	 * Listener for any change at the div with id testbedsCombobox. Updates the
	 * text for the schedule name
	 */
	@Listen("onChange = #testbedsCombobox")
	public void changeTestbeds() {
		String testbedName = testbedsCombobox.getValue();
		Set<String> resources = null;
		if (((ListModelList<String>) testbedsModel).contains(testbedName)) {
			// update info on second tab
			for (Testbed tb : availableTestbeds) {
				if (tb.getTestbedName().compareToIgnoreCase(testbedName) == 0) {
					if (tb.getResources() == null) {
						resources = new HashSet<String>(TestbedData.getAvailResources(testbedName, ssoToken));
						tb.setResources(resources);
					} else {
						resources = tb.getResources();
					}

					// Update testbed data
					testbed.setId(tb.getId());
					testbed.setResources(resources);
					testbed.setSecurityKeys(new HashSet<String>(tb.getSecurityKeys()));
					testbed.setTestbedName(tb.getTestbedName());
					testbed.setTestbedURIs(tb.getTestbedURIs());

					// Update testbed services at portal
					testbedURIModel = new ListModelList<String>(tb.getTestbedURIs());
					((ListModelList<String>) testbedURIModel).addToSelection(testbedURIModel.getElementAt(0));
					testbedURICombobox.setModel(testbedURIModel);

					// Update schedule name at portal
					scheduleName = "";
					scheduleNameTextbox.setValue(scheduleName);

					// Update security keys at portal
					securityKeyModel = new ListModelList<String>(TestbedData.getSecurityKeys());
					securityKeyModel = new ListModelList<String>(tb.getSecurityKeys());
					securityKeyCombobox.setModel(securityKeyModel);

					// Update start time at portal
					startTime = "";
					startTimeTextbox.setValue(startTime);
					break;
				}
			}

			testbed.setTestbedName(testbedName);
			resourcesModel = new ListModelList<String>(resources);
			((ListModelList<String>) resourcesModel).setMultiple(true);
			resourcesListbox.setModel(resourcesModel);

			ListModel<String> selectedResourcesModel = new ListModelList<String>();
			((ListModelList<String>) selectedResourcesModel).setMultiple(true);
			selectedResources.setModel(selectedResourcesModel);
		} else {
			showNotify("Unknow testbedName : " + testbedName, testbedsCombobox);
			logger.error("Unknow testbedName : " + testbedName);
		}
	}

	/**
	 * Listener for the button on first tab with id add Adds some selected
	 * resources to the selected resources section
	 */
	@Listen("onClick = #add")
	public void add() {
		Set<String> resources = ((ListModelList<String>) resourcesModel).getSelection();
		testbed.setResources(resources);

		if (resourcesListbox.getSelectedItem() == null) {
			showNotify("Select at least one resource first.", resourcesListbox);
		}
		while (resourcesListbox.getSelectedItem() != null) {
			resourcesListbox.getSelectedItem().setParent(selectedResources);
		}
	}

	/**
	 * Listener for the button on first tab with id addAll Adds resources to the
	 * selected resources section
	 */
	@Listen("onClick = #addAll")
	public void addAll() {
		while (resourcesListbox.getItemCount() != 0) {
			resourcesListbox.getItemAtIndex(0).setParent(selectedResources);
		}
	}

	/**
	 * Listener for the button on first tab with id addAll Adds resources to the
	 * selected resources section
	 */
	@Listen("onClick = #removeAll")
	public void removeAll() {
		while (selectedResources.getItemCount() != 0) {
			selectedResources.getItemAtIndex(0).setParent(resourcesListbox);
		}
	}

	/**
	 * Listener for the button on first tab with id remove Moves some resources
	 * from the selected resources section to the available resources section
	 */
	@Listen("onClick = #remove")
	public void release() {
		if (selectedResources.getSelectedItem() == null) {
			showNotify("Select at least one resource first.", selectedResources);
		}
		while (selectedResources.getSelectedItem() != null) {
			selectedResources.getSelectedItem().setParent(resourcesListbox);
		}
	}

	/**
	 * Listener for any change at the div with id resourceListbox Updates the
	 * selection for the resources
	 */
	@Listen("onSelect = #resourceListbox")
	public void changeResource() {

		Set<String> resources = ((ListModelList<String>) resourcesModel).getSelection();
		testbed.setResources(resources);

	}

	/**
	 * Listener for any change at the div with id scheduleNameTextbox Updates
	 * the text for the schedule name
	 */
	@Listen("onChange = #scheduleNameTextbox")
	public void changeTitle() {
		scheduleName = scheduleNameTextbox.getValue();
	}

	/**
	 * Listener for any change at the div with id timeUnitCombobox Updates the
	 * text for the schedule's timeunit
	 */
	@Listen("onChange = #timeUnitCombobox")
	public void changeTimeUnit() {
		timeUnit = timeUnitCombobox.getValue();
	}

	/**
	 * Listener for any change at the div with id securityKeyCombobox Updates
	 * the text for the schedule's security key
	 */
	@Listen("onChange = #securityKeyCombobox")
	public void changeSecurityKey() {
		securityKey = securityKeyCombobox.getValue();
	}

	/**
	 * Listener for any change at the div with id testbedURICombobox Updates the
	 * text for the schedule's testbedURI
	 */
	@Listen("onChange = #testbedURICombobox")
	public void changeTestbedURI() {
		testbedURI = testbedURICombobox.getValue();
		//TODO: Needs to be tested
		
		if(TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("pushLastObservations")) 
		{
			startTimeTextbox.setVisible(false);
			frequencyIntbox.setVisible(false);
			timeUnitCombobox.setVisible(false);		
		}
		else if(TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getLastObservations") || TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getObservations")){
			//if(TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getLastObservations") ){
				frequencyIntbox.setValue(minimumFrequencyInMinutes);
				timeUnitCombobox.setValue("minute");
			//}
			startTimeTextbox.setVisible(true);
			frequencyIntbox.setVisible(true);
			timeUnitCombobox.setVisible(true);
		}	
		else{
			//TODO: update the if statements
		}
	}

	/**
	 * Listener for any change at the div with id frequencyIntbox Updates the
	 * text for the schedule's frequency
	 */
	@Listen("onChange = #frequencyIntbox")
	public void changeFrequency() {
		frequency = frequencyIntbox.getValue();
	}

	/**
	 * Listener for any change at the div with id startTimeTextbox Updates the
	 * text for the schedule's start time
	 */
	@Listen("onChange = #startTimeTextbox")
	public void changeStart() {
		startTime = startTimeTextbox.getValue();
	}

	/**
	 * Listener of button with id btn Changes the view to other tab on click
	 */
	@Listen("onClick = #btn")
	public void onClick$btn() {
		((Tab) ((Tab) tbs.getFirstChild()).getNextSibling()).setSelected(true);
		 changeTestbedURI();
	}
	
	/**
	 * Listener of button with id btn Changes the view to other tab on click
	 */
	@Listen("onClick = #scheduleTab")
	public void onClickScheduleTab() {
		 changeTestbedURI();
	}

	/**
	 * Listener of button with id schedule Changes the view to othe tab on
	 * click. Sends a schedule to the DMS services named
	 * /subscribeToObservations and adds the scheduled job to the list for
	 * scheduled jobs.
	 */
	@Listen("onClick = #schedule")
	public void submit() {
		List<Listitem> resources = selectedResources.getItems();
		List<String> sensorIDs = new ArrayList<String>();

		for (int i = 0; i < resources.size(); i++) {
			sensorIDs.add(resources.get(i).getValue().toString());
		}

		testbedURI = testbedURICombobox.getValue();
		resourcesInUse.addAll(sensorIDs);
		
		if (securityKey == null) {
			securityKey = "none";
		}

		if (resources.isEmpty()) {
			alert("No selected resources.");
			return;
		} else if (testbedURI == null || testbedURI.compareTo("") == 0) {
			alert("Please provide a testbed service.");
			return;
		} else if (endpointURI == null || endpointURI.compareTo("") == 0) {
			alert("No endpoint found in the configuration file.");
			return;
		} else if (scheduleName == null || scheduleName.compareToIgnoreCase("") == 0) {
			alert("The name cannot be empty.");
			return;
		} else {
		}
		
				
		if (TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getLastObservations") || TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getObservations")) {/*Mapping of testbedURIs*/
			if (frequency == 0) {
				alert("The frequency cannot be zero.");
				return;
			}

			if (startTime == null) {
				startTime = "";
			}

			if (timeUnit == null) {
				timeUnit = "minute";
			}
			
			if("second".compareTo(timeUnit)==0 && frequency<(minimumFrequencyInMinutes*60) || "minute".compareTo(timeUnit)==0 && frequency<minimumFrequencyInMinutes){
				alert("The frequency needs to be greater than " + minimumFrequencyInMinutes + " minutes.");
				return;
			}

			
			
			
			
			HttpClient client = HttpClients.createDefault();
			final HttpPost request = new HttpPost(subscribeToObservationsURI);
			Map<String, Object> payload = new HashMap<String, Object>();
			Map<String, Object> ts = new HashMap<String, Object>();

			payload.put("sensorIDs", sensorIDs);
			payload.put("testbedURI", testbedURI);
			payload.put("endpointURI", endpointURI);
			payload.put("securityKey", securityKey);
			payload.put("scheduleName", scheduleName);

			String userID = getUserByCookie(ssoToken);
			payload.put("userID", userID);

			ts.put("startTime", startTime);
			ts.put("frequency", new Integer((int) frequency));
			ts.put("timeUnit", timeUnit);
			payload.put("timeSchedule", ts);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String params;

			try {
				int timeoutForConnection = defaultConnectionTimeout;
				
				params = new String(objectMapper.writeValueAsString(payload));
				
				request.addHeader("content-type", "application/json");
				request.addHeader("iPlanetDirectoryPro", ssoToken);
				request.setEntity(new StringEntity(params));
				final RequestConfig config = RequestConfig.custom().setSocketTimeout(timeoutForConnection).setConnectTimeout(timeoutForConnection)
						.setConnectionRequestTimeout(timeoutForConnection).build();
				request.setConfig(config);

				final HttpResponse response = client.execute(request);
				final int sc = response.getStatusLine().getStatusCode();

				if (sc != HttpStatus.SC_OK) {
					alert("Failed to schedule the task [ status code: " + sc + " ].");
					logger.error("Failed to schedule the task [ status code: " + sc + " ].");
				} else {
					ResponseHandler<String> handler = new BasicResponseHandler();
					String body = handler.handleResponse(response);

					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = mapper.readValue(body, new TypeReference<HashMap<String, Object>>() {
					});
					String jobID = (String) map.get("jobID");
					scheduledJobService.addScheduledJob(new ScheduledJob(sensorIDs.size() + " resources in use",
							testbedURI, sensorIDs, jobID, scheduleName));

					scheduledJobsModel = new ListModelList<ScheduledJob>(scheduledJobService.findAll());
					scheduledJobsListbox.setModel(scheduledJobsModel);

					// clear all
					resourcesModel = new ListModelList<String>(
							TestbedData.getAvailResources(testbed.getTestbedName(), ssoToken));
					((ListModelList<String>) resourcesModel).setMultiple(true);
					resourcesListbox.setModel(resourcesModel);
					setScheduleName("");
					scheduleNameTextbox.setValue(scheduleName);

					// Delete selected resources if testbed changes
					ListModel<String> selectedResourcesModel = new ListModelList<String>();
					((ListModelList<String>) selectedResourcesModel).setMultiple(true);
					selectedResources.setModel(selectedResourcesModel);
					((Tab) ((Tab) ((Tab) tbs.getFirstChild()).getNextSibling()).getNextSibling()).setSelected(true);
					Messagebox.show("Job successfully scheduled!");
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				alert("Failed to schedule job.");
			} catch (JsonProcessingException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				alert("Failed to schedule job.");
			} catch (ClientProtocolException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				alert("Failed to schedule job.");
			} catch (IOException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
				alert("Failed to schedule job.");
			}

		} else if (TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("pushLastObservations")) { /*Mapping of testbedURIs*/
			// TODO: check the functionlity of the pushLastObservations			
			if(pushObservationsStreamProxyURI.compareToIgnoreCase("")==0){ //check this functionality
				alert("Failed to schedule job.");
				logger.error("Failed to schedule job.");
				return;
			}
						
			HttpClient client1 = HttpClients.createDefault();
			final HttpPost request1 = new HttpPost(subscribeToObservationStreamURI);
			Map<String, Object> payload1 = new HashMap<String, Object>();
			
			payload1.put("sensorIDs", sensorIDs);
			payload1.put("testbedURI", testbedURI);
			payload1.put("endpointURI", pushObservationsStreamProxyURI);
			payload1.put("securityKey", securityKey); 
			payload1.put("scheduleName", scheduleName);
			String userID1 = getUserByCookie(ssoToken);
			payload1.put("userID", userID1);

			ObjectMapper objectMapper1 = new ObjectMapper();
			objectMapper1.enable(SerializationFeature.INDENT_OUTPUT);
			String params1;

			try {				
				params1 = new String(objectMapper1.writeValueAsString(payload1));
				request1.addHeader("content-type", "application/json");
				request1.addHeader("iPlanetDirectoryPro", ssoToken);
				request1.setEntity(new StringEntity(params1));
				final RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
						.setConnectionRequestTimeout(30000).build();
				request1.setConfig(config);

				final HttpResponse response = client1.execute(request1);
				final int sc = response.getStatusLine().getStatusCode();
				
				if (sc != HttpStatus.SC_OK) {
					alert("Failed to schedule the task [ status code: " + sc + " ].");
					logger.error("Failed to schedule the task [ status code: " + sc + " ].");
				} else {
					ResponseHandler<String> handler = new BasicResponseHandler();
					String body = handler.handleResponse(response);
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = mapper.readValue(body, new TypeReference<HashMap<String, Object>>() {
					});
					String jobID = (String) map.get("jobID");
					String testbedURI = (String) map.get("stopPushOfObservations");
					
					TestbedData.allTestbedUriMappings.put(testbedURI,"stopPushOfObservations");
					//testbed.getTestbedUriMapping().put(testbedURI,"stopPushOfObservations");
					
					scheduledJobService.addScheduledJob(new ScheduledJob(sensorIDs.size()+ " resources in use", testbedURI, sensorIDs, jobID, scheduleName));
					scheduledJobsModel = new ListModelList<ScheduledJob>(scheduledJobService.findAll());
					scheduledJobsListbox.setModel(scheduledJobsModel);

					// clear all
					resourcesModel = new ListModelList<String>(
							TestbedData.getAvailResources(testbed.getTestbedName(), ssoToken));
					((ListModelList<String>) resourcesModel).setMultiple(true);
					resourcesListbox.setModel(resourcesModel);
					setScheduleName("");
					scheduleNameTextbox.setValue(scheduleName);

					// Delete selected resources if testbed changes
					ListModel<String> selectedResourcesModel = new ListModelList<String>();
					((ListModelList<String>) selectedResourcesModel).setMultiple(true);
					selectedResources.setModel(selectedResourcesModel);
					((Tab) ((Tab) ((Tab) tbs.getFirstChild()).getNextSibling()).getNextSibling()).setSelected(true);
					Messagebox.show("Job successfully scheduled!");
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
			} catch (JsonProcessingException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
			} catch (ClientProtocolException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
			} catch (IOException e) {
				logger.error("Could not schelude job. ");
				logger.error("" + e);
			}
		} else {
			/*ToDo: add functionality*/
		}

	}

	/**
	 * Get the user ID from cookie-token
	 */
	private String getUserByCookie(String SSOtoken) {
		/**
		 * Create a Jersey client to GET the user id information from the
		 * Session using a POST call to the OpenAM idm API.
		 */
		Client client = Client.create();
		WebResource webResourceOpenAM = client.resource(AUTHENTICATION_URL);

		ClientResponse responseAuth = webResourceOpenAM.type("application/json").header(IPLANETDIRECTORYPRO, SSOtoken)
				.post(ClientResponse.class, "{}");
		if (responseAuth.getStatus() != 200) {
			logger.error("Could not retrieve User ID by cookie. ");
			throw new RuntimeException("Failed : HTTP error code : " + responseAuth.getStatus());
		}
		String userObject = responseAuth.getEntity(String.class);
		ObjectMapper objectUser = new ObjectMapper();
		JsonNode userNode;
		String id = null;
		try {
			userNode = objectUser.readValue(userObject, JsonNode.class);
			id = userNode.get("id").asText();
		} catch (JsonParseException e) {
			logger.error("Could not retrieve User ID by cookie. ");
			logger.error("" + e);
		} catch (JsonMappingException e) {
			logger.error("Could not retrieve User ID by cookie. ");
			logger.error("" + e);
		} catch (IOException e) {
			logger.error("Could not retrieve User ID by cookie. ");
			logger.error("" + e);
		}
		return id;
	}

	/**
	 * Listener of button with id stop Sends a payload to the DMS services named
	 * /unsubscribeFromObservations and removes the related the scheduled job to
	 * the list for scheduled jobs.
	 */
	@Listen("onClick = #stop")
	public void stop() {
		if (scheduledJobsListbox.getSelectedItem() == null) {
			alert("Please select a scheduled job first.");
			return;
		} else {
			testbedURI = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getTestbedURI();

			if (testbedURI == null) {
				alert("Please select a scheduled job first.");
				return;
			}
			
			if (TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getLastObservations") || TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("getObservations")) {/*Mapping of testbedURIs*/
				List<String> sensorIDsSTOP = new ArrayList<String>();
				String testbedURISTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex())
						.getTestbedURI();
				sensorIDsSTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getSensorIDs();
				String jobIDSTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getJobID();

				HttpClient client = HttpClients.createDefault();
				final HttpPost request = new HttpPost(unsubscribeFromObservationsURI);
				request.addHeader("iPlanetDirectoryPro", ssoToken);

				Map<String, Object> payload = new HashMap<String, Object>();
				payload.put("sensorIDs", sensorIDsSTOP);
				payload.put("testbedURI", testbedURISTOP);
				payload.put("jobID", jobIDSTOP);

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				String params;

				try {
					params = new String(objectMapper.writeValueAsString(payload));
					request.addHeader("content-type", "application/json");
					request.setEntity(new StringEntity(params));

					final RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
							.setConnectionRequestTimeout(30000).build();
					request.setConfig(config);

					HttpResponse response = client.execute(request);
					int sc = response.getStatusLine().getStatusCode();

					if (sc != HttpStatus.SC_OK) {
						Messagebox.show("Failed to stop the task [ status code: " + sc + " ].");
						logger.error("Failed to stop the task [ status code: " + sc + " ].");
					} else {
						ResponseHandler<String> handler = new BasicResponseHandler();
						String body = handler.handleResponse(response);

						ObjectMapper mapper = new ObjectMapper();
						String jsonInString = mapper.writeValueAsString(body);

						int indexOfScheduledJob = scheduledJobsListbox.getSelectedIndex();

						scheduledJobService.removeScheduledJob(scheduledJobsModel.getElementAt(indexOfScheduledJob));
						scheduledJobsListbox.removeItemAt(indexOfScheduledJob);
						scheduledJobsModel = new ListModelList<ScheduledJob>(scheduledJobService.findAll());
						scheduledJobsListbox.setModel(scheduledJobsModel);

						// update resources - release resources
						resourcesInUse.removeAll(sensorIDsSTOP);
						resourcesModel = new ListModelList<String>(
								TestbedData.getAvailResources(testbed.getTestbedName(), ssoToken));
						((ListModelList<String>) resourcesModel).setMultiple(true);
						resourcesListbox.setModel(resourcesModel);
						Messagebox.show("Job successfully deleted.");

						testbedURISTOP = "";
						sensorIDsSTOP = null;
						jobIDSTOP = "";
						response = null;
						request.releaseConnection();
					}
				} catch (UnsupportedEncodingException e) {
					logger.error("Encoding is not supported. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				} catch (JsonProcessingException e) {
					logger.error("Failed to stop scheduled job. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				} catch (ClientProtocolException e) {
					logger.error("Failed to stop scheduled job. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				} catch (IOException e) {
					logger.error("Failed to stop scheduled job. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				}
			} else if (TestbedData.allTestbedUriMappings.get(testbedURI).endsWith("stopPushOfObservations")) { /*Mapping of testbedURIs*//* || testbed.getTestbedUriMapping().get(testbedURI).endsWith("stopPushOfObservations")*/ /*stopPushOfObservations = the tps service URI*/
				// TODO: check the functionlity of the stopPushOfObservations
				List<String> sensorIDsSTOP = new ArrayList<String>();				
				String testbedURISTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getTestbedURI();
				String endpointURISTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getEndpointURI();
				sensorIDsSTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getSensorIDs();			
				String jobIDSTOP = scheduledJobsModel.getElementAt(scheduledJobsListbox.getSelectedIndex()).getJobID();
				
				HttpClient client = HttpClients.createDefault();
				final HttpPost request = new HttpPost(stopPushOfObservationsURI); /*stopPushOfObservationsURI = the dms service URI*/
				request.addHeader("iPlanetDirectoryPro", ssoToken);
				Map<String, Object> payload = new HashMap<String, Object>();
				payload.put("testbedURI", testbedURISTOP);
				payload.put("sensorIDs", sensorIDsSTOP);
				payload.put("endpointURI", endpointURISTOP);
				payload.put("jobID", jobIDSTOP);

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				String params;

				try {
					params = new String(objectMapper.writeValueAsString(payload));
					request.addHeader("content-type", "application/json");
					request.setEntity(new StringEntity(params));

					final RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
							.setConnectionRequestTimeout(30000).build();
					request.setConfig(config);

					HttpResponse response = client.execute(request);
					int sc = response.getStatusLine().getStatusCode();
					
					if (sc != HttpStatus.SC_OK) {
						Messagebox.show("Failed to stop the task [ status code: " + sc + " ].");
						logger.error("Failed to stop the task [ status code: " + sc + " ].");
					} else {
						ResponseHandler<String> handler = new BasicResponseHandler();
						String body = handler.handleResponse(response);
						
						ObjectMapper mapper = new ObjectMapper();
						String jsonInString = mapper.writeValueAsString(body);
						
						int indexOfScheduledJob = scheduledJobsListbox.getSelectedIndex();
						
						scheduledJobService.removeScheduledJob(
						scheduledJobsModel.getElementAt(indexOfScheduledJob));
						scheduledJobsListbox.removeItemAt(indexOfScheduledJob);					
						scheduledJobsModel = new ListModelList<ScheduledJob>(scheduledJobService.findAll());
						scheduledJobsListbox.setModel(scheduledJobsModel);
						
						// update resources - release resources
						resourcesInUse.removeAll(sensorIDsSTOP);
						resourcesModel = new ListModelList<String>(
								TestbedData.getAvailResources(testbed.getTestbedName(), ssoToken));
						((ListModelList<String>) resourcesModel).setMultiple(true);
						resourcesListbox.setModel(resourcesModel);					
						Messagebox.show("Job successfully deleted.");

						testbedURISTOP="";
						sensorIDsSTOP=null;
						jobIDSTOP="";
						response = null;
						request.releaseConnection();
					}
				} catch (UnsupportedEncodingException e) {
					logger.error("Encoding is not supported. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				} catch (JsonProcessingException e) {
					logger.error("Failed to stop scheduled job. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				} catch (ClientProtocolException e) {
					logger.error("Failed to stop scheduled job. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				} catch (IOException e) {
					logger.error("Failed to stop scheduled job. ");
					logger.error("" + e);
					alert("Failed to stop scheduled job.");
				}			
			} else {
				/*ToDo: add functionality*/
			}
		}

	}
	/*---------Getter and Setter Methods---------*/

	/**
	 * Getter method for scheduledJobsListbox
	 */
	public Listbox getScheduledJobsListbox() {
		return scheduledJobsListbox;
	}

	/**
	 * Setter method for scheduledJobsListbox
	 */
	public void setScheduledJobsListbox(Listbox scheduledJobsListbox) {
		this.scheduledJobsListbox = scheduledJobsListbox;
	}

	/**
	 * Getter method for testbed
	 */
	public Testbed getTestbed() {
		return testbed;
	}

	/**
	 * Getter method for scheduleName
	 */
	public String getScheduleName() {
		return scheduleName;
	}

	/**
	 * Setter method for scheduleName
	 */
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	/**
	 * Getter method for testbedsModel
	 */
	public ListModel<String> getTestbedsModel() {
		return testbedsModel;
	}

	/**
	 * Getter method for timeUnitModel
	 */
	public ListModel<String> getTimeUnitModel() {
		return timeUnitModel;
	}

	/**
	 * Getter method for resourcesModel
	 */
	public ListModel<String> getResourcesModel() {
		return resourcesModel;
	}

	/**
	 * Getter method for endpointURI
	 */
	public String getEndpointURI() {
		return endpointURI;
	}

	/**
	 * Getter method for securityKeyModel
	 */
	public ListModel<String> getSecurityKeyModel() {
		return securityKeyModel;
	}

	/**
	 * Getter method for testbedURIModel
	 */
	public ListModel<String> getTestbedURIModel() {
		return testbedURIModel;
	}

	/**
	 * Setter method for testbedURIModel
	 */
	public void setTestbedURIModel(ListModel<String> testbedURIModel) {
		this.testbedURIModel = testbedURIModel;
	}

	/**
	 * Setter method for securityKeyModel
	 */
	public void setSecurityKeyModel(ListModel<String> securityKeyModel) {
		this.securityKeyModel = securityKeyModel;
	}

	/**
	 * Setter method for endpointURI
	 */
	public void setEndpointURI(String endpointURI) {
		this.endpointURI = endpointURI;
	}

	/**
	 * Getter method for startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Setter method for startTime
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * Getter method for frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * Setter method for frequency
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	/**
	 * Getter method for testbedURI
	 */
	public String getTestbedURI() {
		return testbedURI;
	}

	/**
	 * Setter method for testbedURI
	 */
	public void setTestbedURI(String testbedURI) {
		this.testbedURI = testbedURI;
	}

	/**
	 * Getter method for scheduledJobsModel
	 */
	public ListModel<ScheduledJob> getScheduledJobsModel() {
		return scheduledJobsModel;
	}

	/**
	 * Setter method for scheduledJobsModel
	 */
	public void setScheduledJobsModel(ListModel<ScheduledJob> scheduledJobsModel) {
		this.scheduledJobsModel = scheduledJobsModel;
	}

	/**
	 * Getter method for the token
	 */
	public String getSsoToken() {
		return ssoToken;
	}

	/**
	 * Setter method for the token
	 */
	public void setSsoToken(String ssoToken) {
		this.ssoToken = ssoToken;
	}
}
