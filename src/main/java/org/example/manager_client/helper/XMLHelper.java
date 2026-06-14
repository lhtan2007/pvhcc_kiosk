package org.example.manager_client.helper;

import com.google.gson.JsonObject;
import org.example.shared.model.CitizenRequest;
import org.example.shared.model.Department;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class XMLHelper {
    public static void exportDepartmentsToXML(List<Department> departments, File file){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElement("departments");
            doc.appendChild(rootElement);
            for(Department dept : departments){
                Element departmentElement = doc.createElement("department");
                rootElement.appendChild(departmentElement);
                Element nameElement = doc.createElement("name");
                nameElement.appendChild(doc.createTextNode(dept.getDepartmentName()));
                departmentElement.appendChild(nameElement);
                Element maxCtzRequestElement = doc.createElement("maxCtzRequest");
                maxCtzRequestElement.appendChild(doc.createTextNode(String.valueOf(dept.getMaxConcurrentRequestInDay())));
                departmentElement.appendChild(maxCtzRequestElement);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void importDepartmentsFromXML(File file){
        List<Department> departments = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("department");
            for(int i = 0; i < nList.getLength(); i++){
                Node n =  nList.item(i);
                if(n.getNodeType() == Node.ELEMENT_NODE){
                    Element e = (Element)n;
                    String id = e.getElementsByTagName("id").item(0).getTextContent();
                    String name = e.getElementsByTagName("name").item(0).getTextContent();
                    int maxCtzRequest
                            = Integer.parseInt(e.getElementsByTagName("maxCtzRequest").item(0).getTextContent());
                    Department dept = new Department();
                    dept.setDepartmentId(UUID.fromString(id));
                    dept.setDepartmentName(name);
                    dept.setMaxConcurrentRequestInDay(maxCtzRequest);
                    departments.add(dept);
                }
            }
            for(Department dept : departments){
                DataUpdater.addDepartmentFromXML(dept);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void exportCitizenRequestsToXML(Department dept, List<CitizenRequest> ctzRequests, File file){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElement("requests");
            doc.appendChild(rootElement);
            Element departmentsElement = doc.createElement("departments");
            departmentsElement.appendChild(doc.createTextNode(dept.getDepartmentName()));
            rootElement.appendChild(departmentsElement);
            for(CitizenRequest request : ctzRequests){
                Element ctzRequestElement = doc.createElement("request");
                rootElement.appendChild(ctzRequestElement);
                Element timeElement = doc.createElement("time");
                timeElement.appendChild(doc.createTextNode(String.valueOf(request.getRequestDate())));
                ctzRequestElement.appendChild(timeElement);
                Element nameElement = doc.createElement("name");
                nameElement.appendChild(doc.createTextNode(request.getFullName()));
                ctzRequestElement.appendChild(nameElement);
                Element nationalIdElement = doc.createElement("nationalId");
                nationalIdElement.appendChild(doc.createTextNode(request.getNationalId()));
                ctzRequestElement.appendChild(nationalIdElement);
                Element requestNumberElement = doc.createElement("requestNumber");
                requestNumberElement.appendChild(doc.createTextNode(String.valueOf(request.getRequestNumber())));
                ctzRequestElement.appendChild(requestNumberElement);
                Element statusElement = doc.createElement("status");
                statusElement.appendChild(doc.createTextNode(String.valueOf(request.getProcessStatus())));
                ctzRequestElement.appendChild(statusElement);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
