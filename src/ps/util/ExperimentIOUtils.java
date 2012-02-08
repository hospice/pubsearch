package ps.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ps.struct.Experiment;
import ps.struct.Item;
import ps.struct.Param;
import ps.struct.Session;

/**
 * Provides experiment configuration IO utilities.
 */
public class ExperimentIOUtils {

	/**
	 * Reads the configuration file.
	 */
	public static Experiment readConfiguration(String path) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		File configFile = new File(path);
		Document doc = dBuilder.parse(configFile);
		doc.getDocumentElement().normalize();
		Experiment exp = new Experiment();
		List<Session> sessions = new ArrayList<Session>();
		NodeList sessionNodeList = doc.getElementsByTagName("session");
		for (int a = 0; a < sessionNodeList.getLength(); a++) {
			// i. new session
			Session session = new Session();
			List<Item> items = new ArrayList<Item>();
			Node sessionNode = sessionNodeList.item(a);
			if (sessionNode.getNodeType() == Node.ELEMENT_NODE) {
				Element sessionElem = (Element) sessionNode;
				NodeList itemNodeList = sessionElem.getChildNodes();
				for (int x = 0; x < itemNodeList.getLength(); x++) {
					// ii. new item
					Item item = new Item();
					Node itemNode = itemNodeList.item(x);
					if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
						Element itemElem = (Element) itemNode;
						item.setName(getTagValue("name", itemElem));
						item.setOrder(Integer.parseInt(getTagValue("order", itemElem)));
						NodeList parametersNodeList = itemElem.getElementsByTagName("parameters");
						// iii. new parameters
						List<Param> parameters = new ArrayList<Param>();
						for (int b = 0; b < parametersNodeList.getLength(); b++) {
							Node parametersNode = parametersNodeList.item(b);
							if (parametersNode.getNodeType() == Node.ELEMENT_NODE) {
								Element parametersElem = (Element) parametersNode;
								NodeList paramNodeList = parametersElem.getChildNodes();
								for (int c = 0; c < paramNodeList.getLength(); c++) {
									Node paramNode = paramNodeList.item(c);
									if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
										Element paramElem = (Element) paramNode;
										// iv. new parameter
										Param param = new Param();
										param.setName(getTagValue("name", paramElem));
										param.setValue(getTagValue("value", paramElem));
										param.setMinValue(getTagValue("min-value", paramElem));
										param.setMaxValue(getTagValue("max-value", paramElem));
										param.setStep(getTagValue("step", paramElem));
										parameters.add(param);
									}
								}
							}
						}
						item.setParameters(parameters);
						items.add(item);
					}
				}
			}
			session.setItems(items);
			sessions.add(session);
		}
		exp.setSessions(sessions);
		return exp;
	}

	/**
	 * Returns the tag value.
	 */
	private static String getTagValue(String tag, Element elem) {
		try {
			return ((Node) (elem.getElementsByTagName(tag).item(0).getChildNodes()).item(0)).getNodeValue();
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Fetches all queries to be included in the current experiment.
	 */
	public static List<Integer> fetchAllQueryIds() throws ParserConfigurationException, IOException {
		List<Integer> l = new ArrayList<Integer>();
		String pathname = PropertyUtils.readProperty(ps.constants.GeneralConstants.QUERIES_FILE_PATH,
				ps.constants.GeneralConstants.EXPERIMENT_CONFIG_FILE_LOCATION);
		FileInputStream fis = new FileInputStream(pathname);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String line;
		while ((line = br.readLine()) != null) {
			l.add(Integer.parseInt(line));
		}
		dis.close();
		return l;
	}

	/**
	 * Prints the experiment data.
	 */
	public static void printExperimentData(Experiment exp) {
		int expCount = 0;
		for (Session s : exp.getSessions()) {
			System.out.println();
			System.out.println("********************************************");
			System.out.println();
			System.out.println("           [EXPERIMENT SESSION: " + ++expCount + "]");
			System.out.println();
			System.out.println("********************************************");
			System.out.println();
			int heurCount = 0;
			System.out.println("--------------------------------------------");
			for (Item i : s.getItems()) {
				System.out.println("             HEURISTIC ITEM: " + ++heurCount);
				System.out.println("--------------------------------------------");
				System.out.println("  Hierarchy Order  : " + i.getOrder());
				System.out.println("  Heuristic Name   : " + i.getName());
				System.out.println();
				System.out.println("  PARAMETERS:");
				List<Param> parameters = i.getParameters();
				if (parameters.size() == 0) {
					System.out.println("   (No parameters defined)");
				}
				for (Param p : parameters) {
					System.out.println("   * Name          : " + p.getName());
					System.out.println("      > Value      : " + p.getValue());
					System.out.println("      > Min-value  : " + p.getMinValue());
					System.out.println("      > Max-value  : " + p.getMaxValue());
					System.out.println("      > Step       : " + p.getStep());
				}
				System.out.println();
				System.out.println("--------------------------------------------");
			}
			System.out.println();
			System.out.println();
		}
		System.out.println();
	}

}
