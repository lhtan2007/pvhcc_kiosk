package org.example.kiosk_client.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.kiosk_client.helper.NetworkInitializer;
import org.example.kiosk_client.helper.TicketPrintHelper;
import org.example.kiosk_client.view.DepartmentView;
import org.example.shared.model.CitizenRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.UUID;

public class DepartmentController implements java.awt.event.ActionListener {
    private final JTextField citizenFullNameInput, citizenNationalIdInput;
    private final DepartmentView departmentView;
    private final Gson commonGson = NetworkInitializer.getInstance().getGson();

    public DepartmentController(JTextField citizenFullNameInput, JTextField citizenNationalIdInput, DepartmentView departmentView) {
        this.citizenFullNameInput = citizenFullNameInput;
        this.citizenNationalIdInput = citizenNationalIdInput;
        this.departmentView = departmentView;
        this.departmentView.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String fullName = citizenFullNameInput.getText();
        String nationalId = citizenNationalIdInput.getText();
        LocalDateTime requestDate = LocalDateTime.now();
        UUID departmentId = departmentView.getDepartment().getDepartmentId();
        JsonObject response = null;
        if(!fullName.isEmpty() && !nationalId.isEmpty()) {
            CitizenRequest citizenRequest = new CitizenRequest(fullName, nationalId, requestDate, departmentId);

            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("clientType", "kiosk");
            requestJson.addProperty("action", "REQUEST_TICKET");
            requestJson.add("data", commonGson.toJsonTree(citizenRequest));
            response = NetworkInitializer.getInstance().sendRequest(requestJson);

            System.out.println("Raw JSON từ Server: " + response.toString());

            if ("ok".equals(response.get("status").getAsString())) {
                String ticketNumber = response.get("data").getAsString();
                if(Integer.parseInt(ticketNumber) != -1) {
                    JOptionPane.showMessageDialog(null, "Yêu cầu của công dân đã được tiếp nhận. Số thứ tự trong hàng đợi là " + ticketNumber + ". Vui lòng có mặt khi được thông báo.");
                    TicketPrintHelper.executePrint(ticketNumber, departmentView.getDepartment().getDepartmentName(), requestDate, fullName, nationalId);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Danh sách chờ làm việc của đơn vị này đã đầy. Xin vui lòng quay lại sau.");
                }
                citizenFullNameInput.setText("");
                citizenNationalIdInput.setText("");
            }
            else {
                String error = response.get("message").getAsString();
                JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + error, "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Thông tin cá nhân không được bỏ trống. Xin vui lòng kiểm tra lại.", "Không thực hiện được yêu cầu", JOptionPane.ERROR_MESSAGE);
        }
    }
}
