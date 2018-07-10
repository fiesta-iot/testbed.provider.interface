package ui.tpi.configurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiestaiot.commons.expdescriptiveids.model.ExpDescriptiveIDs;
import eu.fiestaiot.commons.expdescriptiveids.model.FemoDescriptiveID;
import eu.fiestaiot.commons.fedspec.model.FEDSpec;
import eu.fiestaiot.commons.fedspec.model.FEMO;
import eu.fiestaiot.commons.fedspec.model.FISMO;
//import eu.fiestaiot.experiment.erm.utils.Deserializer;
//import eu.fiestaiot.experiment.erm.utils.SecurityUtil;
import eu.fiestaiot.commons.util.PropertyManagement;

import org.w3c.dom.Document;

/**
 * @author Nikos Kefalakis (nkef) e-mail: nkef@ait.edu.gr
 *
 */
public class ErmClient {

	/**
	 * The ERM Services' URI
	 */
	private String ermServicesPath;

	/**
	 * The property management instance
	 */
	static PropertyManagement propertyManagement = new PropertyManagement();

	/**
	 * The logger
	 */
	final static Logger logger = LoggerFactory.getLogger(ErmClient.class);

	/**
	 * The default constructor
	 */
	public ErmClient() {
		ermServicesPath = propertyManagement.getErmServicesURI();
	}

	/**
	 * Prints the available services of the scheduler interface. Can be used to
	 * check that the scheduler service is alive.
	 * 
	 * @return the welcome message
	 */
	public String welcomeMessage() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		client.target(ermServicesPath);

		return "";
	}

	public String getALLUserExperimentsInXmlString(String userID) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(ermServicesPath).path("getALLUserExperiments");

		// Prepare the request
		target = target.queryParam("userID", userID);

		Response response = target.request().get();

		// Read output in string format
		String value = response.readEntity(String.class);

		logger.debug("FESpec XML: \n" + value);

		response.close();

		return value;

	}

	public ExpDescriptiveIDs getAllUserExperimentsDescreptions(String userID) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(ermServicesPath).path("getAllUserExperimentsDescreptions");

		// Prepare the request
		target = target.queryParam("userID", userID);

		Response response = target.request().get();

		// Read the entity
		ExpDescriptiveIDs expDescriptiveIDs = response.readEntity(ExpDescriptiveIDs.class);

		for (FemoDescriptiveID femoDescriptiveID : expDescriptiveIDs.getFemoDescriptiveID()) {

			logger.debug("femo Domain of Interes:");
			for (String domainOfInterest : femoDescriptiveID.getDomainOfInterest()) {
				logger.debug(domainOfInterest);
			}

			logger.debug("femo Description:" + femoDescriptiveID.getDescription());

		}

		return expDescriptiveIDs;

	}

	public FEMO getExperimentModelObjectEntity(String femoID) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(ermServicesPath).path("getExperimentModelObject");

		// Prepare the request
		target = target.queryParam("femoID", femoID);

		Response response = target.request().get();

		// Read the entity
		FEMO femo = response.readEntity(FEMO.class);

		logger.debug("femo ID:" + femo.getId());
		logger.debug("femo description:" + femo.getDescription());

		for (FISMO fismo : femo.getFISMO()) {
			logger.debug("fismo ID:" + fismo.getId());
			logger.debug("fismo description:" + fismo.getDescription());
		}

		response.close();

		return femo;

	}

	public FISMO getExperimentServiceModelObjectEntity(String fismoID) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(ermServicesPath).path("getExperimentServiceModelObject");

		// Prepare the request
		target = target.queryParam("fismoID", fismoID);

		Response response = target.request().get();

		// Read the entity
		FISMO fismo = response.readEntity(FISMO.class);

		logger.debug("fismo ID:" + fismo.getId());
		logger.debug("fismo description:" + fismo.getDescription());

		response.close();

		return fismo;

	}

	public String saveUserExperiments(FEDSpec fedSpec) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(ermServicesPath).path("saveUserExperiments");
		Response response = target.request(MediaType.TEXT_PLAIN).post(Entity.xml(fedSpec));

		// Read output in string format
		String reply = response.readEntity(String.class);

		logger.debug("Status: " + response.getStatus());
		logger.debug("Reply: " + reply);

		response.close();

		return reply;

	}

	public String saveUserExperiments(Document fedSpecXmlDocument) {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(ermServicesPath).path("saveUserExperiments");
		Response response = target.request(MediaType.TEXT_PLAIN).post(Entity.xml(fedSpecXmlDocument));

		// Read output in string format
		String reply = response.readEntity(String.class);

		logger.debug("Status: " + response.getStatus());
		logger.debug("Reply: " + reply);

		response.close();

		return reply;

	}

	// Deprecated
	public String saveFromFile1(String filepath) throws FileNotFoundException, Exception {

		File file = new File(filepath);

		// Build the xml document
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document xmlDocument = documentBuilder.parse(file);

		// pretty print the document
		printDocument(xmlDocument, System.out);

		// send the generated document to the ERM
		String reply = saveUserExperiments(xmlDocument);

		return reply;
	}

	public String saveFromFile2(String filepath) throws FileNotFoundException, Exception {

		FEDSpec fedSpec = null;
		try {

			File file = new File(filepath);
			JAXBContext jaxbContext = JAXBContext.newInstance(FEDSpec.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			fedSpec = (FEDSpec) jaxbUnmarshaller.unmarshal(file);
			System.out.println(fedSpec);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		// Save the experiment
		String reply = saveUserExperiments(fedSpec);

		return reply;
	}

	public String populateAndSaveTestUserExperiment() {

		// Generate Sample FEDSpec
		FEDSpec fedSpec;
		FEMO femo;
		FISMO fismo;

		// FISMO============================
		fismo = new FISMO();
		fismo.setId("sampleFismoID");
		fismo.setDescription("sampleServiceDescreption");

		// FEMO=============================

		femo = new FEMO();
		femo.setId("sampleFemoID");
		femo.setDescription("sampleExperimentDescreption");
		femo.getFISMO().add(fismo);

		// FEDSpec===========================
		fedSpec = new FEDSpec();
		fedSpec.setUserID("sampleUserID");
		fedSpec.getFEMO().add(femo);

		// Save the experiment
		String reply = saveUserExperiments(fedSpec);

		return reply;

	}

	public void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));

	}

}
