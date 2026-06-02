package org.example.manager_client.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.manager_client.MainManagerClient;
import org.example.shared.model.CitizenRequest;
import org.example.shared.model.Department;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataUpdater {
    public static void updateDepartment(JComboBox<Department> departmentJComboBox) {
        SwingWorker<List<Department>, Void> updateWorker = new SwingWorker<List<Department>, Void>() {
            @Override
            protected List<Department> doInBackground() throws Exception {
                List<Department> departmentList = new ArrayList<>();
                JsonObject request = new JsonObject();
                request.addProperty("clientType", "manager");
                request.addProperty("action", "GET_DEPARTMENTS");
                JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
                if (response != null && "ok".equals(response.get("status").getAsString())) {
                    if (response.has("data") && response.get("data").isJsonArray()) {
                        JsonArray dataArray = response.getAsJsonArray("data");
                        Type deptType = new TypeToken<List<Department>>() {}.getType();
                        departmentList = NetworkInitializer.getInstance().getGson().fromJson(dataArray, deptType);
                    }
                }
                return departmentList;
            }

            @Override
            protected void done() {
                try {
                    List<Department> newDepts = get();
                    if (newDepts == null || newDepts.isEmpty()) return;
                    DefaultComboBoxModel<Department> model = (DefaultComboBoxModel<Department>) departmentJComboBox.getModel();
                    if (model.getSize() == 0 || model.getSize() != newDepts.size()) {
                        Department selected = (Department) model.getSelectedItem();
                        model.removeAllElements();
                        for (Department dept : newDepts) {
                            model.addElement(dept);
                        }
                        if (selected != null) {
                            for (int i = 0; i < model.getSize(); i++) {
                                if (model.getElementAt(i).getDepartmentId().equals(selected.getDepartmentId())) {
                                    model.setSelectedItem(model.getElementAt(i));
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < model.getSize(); i++) {
                            Department currentModelDept = model.getElementAt(i);
                            for (Department freshDept : newDepts) {
                                if (currentModelDept.getDepartmentId().equals(freshDept.getDepartmentId())) {
                                    currentModelDept.setNumOfProcessedRequest(freshDept.getNumOfProcessedRequest());
                                    currentModelDept.setMaxConcurrentRequestInDay(freshDept.getMaxConcurrentRequestInDay());
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật JComboBox: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        updateWorker.execute();
    }
    public static void updateRequest(Department dept, JLabel requestNumber, JLabel fullName, JLabel nationalId) {
        SwingWorker<CitizenRequest, Void> updateWorker = new SwingWorker<CitizenRequest, Void>() {
            @Override
            protected CitizenRequest doInBackground() throws Exception {
                CitizenRequest citizenRequest = null;
                JsonObject request = new JsonObject();
                request.addProperty("clientType", "manager");
                request.addProperty("action", "GET_NEWEST_CITIZEN_REQUEST");
                request.addProperty("data", dept.getDepartmentId().toString());
                JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
                if (response != null && response.has("status") && "ok".equals(response.get("status").getAsString())) {
                    citizenRequest = NetworkInitializer.getInstance().getGson().fromJson(response.getAsJsonObject("data"), CitizenRequest.class);
                }
                return citizenRequest;
            }

            @Override
            protected void done() {
                try {
                    CitizenRequest citizenRequest = get();
                    if(citizenRequest != null) {
                        requestNumber.setText(String.valueOf(citizenRequest.getRequestNumber()));
                        fullName.setText(citizenRequest.getFullName());
                        nationalId.setText(citizenRequest.getNationalId());
                    }
                    else {
                        requestNumber.setText("-");
                        fullName.setText("Chưa có lượt tiếp theo");
                        nationalId.setText("-");
                    }
                    MainManagerClient.currentCtzRequest = citizenRequest;
                }
                catch(Exception e) {
                    System.err.println("Lỗi khi cập nhật thông tin: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        updateWorker.execute();
    }
    public static void inviteCitizen() {

    }
    public static void confirmCancelledRequest(CitizenRequest currentCtzRequest) {
        JsonObject request = new JsonObject();
        request.addProperty("clientType", "manager");
        request.addProperty("action", "SET_REQUEST_STATUS");
        JsonObject data = new JsonObject();
        data.addProperty("requestId", currentCtzRequest.getRequestId().toString());
        data.addProperty("status", 2);
        request.add("data", data);
        NetworkInitializer.getInstance().sendRequest(request);
    }
    public static void confirmAdmittedRequest(CitizenRequest currentCtzRequest) {
        JsonObject request = new JsonObject();
        request.addProperty("clientType", "manager");
        request.addProperty("action", "SET_REQUEST_STATUS");
        JsonObject data = new JsonObject();
        data.addProperty("requestId", currentCtzRequest.getRequestId().toString());
        data.addProperty("status", 1);
        request.add("data", data);
        NetworkInitializer.getInstance().sendRequest(request);
    }
    public static void updateDataToQueue(DefaultTableModel dtm, Department dept) {
        SwingWorker<List<CitizenRequest>, Void> updateWorker = new SwingWorker<List<CitizenRequest>, Void>() {
            @Override
            protected List<CitizenRequest> doInBackground() throws Exception {
                List<CitizenRequest> requestList = null;
                JsonObject request = new JsonObject();
                request.addProperty("clientType", "manager");
                request.addProperty("action", "GET_ALL_REQUESTS_IN_QUEUE");
                request.addProperty("data", dept.getDepartmentId().toString());
                JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
                if(response != null && response.has("status") && "ok".equals(response.get("status").getAsString())) {
                    if (response.has("data") && response.get("data").isJsonArray()) {
                        JsonArray dataArray = response.getAsJsonArray("data");
                        Type rqType = new TypeToken<List<CitizenRequest>>() {}.getType();
                        requestList = NetworkInitializer.getInstance().getGson().fromJson(dataArray, rqType);
                    }
                }
                return requestList;
            }
            @Override
            protected void done() {
                try {
                    List<CitizenRequest> requestList = get();
                    dtm.setRowCount(0);
                    for(CitizenRequest request : requestList) {
                        Object[] rowData = {
                                request.getRequestNumber(),
                                request.getRequestDate(),
                        };
                        dtm.addRow(rowData);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateWorker.execute();
    }
    public static void updateDataToLog(DefaultTableModel dtm, Department dept) {
        SwingWorker<List<CitizenRequest>, Void> updateWorker = new SwingWorker<List<CitizenRequest>, Void>() {
            @Override
            protected List<CitizenRequest> doInBackground() throws Exception {
                List<CitizenRequest> requestList = null;
                JsonObject request = new JsonObject();
                request.addProperty("clientType", "manager");
                request.addProperty("action", "GET_ALL_REQUESTS_FROM_DEPARTMENT");
                request.addProperty("data", dept.getDepartmentId().toString());
                JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
                if(response != null && response.has("status") && "ok".equals(response.get("status").getAsString())) {
                    if (response.has("data") && response.get("data").isJsonArray()) {
                        JsonArray dataArray = response.getAsJsonArray("data");
                        Type rqType = new TypeToken<List<CitizenRequest>>() {}.getType();
                        requestList = NetworkInitializer.getInstance().getGson().fromJson(dataArray, rqType);
                    }
                }
                return requestList;
            }
            @Override
            protected void done() {
                try {
                    List<CitizenRequest> requestList = get();
                    dtm.setRowCount(0);
                    for(CitizenRequest request : requestList) {
                        String statusText = (request.getProcessStatus() == 0) ? "Đang chờ" : "Đã xử lý";
                        Object[] rowData = {
                                request.getRequestDate(),
                                request.getFullName(),
                                request.getNationalId(),
                                request.getRequestNumber(),
                                statusText
                        };
                        dtm.addRow(rowData);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateWorker.execute();
    }

    public static void changePassword(JDialog dialog, String userName, String newPwd, String retypePwd) {
        if(!newPwd.equals(retypePwd)) {
            JOptionPane.showMessageDialog(dialog, "Mật khẩu nhập lại và mật khẩu mới không trùng khớp. Vui lòng kiểm tra lại.", "Không thể đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
        }
        else if(newPwd.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Mật khẩu mới không được bỏ trống. Vui lòng kiểm tra lại.", "Không thể đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
        }
        else {
            JsonObject request = new JsonObject();
            request.addProperty("clientType", "manager");
            request.addProperty("action", "CHANGE_PASSWORD");
            JsonObject accountData = new JsonObject();
            accountData.addProperty("account", userName);
            accountData.addProperty("newPassword", newPwd);
            request.add("data", accountData);
            JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
            if(response != null) {
                String res = response.get("status").getAsString();
                String msg = response.get("message").getAsString();
                if(res.equals("ok")) {
                    JOptionPane.showMessageDialog(dialog, msg, "Đổi mật khẩu thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(dialog, msg, "Không thể đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                JOptionPane.showMessageDialog(dialog, "Lỗi kết nối. Vui lòng kiểm tra lại.", "Không thể đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
