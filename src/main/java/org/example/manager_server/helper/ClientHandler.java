package org.example.manager_server.helper;

import com.google.gson.*;
import org.example.manager_client.helper.LoginHelper;
import org.example.shared.model.Account;
import org.example.shared.model.Department;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Gson gson;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JsonObject requestData = JsonParser.parseString(inputLine).getAsJsonObject();
                String action = requestData.has("action") ? requestData.get("action").getAsString() : "UNKNOWN";
                System.out.println("Nhận yêu cầu: " + action + " từ " + clientSocket.getInetAddress());
                JsonObject responseData = processRequest(action, requestData);
                out.println(gson.toJson(responseData));
            }
        }
        catch (Exception e) {
            System.err.println("Mất kết nối với Kiosk " + clientSocket.getInetAddress() + ": " + e.getMessage());
        }
        finally {
            closeSocket();
        }
    }
    private JsonObject processRequest(String action, JsonObject requestData) {
        JsonObject response = new JsonObject();
        response.addProperty("clientType", "server");
        response.addProperty("action", action);

        try {
            switch (action) {
                case "GET_DEPARTMENTS":
                    List<Department> departmentList = SQLHelper.getAllDepartments();

                    if(departmentList != null && !departmentList.isEmpty()) {
                        JsonElement depts = gson.toJsonTree(departmentList);
                        response.addProperty("status", "ok");
                        response.add("data", depts);
                        response.addProperty("message", "Lấy danh sách thành công");
                    }
                    else {
                        response.addProperty("status", "error");
                        response.add("data", new JsonArray());
                        response.addProperty("message", "Không tìm thấy đơn vị nào hoặc lỗi cơ sở dữ liệu.");
                    }
                    break;

                case "REQUEST_TICKET":
                    JsonObject data = requestData.getAsJsonObject("data");
                    String fullName = data.get("fullName").getAsString();
                    String nationalId = data.get("nationalId").getAsString();
                    LocalDateTime requestDate = LocalDateTime.parse(data.get("requestDate").getAsString());
                    UUID departmentId = UUID.fromString(data.get("departmentId").getAsString());

                    int ticketNumber = SQLHelper.getTicketNumber(fullName, nationalId, requestDate, departmentId);

                    response.addProperty("action", "REQUEST_TICKET");
                    response.addProperty("status", "ok");
                    response.addProperty("data", String.valueOf(ticketNumber));
                    response.addProperty("message", "Đã khởi tạo yêu cầu lấy số thành công.");
                    break;

                case "LOGIN":
                    JsonObject loginInfo = requestData.getAsJsonObject("data");
                    String userName = loginInfo.get("username").getAsString();
                    String hashedPwd = loginInfo.get("password").getAsString();
                    Account acc = SQLHelper.getAccounts(userName);
                    response.addProperty("action", "LOGIN");
                    response.addProperty("status", "ok");
                    if(acc != null && !acc.getLoginStatus() && acc.getHashedPwd().equals(hashedPwd)) {
                        response.addProperty("data", true);
                        acc.setLoginStatus(true);
                    }
                    else {
                        response.addProperty("data", false);
                    }
                    response.addProperty("message", "");
                    break;

                default:
                    response.addProperty("status", "error");
                    response.addProperty("message", "Hành động này không được hệ thống hỗ trợ.");
                    break;
            }
        }
        catch (Exception e) {
            response.addProperty("status", "error");
            response.addProperty("message", "Lỗi nội bộ máy chủ: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
        catch (Exception e) {
            System.err.println("Lỗi khi đóng socket của client: " + e.getMessage());
        }
    }
}