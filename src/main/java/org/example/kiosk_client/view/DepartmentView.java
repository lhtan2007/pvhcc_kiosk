package org.example.kiosk_client.view;

import org.example.shared.model.Department;

import javax.swing.*;

public class DepartmentView extends JButton {
    private final Department department;
    public DepartmentView(Department department) {
        this.department = department;
        this.setText(this.department.getDepartmentName());
    }
    public Department getDepartment() {
        return department;
    }
}
