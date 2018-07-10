package eu.fiestaiot.tpi.api.dms.service;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiestaiot.commons.util.PropertyManagement;

import java.util.Enumeration;

import javax.jms.*;

public class MessageBus {

	/**
	 * The logger's initialization.
	 */
	final static Logger logger = LoggerFactory.getLogger(MessageBus.class);

	/**
	 * The URL of the broker.
	 * 
	 * NOTE: The default broker for ActiveMQ is failover://tcp://localhost:61616
	 */
	public static String endpointURI;

	/**
	 * The name of the queue the broker will receive messages from
	 */
	public static String queueName;

	/**
	 * The username to get access TODO: hardcoded username, should change.
	 */
	private static String USER = "admin";

	/**
	 * The password to get access TODO: hardcoded password, should change.
	 */
	private static String PASSWORD = "admin";

	/**
	 * The property manager.
	 */
	PropertyManagement propertyManagement;

	/**
	 * The name of the broker's topic.
	 */
	static String topicName;

	/**
	 * The name of the broker's URI.
	 */
	static String brokerURI;

	/**
	 * Constructs a Messagebus instance.
	 */
	public MessageBus(String endpointURI, String queueName) {
		MessageBus.endpointURI = endpointURI;
		MessageBus.queueName = queueName;
		propertyManagement = new PropertyManagement();
		topicName = propertyManagement.getMessagebusDispatcherTopic();
		brokerURI = propertyManagement.getMessagebusDispatcherBrokerURI();
	}

	/**
	 * Pushes Observations to the MessageBus.
	 * 
	 * @param observationResult
	 *            the actual observation results.
	 * 
	 */
	public static void pushObservationsToMessageBus(String observationResult, String contentType) throws JMSException {
		logger.debug("Push data to MessageBus.");

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(endpointURI);
		Connection connection = connectionFactory.createConnection();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic(topicName);
		MessageProducer m = session.createProducer(destination);
		TextMessage observation = session.createTextMessage(observationResult);
		
		logger.debug("Sending the observations to the broker's queue.");
		logger.debug("Content-Type: " + contentType);

		observation.setJMSType(contentType);
		m.send(observation);
		connection.close();
		logger.debug("Message produced by: " + Thread.currentThread().getName());
		logger.debug("DONE.");
	}

	/**
	 * Receives the messages (here observations) and "consumes" them.
	 */
	public static void consumeMessageSync() {

		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageConsumer consumer = null;
		Message observation = null;

		try {
			logger.debug("Connect to MessageBus.");
			connection = createConnection();
			connection.start();

			logger.debug("Create Session to get the observations.");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			logger.debug("Get the Queue and the MessageConsumer.");
			destination = session.createQueue(queueName);
			consumer = session.createConsumer(destination);

			// NOTE: This call is by default blocking. The process will wait
			// here
			// for an observation to receive at the queue.
			// 3000 is set as a timeout.
			logger.debug("Wait until observation received.");
			observation = consumer.receive(3000);

			if (observation instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) observation;
				logger.debug(" Text of  message: " + textMessage.getText());
			}
		} catch (JMSException e) {
			logger.error("[ERROR]: " + e);
		} catch (Exception e) {
			logger.error("[ERROR]: Could not establish connection to the Message Broker. " + e);
		} finally {
			try {
				logger.debug("Close connection.");
				connection.close();
			} catch (JMSException e) {
				logger.error("[ERROR]: " + e);
			}
		}
		logger.debug("DONE.");
	}

	/**
	 * Consumes the messages that arrive at the Message Broker.
	 * 
	 */
	public static void consumeMessageAsync() {
		logger.debug("Preparing Message Broker.");

		Destination destination = null;
		ActiveMQConnection connection = null;

		try {
			logger.debug("Connect to MessageBus.");
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, endpointURI);
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			// DestinationSource destSource = connection.getDestinationSource();

			logger.debug("Create the Session Queue and the Broker Queue.");
			QueueSession queueSession = connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
			Queue queue = queueSession.createQueue(queueName);
			QueueBrowser browser = queueSession.createBrowser(queue);
			Enumeration<?> messagesInQueue = browser.getEnumeration();
			connection.start();

			logger.debug("Create the session to get the observations.");
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(queueName);
			MessageConsumer consumer = queueSession.createConsumer(destination);

			logger.debug("Get all messages (Async).");
			while (messagesInQueue.hasMoreElements()) {
				Message queueMessage = (Message) messagesInQueue.nextElement();

				if (queueMessage instanceof TextMessage) {
					Message msg = consumer.receive();
					TextMessage observation = (TextMessage) msg;
					logger.debug("");
					logger.debug("");
					logger.debug("");
					logger.debug("");
					logger.debug("------------------MESSAGE RECEIVED------------------");
					logger.debug("");
					logger.debug(" Text of  message: " + observation.getText());
					logger.debug("");
					logger.debug("----------------------END-----------------------------");
					logger.debug("");
					logger.debug("");
					logger.debug("");
					logger.debug("");
				}
			}
		} catch (JMSException e) {
			logger.error("[ERROR]: Could not consume message asynchronously. " + e);
		} finally {
			logger.debug("Close connection.");
			try {
				connection.close();
			} catch (JMSException e) {
			    logger.error("[ERROR]: Could not consume message asynchronously. " + e);
			}
		}
		logger.debug("DONE");
	}

	/**
	 * Creates the connection to the Message Broker.
	 *
	 * @return the Connection to the message broker.
	 */
	private static Connection createConnection() throws Exception {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(endpointURI);
		return connectionFactory.createConnection();
	}

	/**
	 * Gets the URL of the Message Broker.
	 * 
	 * @return the URL of the Message Broker.
	 */
	public static String getEndpointURI() {
		return endpointURI;
	}

	/**
	 * Gets the name of the Queue.
	 * 
	 * @return the name of the Queue.
	 */
	public static String getQueueName() {
		return queueName;
	}

	/**
	 * Gets the topic of the Messagebus.
	 * 
	 * @return the topic of the Messagebus.
	 */
	public static String getTopicName() {
		return topicName;
	}

	/**
	 * Sets the URL of the Message Broker.
	 * 
	 * @param endpointURI
	 *            the URI of the endpoint (Messagebus)
	 */
	public static void setMessageBusEndpointURL(String endpointURI) {
		MessageBus.endpointURI = endpointURI;
	}

}
