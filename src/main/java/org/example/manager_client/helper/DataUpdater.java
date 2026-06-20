package org.example.manager_client.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.manager_client.MainManagerClient;
import org.example.shared.model.Account;
import org.example.shared.model.CitizenRequest;
import org.example.shared.model.Department;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
                    if(newDepts == null || newDepts.isEmpty()) return;
                    DefaultComboBoxModel<Department> model = (DefaultComboBoxModel<Department>) departmentJComboBox.getModel();
                    if(model.getSize() == 0 || model.getSize() != newDepts.size()) {
                        Department selected = (Department) model.getSelectedItem();
                        model.removeAllElements();
                        for(Department dept : newDepts) {
                            model.addElement(dept);
                        }
                        if(selected != null) {
                            for (int i = 0; i < model.getSize(); i++) {
                                if (model.getElementAt(i).getDepartmentId().equals(selected.getDepartmentId())) {
                                    model.setSelectedItem(model.getElementAt(i));
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        for(int i = 0; i < model.getSize(); i++) {
                            Department currentModelDept = model.getElementAt(i);
                            for(Department freshDept : newDepts) {
                                if(currentModelDept.getDepartmentId().equals(freshDept.getDepartmentId())) {
                                    if(!currentModelDept.getDepartmentName().equals(freshDept.getDepartmentName())) {
                                        currentModelDept.setDepartmentName(freshDept.getDepartmentName());
                                        int idx = model.getIndexOf(currentModelDept);
                                        model.removeElementAt(idx);
                                        model.insertElementAt(currentModelDept, idx);
                                        model.setSelectedItem(currentModelDept);
                                    }
                                    currentModelDept.setNumOfProcessedRequest(freshDept.getNumOfProcessedRequest());
                                    currentModelDept.setMaxConcurrentRequestInDay(freshDept.getMaxConcurrentRequestInDay());
                                    break;
                                }
                            }
                        }
                    }
                }
                catch(Exception e) {
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
    public static void updateAccount(DefaultTableModel accountTm, JTable accountTable) {
        SwingWorker<List<Account>, Void> updateWorker = new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                List<Account> accountList = new ArrayList<>();
                JsonObject request = new JsonObject();
                request.addProperty("clientType", "manager");
                request.addProperty("action", "GET_ACCOUNTS");
                JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
                if (response != null && "ok".equals(response.get("status").getAsString())) {
                    if (response.has("data") && response.get("data").isJsonArray()) {
                        JsonArray dataArray = response.getAsJsonArray("data");
                        Type deptType = new TypeToken<List<Account>>() {}.getType();
                        accountList = NetworkInitializer.getInstance().getGson().fromJson(dataArray, deptType);
                    }
                }
                return accountList;
            }
            @Override
            protected void done() {
                try {
                    List<Account> newAccounts = get();
                    if (newAccounts == null) return;
                    String selectedUsername = null;
                    int selectedRowView = accountTable.getSelectedRow();
                    if(selectedRowView != -1) {
                        int selectedRowModel = accountTable.convertRowIndexToModel(selectedRowView);
                        selectedUsername = (String)accountTm.getValueAt(selectedRowModel, 0);
                    }
                    for (int i = accountTm.getRowCount() - 1; i >= 0; i--) {
                        String currentUsername = (String) accountTm.getValueAt(i, 0);
                        Account matchingAccount = null;
                        for(Account acc : newAccounts) {
                            if(acc.getUserName().equals(currentUsername)) {
                                matchingAccount = acc;
                                break;
                            }
                        }
                        if(matchingAccount != null) {
                            Integer currentRole = (Integer)accountTm.getValueAt(i, 1);
                            if(!currentRole.equals(matchingAccount.getRole())) {
                                accountTm.setValueAt(matchingAccount.getRole(), i, 1);
                            }
                            newAccounts.remove(matchingAccount);
                        }
                        else {
                            accountTm.removeRow(i);
                        }
                    }
                    for(Account newAcc : newAccounts) {
                        Object[] rowData = new Object[]{newAcc.getUserName(), newAcc.getRole()};
                        accountTm.addRow(rowData);
                    }
                    if(selectedUsername != null) {
                        for(int i = 0; i < accountTm.getRowCount(); i++) {
                            String usernameInModel = (String) accountTm.getValueAt(i, 0);
                            if(usernameInModel.equals(selectedUsername)) {
                                int rowIndexView = accountTable.convertRowIndexToView(i);
                                if(rowIndexView != -1) {
                                    accountTable.setRowSelectionInterval(rowIndexView, rowIndexView);
                                }
                                break;
                            }
                        }
                    }
                }
                catch(Exception e) {
                    System.err.println("Lỗi khi cập nhật danh sách tài khoản: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        updateWorker.execute();
    }
    public static void createAccount(JFrame mainWindow, String userName, String hashedPwd, int role) {
        JsonObject request = new JsonObject();
        request.addProperty("clientType", "manager");
        request.addProperty("action", "ADD_ACCOUNT");
        JsonObject accountData = new JsonObject();
        accountData.addProperty("username", userName);
        accountData.addProperty("password", hashedPwd);
        accountData.addProperty("role", role);
        request.add("data", accountData);
        JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
        if(response != null) {
            String res = response.get("status").getAsString();
            String msg = response.get("message").getAsString();
            if(res.equals("ok")) {
                JOptionPane.showMessageDialog(mainWindow, msg, "Thêm tài khoản thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(mainWindow, msg, "Không thể thêm tài khoản",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(mainWindow, "Lỗi kết nối. Vui lòng kiểm tra lại.",
                    "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void deleteAccount(JFrame mainWindow, JTable accountTable, DefaultTableModel accountTm) {
        int selectedRowView = accountTable.getSelectedRow();
        if(selectedRowView == -1) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Vui lòng chọn một tài khoản trong bảng trước khi thực hiện.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRowModel = accountTable.convertRowIndexToModel(selectedRowView);
        String username = (String)accountTm.getValueAt(selectedRowModel, 0);
        int confirm = JOptionPane.showConfirmDialog(mainWindow,
                "Bạn có chắc chắn muốn xóa tài khoản " + username + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            JsonObject request = new JsonObject();
            request.addProperty("clientType", "manager");
            request.addProperty("action", "DELETE_ACCOUNT");
            request.addProperty("data", username);
            JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
            System.out.println("Đang gửi yêu cầu xóa tài khoản: " + username);
            if(response != null) {
                String msg = response.get("message").getAsString();
                String status = response.get("status").getAsString();
                if(status.equals("ok")) {
                    JOptionPane.showMessageDialog(mainWindow, msg, "Xóa tài khoản",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else if(status.equals("error")) {
                    JOptionPane.showMessageDialog(mainWindow, msg, "Xóa tài khoản", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
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

    public static void addDepartment(JDialog dialog, String deptName, int maxConcurrentCtz) {
        if(deptName.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Tên đơn vị không thể bỏ trống. Vui lòng kiểm tra lại.", "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
        }
        else if(maxConcurrentCtz == 0) {
            JOptionPane.showMessageDialog(dialog, "Số lượt tiếp công dân phải là số nguyên lớn hơn 0. Vui lòng kiểm tra lại.", "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
        }
        else {
            JsonObject request = new JsonObject();
            request.addProperty("clientType", "manager");
            request.addProperty("action", "ADD_DEPARTMENT");
            JsonObject departmentData = new JsonObject();
            departmentData.addProperty("deptName", deptName);
            departmentData.addProperty("maxConcurrentCtz", maxConcurrentCtz);
            request.add("data", departmentData);
            JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
            if(response != null) {
                String res = response.get("status").getAsString();
                String msg = response.get("message").getAsString();
                if(res.equals("ok")) {
                    JOptionPane.showMessageDialog(dialog, msg, "Thêm đơn vị thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(dialog, msg, "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                JOptionPane.showMessageDialog(dialog, "Lỗi kết nối. Vui lòng kiểm tra lại.", "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void addDepartmentFromXML(Department department) {
        JsonObject request = new JsonObject();
        request.addProperty("clientType", "manager");
        request.addProperty("action", "ADD_DEPARTMENT");
        String deptName = department.getDepartmentName();
        int maxConcurrentCtz = department.getMaxConcurrentRequestInDay();
        JsonObject departmentData = new JsonObject();
        departmentData.addProperty("deptName", deptName);
        departmentData.addProperty("maxConcurrentCtz", maxConcurrentCtz);
        request.add("data", departmentData);
        JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
    }

    public static void editDepartment(JDialog dialog, String deptId, String deptName, int maxConcurrentCtz) {
        if(deptName.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Tên đơn vị không thể bỏ trống. Vui lòng kiểm tra lại.", "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
        }
        else if(maxConcurrentCtz == 0) {
            JOptionPane.showMessageDialog(dialog, "Số lượt tiếp công dân phải là số nguyên lớn hơn 0. Vui lòng kiểm tra lại.", "Không thể thêm đơn vị", JOptionPane.ERROR_MESSAGE);
        }
        else {
            JsonObject request = new JsonObject();
            request.addProperty("clientType", "manager");
            request.addProperty("action", "EDIT_DEPARTMENT");
            JsonObject departmentData = new JsonObject();
            departmentData.addProperty("deptId", deptId);
            departmentData.addProperty("deptName", deptName);
            departmentData.addProperty("maxConcurrentCtz", maxConcurrentCtz);
            request.add("data", departmentData);
            JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
            if(response != null) {
                String res = response.get("status").getAsString();
                String msg = response.get("message").getAsString();
                if(res.equals("ok")) {
                    JOptionPane.showMessageDialog(dialog, msg, "Sửa cấu hình đơn vị thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(dialog, msg, "Không thể sửa cấu hình đơn vị", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                JOptionPane.showMessageDialog(dialog, "Lỗi kết nối. Vui lòng kiểm tra lại.", "Không thể sửa cấu hình đơn vị", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void deleteDepartment(JFrame mainWindow, String deptId) {
        JsonObject request = new JsonObject();
        request.addProperty("clientType", "manager");
        request.addProperty("action", "DELETE_DEPARTMENT");
        request.addProperty("data", deptId);
        JsonObject response = NetworkInitializer.getInstance().sendRequest(request);
        if(response != null) {
            String res = response.get("status").getAsString();
            String msg = response.get("message").getAsString();
            if(res.equals("ok")) {
                JOptionPane.showMessageDialog(mainWindow, msg, "Xóa đơn vị thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(mainWindow, msg, "Không thể xóa đơn vị", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(mainWindow, "Lỗi kết nối. Vui lòng kiểm tra lại.", "Không thể " +
                    "xóa đơn vị", JOptionPane.ERROR_MESSAGE);
        }
    }
}
