package org.example.kiosk_client.helper;

import com.google.gson.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetworkInitializer {
    private String IP_ADDRESS = "";
    private int PORT = 9999;
    private static NetworkInitializer instance;
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private final Gson gson;
    private boolean isConnected = false;
    private static final String CONFIG_FILE_PATH = "target/classes/NetworkConfig.xml";
    private void loadConfigurationFromXML() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (!configFile.exists()) {
                System.err.println("Không tìm thấy file " + CONFIG_FILE_PATH + ". Hệ thống sẽ dùng cấu hình mặc định (localhost:9999).");
                this.IP_ADDRESS = "127.0.0.1";
                this.PORT = 9999;
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);
            document.getDocumentElement().normalize();
            NodeList ipList = document.getElementsByTagName("server-ip");
            if (ipList.getLength() > 0) {
                this.IP_ADDRESS = ipList.item(0).getTextContent().trim();
            }
            NodeList portList = document.getElementsByTagName("server-port");
            if (portList.getLength() > 0) {
                this.PORT = Integer.parseInt(portList.item(0).getTextContent().trim());
            }
            System.out.println("Đọc cấu hình XML thành công! Server gốc: " + IP_ADDRESS + ":" + PORT);
        }
        catch (Exception e) {
            System.err.println("Lỗi khi đọc file cấu hình XML: " + e.getMessage());
            System.err.println("Hệ thống tự động chuyển về cấu hình dự phòng mạng nội bộ.");
            this.IP_ADDRESS = "127.0.0.1";
            this.PORT = 9999;
        }
    }
    private NetworkInitializer() {
        loadConfigurationFromXML();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext context) {
                        return new JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                })
                .create();
    }
    public String getIpAddress() {
        return IP_ADDRESS;
    }
    public int getPort() {
        return PORT;
    }
    public Gson getGson() {
        return gson;
    }
    public static synchronized NetworkInitializer getInstance() {
        if(instance == null) instance = new NetworkInitializer();
        return instance;
    }
    public synchronized boolean connect() {
        if(clientSocket != null && !clientSocket.isClosed()) {
            return true;
        }
        try {
            System.out.println("Đang kết nối đến server " + IP_ADDRESS + ":" + PORT + "...");
            this.clientSocket = new Socket();
            InetSocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS, PORT);
            int timeoutMs = 3000;
            this.clientSocket.connect(socketAddress, timeoutMs);
            this.clientOut = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            this.isConnected = true;
            System.out.println("Kết nối đến server thành công");
            return true;
        }
        catch(Exception e) {
            System.err.println("Kết nối thất bại: " + e.getMessage());
            this.isConnected = false;
            return false;
        }
    }
    public synchronized void updateConfiguration(String ip, int port) {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("configuration");
            document.appendChild(rootElement);
            Element ipElement = document.createElement("server-ip");
            ipElement.setTextContent(ip.trim());
            rootElement.appendChild(ipElement);
            Element portElement = document.createElement("server-port");
            portElement.setTextContent(String.valueOf(port).trim());
            rootElement.appendChild(portElement);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(configFile);
            transformer.transform(source, result);
            this.IP_ADDRESS = ip;
            this.PORT = port;
            System.out.println("Đã lưu và đồng bộ cấu hình XML thành công: " + ip + ":" + port);
            this.closeConnection();
        }
        catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng đã xảy ra khi ghi cấu hình ra file XML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private JsonObject createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("clientType", "kiosk");
        response.addProperty("action", "");
        response.addProperty("status", "error");
        response.addProperty("data", "");
        response.addProperty("message", message);
        return response;
    }
    public synchronized JsonObject sendRequest(JsonObject requestData) {
        if(!isConnected || clientSocket == null || clientSocket.isClosed()) {
            System.out.println("Kết nối không ổn định. Đang kết nối lại...");
            if(!connect()) {
                return createErrorResponse("Không thể kết nối đến server.");
            }
        }
        try {
            String jsonString = gson.toJson(requestData);
            clientOut.println(jsonString);
            String responseLine = clientIn.readLine();
            if(responseLine != null) {
                return JsonParser.parseString(responseLine).getAsJsonObject();
            }
            else {
                closeConnection();
                return createErrorResponse("Máy chủ đã ngắt kết nối đột ngột.");
            }
        }
        catch(Exception e) {
            System.err.println("Lỗi trong quá trình truyền dữ liệu: " + e.getMessage());
            closeConnection();
            return createErrorResponse("Lỗi đường truyền: " + e.getMessage());
        }
    }
    public synchronized void closeConnection() {
        try {
            if(clientOut != null) clientOut.close();
            if(clientIn != null) clientIn.close();
            if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            System.out.println("Đã đóng kết nối mạng an toàn");
        }
        catch(Exception e) {
            System.err.println("Lỗi khi giải phóng kết nối: " + e.getMessage());
        }
        finally {
            this.isConnected = false;
            this.clientSocket = null;
        }
    }
}