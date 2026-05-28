package org.example.kiosk_client.view;

import org.example.kiosk_client.helper.NetworkInitializer;

import javax.swing.*;
import java.awt.*;

public class ConfigDialog extends JDialog {
    private JTextField ipInput, portInput;
    public ConfigDialog(JFrame parent) {
        super(parent, "Cài đặt kết nối máy chủ", true);
        setLayout(new BorderLayout(10, 10));
        setSize(600, 250);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Địa chỉ IP Server"));
        ipInput = new JTextField("127.0.0.1");
        ipInput.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        formPanel.add(ipInput);

        formPanel.add(new JLabel("Cổng"));
        portInput = new JTextField("9999");
        portInput.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        formPanel.add(portInput);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Lưu & kết nối lại");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnSave.addActionListener(e -> {
            try {
                String ip = ipInput.getText().trim();
                int port = Integer.parseInt(portInput.getText().trim());

                NetworkInitializer.getInstance().updateConfiguration(ip, port);

                JOptionPane.showMessageDialog(this, "Đã áp dụng cấu hình mạng mới!");
                dispose();
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cổng phải là một số nguyên hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
