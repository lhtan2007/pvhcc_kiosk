package org.example.manager_client;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonObject;
import org.example.manager_client.helper.ClockPanel;
import org.example.manager_client.helper.LoginHelper;
import org.example.manager_client.helper.NetworkInitializer;
import org.example.shared.helper.CustomSVGTranscoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MainManagerClient {
    public static void main(String[] args) {
        //FlatLaf
        System.setProperty("flatlaf.useWindowDecorations", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        }
        catch(Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 16));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Custom font
                Font svnKelsonRegular = null;
                Font svnKelsonBold = null;
                Font svnKelsonLight = null;
                try {
                    svnKelsonRegular = Font.createFont(Font.TRUETYPE_FONT, new
                            File("target/classes/fonts/KelsonSansRegular.otf"));
                    svnKelsonRegular = svnKelsonRegular.deriveFont(24f);
                    svnKelsonBold = Font.createFont(Font.TRUETYPE_FONT, new
                            File("target/classes/fonts/KelsonSansBold.otf"));
                    svnKelsonBold = svnKelsonBold.deriveFont(24f);
                    svnKelsonLight = Font.createFont(Font.TRUETYPE_FONT, new
                            File("target/classes/fonts/KelsonSansLight.otf"));
                    svnKelsonLight = svnKelsonLight.deriveFont(24f);
                }
                catch (FontFormatException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                GraphicsEnvironment ge =
                        GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(svnKelsonRegular);
                ge.registerFont(svnKelsonBold);
                ge.registerFont(svnKelsonLight);

                //Main window
                JFrame mainWindow = new JFrame("Hệ thống quản lý công tác tiếp công dân thực hiện TTHC");
                mainWindow.setVisible(true);
                mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                JPanel mainPane = (JPanel)mainWindow.getContentPane();
                mainPane.setPreferredSize(new Dimension(1280, 720));
                mainPane.setLayout(new BorderLayout());
                mainWindow.pack();
                mainWindow.setLocationRelativeTo(null);

                //Banner zone
                JPanel banner = new JPanel();
                banner.setLayout(new GridBagLayout());
                banner.setBackground(Color.red);
                GridBagConstraints banner_gbc = new GridBagConstraints();
                banner.setPreferredSize(new Dimension(-1, 130));
                CustomSVGTranscoder transcoder = new CustomSVGTranscoder();
                ImageIcon logo = transcoder.getIcon("mainicon.svg", 138, 128);
                JLabel bannerPlaceholder = new JLabel(logo);
                bannerPlaceholder.setForeground(new Color(255, 203, 5));
                bannerPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
                bannerPlaceholder.setText("<html>"
                        + "TRUNG TÂM PHỤC VỤ HÀNH CHÍNH CÔNG <br/>"
                        + "XÃ ...... <br/>"
                        + "HỆ THỐNG QUẢN LÝ CÔNG TÁC TIẾP CÔNG DÂN THỰC HIỆN TTHC"
                        + "</html>");
                bannerPlaceholder.setFont(svnKelsonBold);

                banner_gbc.gridx = 0;
                banner_gbc.weightx = 0.8;
                banner_gbc.fill = GridBagConstraints.HORIZONTAL;
                banner.add(bannerPlaceholder, banner_gbc);
                banner_gbc.gridx = 1;
                banner_gbc.weightx = 0.2;
                banner_gbc.fill = GridBagConstraints.HORIZONTAL;
                /*ClockPanel clockPanel = new ClockPanel(banner, banner_gbc, new Font("Segoe UI", Font.BOLD, 24));
                clockPanel.execute();*/
                mainPane.add(banner, BorderLayout.NORTH);

                //Main zone
                CardLayout mainCardLayout = new CardLayout();
                JPanel mainZone = new JPanel(mainCardLayout);
                mainPane.add(mainZone, BorderLayout.CENTER);

                JPanel loginPane = new JPanel();
                loginPane.setLayout(new BoxLayout(loginPane, BoxLayout.Y_AXIS));
                loginPane.add(Box.createVerticalGlue());
                JLabel notice = new JLabel("Vui lòng đăng nhập để tiếp tục");
                notice.setAlignmentX(Component.CENTER_ALIGNMENT);
                notice.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 24));
                loginPane.add(notice);
                loginPane.add(Box.createVerticalStrut(50));
                JPanel loginForm = new JPanel(new GridLayout(3, 2, 30, 10));
                loginForm.setAlignmentX(Component.CENTER_ALIGNMENT);
                loginForm.setMaximumSize(new Dimension(600, loginForm.getPreferredSize().height));
                JLabel ipAddress = new JLabel("Địa chỉ IP máy chủ");
                JTextField ipAddressInput = new JTextField();
                JLabel usrName = new JLabel("Tên đăng nhập");
                JTextField usrNameInput = new JTextField();
                JLabel pwd = new JLabel("Mật khẩu");
                JPasswordField pwdInput = new JPasswordField();
                loginForm.add(ipAddress);
                loginForm.add(ipAddressInput);
                loginForm.add(usrName);
                loginForm.add(usrNameInput);
                loginForm.add(pwd);
                loginForm.add(pwdInput);
                loginPane.add(loginForm);
                loginPane.add(Box.createVerticalStrut(25));
                JButton loginButton = new JButton("Đăng nhập");
                loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                loginButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String address = ipAddressInput.getText();
                        String userName = usrNameInput.getText();
                        char[] pwdChars = pwdInput.getPassword();
                        if(address.isEmpty() || userName.isEmpty() || pwdChars.length == 0) {
                            JOptionPane.showMessageDialog(null, "Thông tin đăng nhập không được bỏ trống. Xin vui lòng kiểm tra lại.", "Không thực hiện được yêu cầu", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        String hashedPwd = LoginHelper.hashPwd(new String(pwdChars));
                        JsonObject response = null;
                        JButton loginButton = (JButton)e.getSource();
                        loginButton.setEnabled(false);
                        loginButton.setText("Đang xử lý...");

                        SwingWorker<JsonObject, Void> loginWorker = new SwingWorker<JsonObject, Void>() {
                            @Override
                            protected JsonObject doInBackground() throws Exception {
                                NetworkInitializer.getInstance().updateConfiguration(address, 9999);
                                JsonObject loginRequest = new JsonObject();
                                JsonObject loginAccount = new JsonObject();
                                loginRequest.addProperty("clientType", "manager");
                                loginRequest.addProperty("action", "LOGIN");
                                loginAccount.addProperty("username", userName);
                                loginAccount.addProperty("password", hashedPwd);
                                loginRequest.add("data", loginAccount);
                                return NetworkInitializer.getInstance().sendRequest(loginRequest);
                            }
                            @Override
                            protected void done() {
                                loginButton.setEnabled(true);
                                loginButton.setText("Đăng nhập");
                                try {
                                    JsonObject response = get();
                                    if(response == null) {
                                        JOptionPane.showMessageDialog(null, "Mất kết nối với máy chủ.", "Lỗi mạng", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    System.out.println("JSON: " + response.toString());
                                    if("ok".equals(response.get("status").getAsString())) {
                                        boolean isLoggedIn = response.get("data").getAsBoolean();
                                        if(isLoggedIn) {
                                            System.out.println("Login successfully!");
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null, "Thông tin tên đăng nhập hoặc mật khẩu không đúng. Vui lòng kiểm tra lại.", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                                        }
                                        usrNameInput.setText("");
                                        pwdInput.setText("");
                                    }
                                    else {
                                        String error = response.get("message").getAsString();
                                        JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + error, "Thất bại", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(null, "Lỗi trong quá trình xử lý: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };
                        loginWorker.execute();
                    }
                });
                loginPane.add(loginButton);
                loginPane.add(Box.createVerticalGlue());
                mainZone.add(loginPane);

                JPanel dashboardPane = new JPanel();
            }
        });
    }
}
