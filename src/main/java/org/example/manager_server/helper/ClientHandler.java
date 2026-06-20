package org.example.manager_server.helper;

import com.google.gson.*;
import org.example.shared.model.Account;
import org.example.shared.model.CitizenRequest;
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

                case "ADD_DEPARTMENT":
                    JsonObject deptData = requestData.get("data").getAsJsonObject();
                    if(deptData != null) {
                        String deptName = deptData.get("deptName").getAsString();
                        int maxConcurrentCtz = deptData.get("maxConcurrentCtz").getAsInt();
                        boolean isCompleted = SQLHelper.addDepartment(deptName,  maxConcurrentCtz);
                        if(isCompleted) {
                            response.addProperty("status", "ok");
                            response.addProperty("message", "Đã thêm thành công đơn vị có thông tin vừa nhập.");
                        }
                        else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "Không thể thêm đơn vị do bị trùng tên với đơn vị khác. Vui lòng kiểm tra lại.");
                        }
                    }
                    break;

                case "GET_NEWEST_CITIZEN_REQUEST":
                    Department dept = SQLHelper.getDepartment(UUID.fromString(requestData.get("data").getAsString()));
                    if(dept != null) {
                        CitizenRequest citizenRequest = SQLHelper.getNewestCtzRequest(dept);
                        if(citizenRequest != null) {
                            JsonElement ctzRequest = gson.toJsonTree(citizenRequest);
                            response.addProperty("status", "ok");
                            response.add("data", ctzRequest);
                            response.addProperty("message", "Lấy thông tin đơn vị thành công");
                        }
                        else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "Không có công dân nào đang chờ ở đơn vị này.");
                        }
                    }
                    else {
                        response.addProperty("status", "error");
                        response.add("data", new JsonArray());
                        response.addProperty("message", "Không tìm thấy đơn vị hoặc lỗi cơ sở dữ liệu");
                    }
                    break;

                case "GET_ALL_REQUESTS_FROM_DEPARTMENT":
                    List<CitizenRequest> requests = SQLHelper.getAllRequestsFromDept(UUID.fromString(requestData.get("data").getAsString()));
                    if(requests != null) {
                        JsonElement ctzRequestList = gson.toJsonTree(requests);
                        response.addProperty("status", "ok");
                        response.add("data", ctzRequestList);
                        response.addProperty("message", "Đã lấy thành công danh sách lượt công dân thực hiện TTHC ở đơn vị.");
                    }
                    else {
                        response.addProperty("status", "error");
                        response.add("data", new JsonArray());
                        response.addProperty("message", "Không lấy được danh sách do lỗi cơ sở dữ liệu.");
                    }
                    break;

                case "GET_ALL_REQUESTS_IN_QUEUE":
                    List<CitizenRequest> requestsInQueue = SQLHelper.getAllRequestsInQueue(UUID.fromString(requestData.get("data").getAsString()));
                    if(requestsInQueue != null) {
                        JsonElement ctzRequestList = gson.toJsonTree(requestsInQueue);
                        response.addProperty("status", "ok");
                        response.add("data", ctzRequestList);
                        response.addProperty("message", "Đã lấy thành công danh sách lượt công dân thực hiện TTHC ở đơn vị.");
                    }
                    else {
                        response.addProperty("status", "error");
                        response.add("data", new JsonArray());
                        response.addProperty("message", "Không lấy được danh sách do lỗi cơ sở dữ liệu.");
                    }
                    break;

                case "GET_CITIZEN_REQUEST":
                    CitizenRequest citizenRequest = SQLHelper.getCitizenRequest(UUID.fromString(requestData.get("data").getAsString()));
                    if(citizenRequest != null) {
                        JsonElement ctzRequest = gson.toJsonTree(citizenRequest);
                        response.addProperty("status", "ok");
                        response.add("data", ctzRequest);
                        response.addProperty("message", "Lấy thông tin lượt làm việc thành công");
                    }
                    else {
                        response.addProperty("status", "error");
                        response.add("data", new JsonArray());
                        response.addProperty("message", "Không tìm thấy lượt hoặc lỗi cơ sở dữ liệu");
                    }
                    break;

                case "SET_REQUEST_STATUS":
                    JsonObject ctzRequestData = requestData.get("data").getAsJsonObject();
                    UUID requestId = UUID.fromString(ctzRequestData.get("requestId").getAsString());
                    int requestStatus = ctzRequestData.get("status").getAsInt();
                    SQLHelper.setRequestProcessStatus(requestId, requestStatus);
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
                    Account acc = SQLHelper.getAccount(userName);
                    response.addProperty("action", "LOGIN");
                    response.addProperty("status", "ok");
                    if(acc != null && !acc.getLoginStatus() && acc.getHashedPwd().equals(hashedPwd)) {
                        SQLHelper.setLoginStatus(acc, true);
                        JsonObject authData = new JsonObject();
                        authData.addProperty("isLoggedIn", true);
                        authData.addProperty("userName", acc.getUserName());
                        authData.addProperty("role", acc.getRole());
                        response.add("data", authData);
                    }
                    else {
                        JsonObject authData = new JsonObject();
                        authData.addProperty("isLoggedIn", false);
                        response.add("data", authData);
                    }
                    response.addProperty("message", "");
                    break;

                case "LOGOUT":
                    String user = requestData.get("data").getAsString();
                    Account acc1 = SQLHelper.getAccount(user);
                    response.addProperty("action", "LOGOUT");
                    response.addProperty("status", "ok");
                    if(acc1 != null && acc1.getLoginStatus()) {
                        response.addProperty("data", true);
                        SQLHelper.setLoginStatus(acc1, false);
                    }
                    else {
                        response.addProperty("data", false);
                    }
                    response.addProperty("message", "");
                    break;

                case "CHANGE_PASSWORD":
                    JsonObject accountInfo = requestData.get("data").getAsJsonObject();
                    String currentUsrName = accountInfo.get("account").getAsString();
                    String newPwd = accountInfo.get("newPassword").getAsString();
                    response.addProperty("action", "CHANGE_PASSWORD");
                    boolean result = SQLHelper.changePassword(currentUsrName, newPwd);
                    if(result) {
                        response.addProperty("status", "ok");
                        response.addProperty("message", "Đã đổi thành công mật khẩu của tài khoản " + currentUsrName + ".");
                    }
                    else {
                        response.addProperty("status", "error");
                        response.addProperty("message", "Tài khoản không tồn tại hoặc lỗi hệ thống.");
                    }
                    break;

                case "EDIT_DEPARTMENT":
                    JsonObject orgDeptData = requestData.get("data").getAsJsonObject();
                    if(orgDeptData != null) {
                        UUID deptId = UUID.fromString(orgDeptData.get("deptId").getAsString());
                        String deptName = orgDeptData.get("deptName").getAsString();
                        int maxConcurrentCtz = orgDeptData.get("maxConcurrentCtz").getAsInt();
                        boolean isCompleted = SQLHelper.editDepartment(deptId, deptName,  maxConcurrentCtz);
                        if(isCompleted) {
                            response.addProperty("status", "ok");
                            response.addProperty("message", "Đã sửa thành công đơn vị có thông tin vừa nhập.");
                        }
                        else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "Không thể sửa đơn vị do bị trùng tên với đơn vị khác. Vui lòng kiểm tra lại.");
                        }
                    }
                    break;

                case "DELETE_DEPARTMENT":
                    String deldept = requestData.get("data").getAsString();
                    if(deldept != null) {
                        UUID deptId = UUID.fromString(deldept);
                        boolean isCompleted = SQLHelper.deleteDepartment(deptId);
                        if(isCompleted) {
                            response.addProperty("status", "ok");
                            response.addProperty("message", "Đã xóa thành công đơn vị.");
                        }
                        else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "Không thể xóa đơn vị. Vui lòng kiểm tra lại.");
                        }
                    }
                    break;

                case "GET_ACCOUNTS":
                    List<Account> accountList = SQLHelper.getAccounts();
                    if(accountList != null && !accountList.isEmpty()) {
                        JsonElement accs = gson.toJsonTree(accountList);
                        response.addProperty("status", "ok");
                        response.add("data", accs);
                        response.addProperty("message", "Lấy danh sách thành công");
                    }
                    else {
                        response.addProperty("status", "error");
                        response.add("data", new JsonArray());
                        response.addProperty("message", "Lỗi cơ sở dữ liệu.");
                    }
                    break;

                case "ADD_ACCOUNT":
                    JsonObject newAccData = requestData.get("data").getAsJsonObject();
                    if(newAccData != null) {
                        String newUsrName = newAccData.get("username").getAsString();
                        String newAccPwd = newAccData.get("password").getAsString();
                        int role = newAccData.get("role").getAsInt();
                        boolean isCompleted = SQLHelper.createAccount(newUsrName, newAccPwd, role);
                        if(isCompleted) {
                            response.addProperty("status", "ok");
                            response.addProperty("message", "Đã thêm thành công tài khoản" + "\""
                                + newUsrName + "\"."
                            );
                        }
                        else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "Không thể thêm do bị trùng tên với " +
                                    "tài khoản khác. Vui lòng kiểm tra lại.");
                        }
                    }
                    break;

                case "DELETE_ACCOUNT":
                    String deletedAcc = requestData.get("data").getAsString();
                    if(deletedAcc != null) {
                        boolean isCompleted = SQLHelper.deleteAccount(deletedAcc);
                        if(isCompleted) {
                            response.addProperty("status", "ok");
                            response.addProperty("message", "Đã xóa thành công tài khoản "
                                    + deletedAcc + ".");
                        }
                        else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "Không thể xóa tài khoản. " +
                                    "Vui lòng kiểm tra lại.");
                        }
                    }
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