package experimentPlanner;

import data.StorageOptions;
import data.DataAccount;
import data.DataFile;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pipeline.IPipeProcess;

/**
 *
 * @author rlibardi
 */
public class ScenarioParser {

    public static Scenario parse(String pathToXMLFile) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Scenario scenario = new Scenario();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        builder = builderFactory.newDocumentBuilder();

        org.w3c.dom.Document document = builder.parse(new FileInputStream(pathToXMLFile));

        XPath xPath = XPathFactory.newInstance().newXPath();

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

        //Get providers parameters
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            String name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            DataAccount ssa = new DataAccount(name, name, name);

            NodeList provAttr = nodeList.item(i).getChildNodes();
            for (int j = 0; j < provAttr.getLength(); j++)
            {
                if (provAttr.item(j).getNodeName() == "type")
                {
                    ssa.setType(provAttr.item(j).getFirstChild().getNodeValue());
                } else if (provAttr.item(j).getNodeName() == "path")
                {
                    ssa.setPath(provAttr.item(j).getFirstChild().getNodeValue());
                } else if (provAttr.item(j).getNodeName() == "parameters")
                {
                    NodeList provParam = provAttr.item(j).getChildNodes();
                    HashMap map = new HashMap();
                    for (int k = 0; k < provParam.getLength(); k++)
                    {
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

        //Get Operations
        expression = "/*/operationList/operation";
        nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

        //Get operations parameters
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            String action = nodeList.item(i).getAttributes().getNamedItem("action").getNodeValue();
            ScenarioOperation operation = new ScenarioOperation();
            operation.setAction(action);

            NodeList operAttr = nodeList.item(i).getChildNodes();
            for (int j = 0; j < operAttr.getLength(); j++)
            {
                if (operAttr.item(j).getNodeName() == "totalParts")
                {
                    operation.setTotalParts(Integer.parseInt(operAttr.item(j).getFirstChild().getNodeValue()));
                } else if (operAttr.item(j).getNodeName() == "reqParts")
                {
                    operation.setReqParts(Integer.parseInt(operAttr.item(j).getFirstChild().getNodeValue()));
                } else if (operAttr.item(j).getNodeName() == "idaMethod")
                {
                    operation.setIdaMethod(operAttr.item(j).getFirstChild().getNodeValue());
                } else if (operAttr.item(j).getNodeName() == "file")
                {
                    NodeList fileAttr = operAttr.item(j).getChildNodes();
                    DataFile ssf = new DataFile(null, 0);
                    ArrayList<IPipeProcess> filePipeline = new ArrayList();
                    ArrayList<IPipeProcess> slicePipeline = new ArrayList();
                    StorageOptions options = new StorageOptions();
                    
                    options.filePipeline = filePipeline;
                    options.slicePipeline = slicePipeline;
                    operation.setOptions(options);
                    
                    HashMap map = new HashMap();
                    

                    for (int k = 0; k < fileAttr.getLength(); k++)
                    {
                        if (fileAttr.item(k).getNodeName() == "path")
                        {
                            operation.setPathForFile(fileAttr.item(k).getFirstChild().getNodeValue());
                        } else if (fileAttr.item(k).getNodeName() == "revision")
                        {
                            ssf.setRevision(Integer.parseInt(fileAttr.item(k).getFirstChild().getNodeValue()));
                        } else if (fileAttr.item(k).getNodeName() == "type")
                        {
                            ssf.setType(fileAttr.item(k).getFirstChild().getNodeValue());
                        } else if (fileAttr.item(k).getNodeName() == "options")
                        {                            

                            NodeList optionsAttr = fileAttr.item(k).getChildNodes();

                            for (int m = 0; m < optionsAttr.getLength(); m++)
                            {
                                if (optionsAttr.item(m).getNodeName() == "filePipeline")
                                {
                                    NodeList filePipelineAttr = optionsAttr.item(m).getChildNodes();
                                    for (int l = 0; l < filePipelineAttr.getLength(); l++)
                                    {
                                        if (filePipelineAttr.item(l).getNodeName() == "pipe")
                                        {
                                            filePipeline.add((IPipeProcess) Class.forName(filePipelineAttr.item(l).getAttributes().getNamedItem("name").getNodeValue()).getConstructor().newInstance());
                                        }

                                    }

                                } else if (optionsAttr.item(m).getNodeName() == "slicePipeline")
                                {
                                    NodeList slicePipelineAttr = optionsAttr.item(m).getChildNodes();
                                    for (int l = 0; l < slicePipelineAttr.getLength(); l++)
                                    {
                                        if (slicePipelineAttr.item(l).getNodeName() == "pipe")
                                        {
                                            slicePipeline.add((IPipeProcess) Class.forName(slicePipelineAttr.item(l).getAttributes().getNamedItem("name").getNodeValue()).getConstructor().newInstance());
                                        }

                                    }

                                } else if (optionsAttr.item(m).getNodeName() == "parameters")
                                {
                                    NodeList fileParam = optionsAttr.item(m).getChildNodes();
                                    
                                    for (int l = 0; l < fileParam.getLength(); l++)
                                    {
                                        if (fileParam.item(l).getNodeName() == "param")
                                        {
                                            String key = fileParam.item(l).getAttributes().getNamedItem("key").getNodeValue();
                                            String value = fileParam.item(l).getAttributes().getNamedItem("value").getNodeValue();
                                            map.put(key, value);
                                        }
                                    }
                                    
                                }

                            }
                            
                        }
                    }
                    operation.setFile(ssf);
                    ssf.setOptions(options);
                    options.additionalParameters = map;

                }

            }
            scenario.operationList.add(operation);

        }
        return scenario;
    }

}
