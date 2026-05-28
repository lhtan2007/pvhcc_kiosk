package org.example.kiosk_client.helper;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetworkInitializer {
    private String IP_ADDRESS = "127.0.0.1";
    private int PORT = 9999;
    private static NetworkInitializer instance;
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private final Gson gson;
    private boolean isConnected = false;
    private NetworkInitializer() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext context) {
                        return new JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                })
                .create();
    }
    public Gson getGson() {
        return gson;
    }
    public static synchronized NetworkInitializer getInstance() {
        if(instance == null) instance = new NetworkInitializer();
        return instance;
    }
    public synchronized boolean connect() {
        if(isConnected && clientSocket != null && clientSocket.isClosed()) {
            return true;
        }
        try {
            System.out.println("Đang kết nối đến server " + IP_ADDRESS + ":" + PORT + "...");
            this.clientSocket = new Socket(IP_ADDRESS, PORT);
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
    public synchronized void updateConfiguration(String newIpAddress, int newPort) {
        IP_ADDRESS = newIpAddress;
        PORT = newPort;
        this.closeConnection();
        this.connect();
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