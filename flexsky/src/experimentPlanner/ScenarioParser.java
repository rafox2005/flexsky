package experimentPlanner;

import data.StoreSafeAccount;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author rlibardi
 */
public class ScenarioParser {

    public static Scenario parse(String pathToXMLFile) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
        Scenario scenario = new Scenario();
        
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        builder = builderFactory.newDocumentBuilder();
        
        org.w3c.dom.Document document = builder.parse(new FileInputStream(pathToXMLFile));
        
        XPath xPath =  XPathFactory.newInstance().newXPath();
        
        //Get Scenario Name
        String expression = "/*/@name";
        scenario.name = xPath.compile(expression).evaluate(document);
        
        //Get Scenario replication
        expression = "/*/repeat";
        scenario.repeatNo = Integer.parseInt(xPath.compile(expression).evaluate(document));
        
        //Get Scenario DB
        expression = "/*/db";
        scenario.path_to_DB = xPath.compile(expression).evaluate(document);
        
        //Get Scenario DB
        expression = "/*/db/@resetOnStart";
        scenario.resetOnStartDB = Boolean.parseBoolean(xPath.compile(expression).evaluate(document));
        
        //Get Scenario Log DB
        expression = "/*/log_db";
        scenario.path_to_logDB = xPath.compile(expression).evaluate(document);
        
        //Get providers
        expression = "/*/providerList/account";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        
        for (int i = 0; i < nodeList.getLength(); i++) {
            String name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            StoreSafeAccount ssa = new StoreSafeAccount(name, name, name);     
                      
            NodeList provAttr = nodeList.item(i).getChildNodes();
            for (int j = 0; j < provAttr.getLength(); j++) {
                if (provAttr.item(j).getNodeName() == "type") ssa.setType(provAttr.item(j).getNodeValue());
                else if (provAttr.item(j).getNodeName() == "path") ssa.setPath(provAttr.item(j).getNodeValue());
                else if (provAttr.item(j).getNodeName() == "parameters")
                {
                    NodeList provParam = provAttr.item(j).getChildNodes();
                    HashMap map = new HashMap();
                    for (int k = 0; k < provParam.getLength(); k++) {
                        if (provParam.item(k).getNodeName() == "param")
                        {
                        String key = provParam.item(k).getAttributes().getNamedItem("key").getNodeValue();
                        String value = provParam.item(k).getAttributes().getNamedItem("value").getNodeValue();
                        map.put(key, value);
                        }
                    }
                    ssa.setAdditionalParameters(map);
                }
            }
            scenario.providerList.add(ssa);
        }
        
        
        
        //DEFAULT ERROR
        return null;
       
        
        

    }
}
