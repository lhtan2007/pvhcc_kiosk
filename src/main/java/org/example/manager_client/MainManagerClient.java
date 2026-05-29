package org.example.manager_client;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonObject;
import org.example.manager_client.helper.ClockPanel;
import org.example.manager_client.helper.LoginHelper;
import org.example.manager_client.helper.NetworkInitializer;
import org.example.shared.helper.CustomSVGTranscoder;
import org.example.shared.model.Department;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MainManagerClient {
    public static int currentInviteCount = 0;

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
                mainWindow.setMinimumSize(mainWindow.getPreferredSize());
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
                                            mainCardLayout.next(mainZone);
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null, "Thông tin đăng nhập không đúng hoặc tài khoản đang được sử dụng trên thiết bị khác. Vui lòng kiểm tra lại.", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
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
                dashboardPane.setLayout(new BorderLayout());
                ClockPanel clock = new ClockPanel(dashboardPane, UIManager.getFont("defaultFont").deriveFont(Font.BOLD | Font.ITALIC));
                clock.execute();

                //Button panel
                JPanel btnPanel = new JPanel();
                btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
                JComboBox<Department> departmentList = new JComboBox<Department>();
                JButton importList = new JButton("Nhập danh sách đơn vị");
                JButton exportList = new JButton("Xuất danh sách đơn vị");
                JButton exportRequestList = new JButton("Xuất danh sách lượt tiếp công dân");
                JButton addDepartment = new JButton("Thêm đơn vị");
                JButton modifyDepartment = new JButton("Sửa thông tin đơn vị");
                JButton deleteDepartment = new JButton("Xóa đơn vị");
                btnPanel.add(departmentList);
                btnPanel.add(importList);
                btnPanel.add(exportList);
                btnPanel.add(exportRequestList);
                btnPanel.add(addDepartment);
                btnPanel.add(modifyDepartment);
                btnPanel.add(deleteDepartment);
                dashboardPane.add(btnPanel, BorderLayout.NORTH);

                //Department information and statisitcs panel
                JPanel deptPanel = new JPanel();
                deptPanel.setBackground(Color.GRAY);
                deptPanel.setLayout(new BoxLayout(deptPanel, BoxLayout.Y_AXIS));
                JPanel deptInfoPanel = new JPanel();
                deptInfoPanel.setLayout(new BoxLayout(deptInfoPanel, BoxLayout.Y_AXIS));
                deptInfoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin đơn vị"));
                deptInfoPanel.setPreferredSize(new Dimension(400, 300));
                deptInfoPanel.setMaximumSize(deptInfoPanel.getPreferredSize());
                deptInfoPanel.add(Box.createVerticalStrut(30));
                JTextPane deptName = new JTextPane();
                SimpleAttributeSet deptName_sas = new SimpleAttributeSet();
                StyleConstants.setAlignment(deptName_sas, StyleConstants.ALIGN_CENTER);
                StyledDocument deptName_doc = deptName.getStyledDocument();
                deptName_doc.setParagraphAttributes(0, deptName_doc.getLength(), deptName_sas, false);
                deptName.setEditable(false);
                deptName.setCursor(null);
                deptName.setOpaque(false);
                deptName.setFocusable(false);
                deptName.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 24));
                deptName.setForeground(UIManager.getColor("Label.foreground"));
                deptName.setAlignmentX(Component.CENTER_ALIGNMENT);
                deptName.setText("Các ban quản lý dự án và các đơn vị sự nghiệp trực thuộc UBND xã");
                deptInfoPanel.add(deptName);
                JPanel deptDetailPanel = new JPanel();
                GridBagLayout deptDetailPanelGbl = new GridBagLayout();
                GridBagConstraints deptDetailPanelGbc = new GridBagConstraints();
                deptDetailPanel.setLayout(deptDetailPanelGbl);
                JLabel numOfProcessedRequest = new JLabel("Số lượt công dân đến làm thủ tục đã xử lý");
                JLabel numOfProcessedRequestSrv = new JLabel("100");
                JLabel maxConcurrentRqInDay = new JLabel("Số lượt tiếp công dân tối đa trong ngày");
                JLabel maxConcurrentRqInDaySrv = new JLabel("200");
                deptDetailPanelGbc.gridx = 0;
                deptDetailPanelGbc.gridy = 0;
                deptDetailPanelGbc.weightx = 0.8;
                deptDetailPanelGbc.weighty = 1;
                deptDetailPanelGbc.fill = GridBagConstraints.HORIZONTAL;
                deptDetailPanel.add(numOfProcessedRequest, deptDetailPanelGbc);
                deptDetailPanelGbc.gridx = 1;
                deptDetailPanel.add(numOfProcessedRequestSrv, deptDetailPanelGbc);
                deptDetailPanelGbc.gridx = 0;
                deptDetailPanelGbc.gridy = 1;
                deptDetailPanel.add(maxConcurrentRqInDay, deptDetailPanelGbc);
                deptDetailPanelGbc.gridx = 1;
                deptDetailPanel.add(maxConcurrentRqInDaySrv, deptDetailPanelGbc);
                deptInfoPanel.add(deptDetailPanel);

                JPanel deptStatisticsPanel = new JPanel();
                deptStatisticsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê"));

                deptPanel.add(deptInfoPanel);
                deptPanel.add(deptStatisticsPanel);
                dashboardPane.add(deptPanel, BorderLayout.WEST);
                //Citizen's request approval panel
                JPanel ctzRequestWrapperPanel = new JPanel();
                ctzRequestWrapperPanel.setLayout(new BoxLayout(ctzRequestWrapperPanel, BoxLayout.Y_AXIS));
                ctzRequestWrapperPanel.setPreferredSize(new Dimension(300, 0));

                JPanel ctzRequestPanel = new JPanel();
                ctzRequestPanel.setLayout(new BoxLayout(ctzRequestPanel, BoxLayout.Y_AXIS));
                ctzRequestPanel.setBorder(BorderFactory.createTitledBorder("Lượt hiện tại"));
                ctzRequestPanel.add(Box.createVerticalStrut(50));
                JLabel requestNumber = new JLabel("123");
                requestNumber.setAlignmentX(Container.CENTER_ALIGNMENT);
                requestNumber.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 36));
                requestNumber.setMaximumSize(new Dimension(Integer.MAX_VALUE, requestNumber.getPreferredSize().height));
                requestNumber.setHorizontalAlignment(SwingConstants.CENTER);
                ctzRequestPanel.add(requestNumber);
                ctzRequestPanel.add(Box.createVerticalStrut(50));
                JLabel fullName = new JLabel("Họ và tên");
                fullName.setAlignmentX(Component.CENTER_ALIGNMENT);
                fullName.setFont(UIManager.getFont("defaultFont").deriveFont(Font.ITALIC));
                fullName.setMaximumSize(new Dimension(Integer.MAX_VALUE, fullName.getPreferredSize().height));
                fullName.setHorizontalAlignment(SwingConstants.LEFT);
                ctzRequestPanel.add(fullName);
                ctzRequestPanel.add(Box.createVerticalStrut(5));
                JLabel fullNameSrv = new JLabel("Nguyễn Văn A");
                fullNameSrv.setAlignmentX(Component.CENTER_ALIGNMENT);
                fullNameSrv.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD));
                fullNameSrv.setMaximumSize(new Dimension(Integer.MAX_VALUE, fullNameSrv.getPreferredSize().height));
                fullNameSrv.setHorizontalAlignment(SwingConstants.CENTER);
                ctzRequestPanel.add(fullNameSrv);
                ctzRequestPanel.add(Box.createVerticalStrut(5));
                JLabel nationalId = new JLabel("Số định danh cá nhân");
                nationalId.setMaximumSize(new Dimension(Integer.MAX_VALUE, nationalId.getPreferredSize().height));
                nationalId.setAlignmentX(Component.CENTER_ALIGNMENT);
                nationalId.setFont(UIManager.getFont("defaultFont").deriveFont(Font.ITALIC));
                nationalId.setHorizontalAlignment(SwingConstants.LEFT);
                ctzRequestPanel.add(nationalId);
                ctzRequestPanel.add(Box.createVerticalStrut(5));
                JLabel nationalIdSrv = new JLabel("000000000000");
                nationalIdSrv.setAlignmentX(Component.CENTER_ALIGNMENT);
                nationalIdSrv.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD));
                nationalIdSrv.setMaximumSize(new Dimension(Integer.MAX_VALUE, nationalIdSrv.getPreferredSize().height));
                nationalIdSrv.setHorizontalAlignment(SwingConstants.CENTER);
                ctzRequestPanel.add(nationalIdSrv);
                ctzRequestPanel.add(Box.createVerticalStrut(5));
                JPanel actionPanel = new JPanel();
                actionPanel.setLayout(new GridLayout(2,2,10,10));
                JPanel buttonWrapper = new JPanel();
                CardLayout btnWrapperLayout = new CardLayout();
                buttonWrapper.setLayout(btnWrapperLayout);
                JButton inviteCtz = new JButton("Mời công dân đến làm việc");
                inviteCtz.setAlignmentX(Container.CENTER_ALIGNMENT);
                JButton confirmRequest = new JButton("Xác nhận đã tiếp công dân");
                confirmRequest.setAlignmentX(Container.CENTER_ALIGNMENT);
                confirmRequest.setMaximumSize(new Dimension(Integer.MAX_VALUE, confirmRequest.getPreferredSize().height));
                JButton confirmCancelRq = new JButton("<html>Xác nhận công dân<br>không đến làm việc</html>");
                confirmCancelRq.setAlignmentX(Container.CENTER_ALIGNMENT);

                inviteCtz.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(currentInviteCount < 3) {
                            //TODO: Add actual invite logic
                            currentInviteCount++;
                        }
                        else {
                            btnWrapperLayout.show(buttonWrapper, "CANCEL");
                        }
                    }
                });
                confirmRequest.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //TODO: Add actual confirmation logic
                    }
                });
                confirmCancelRq.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //TODO: Add actual logic
                        currentInviteCount = 0;
                        btnWrapperLayout.show(buttonWrapper, "INVITE");
                    }
                });


                buttonWrapper.add(inviteCtz, "INVITE");
                buttonWrapper.add(confirmCancelRq, "CANCEL");
                buttonWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonWrapper.getPreferredSize().height));
                ctzRequestPanel.add(Box.createVerticalStrut(30));
                ctzRequestPanel.add(buttonWrapper);
                ctzRequestPanel.add(Box.createVerticalStrut(2));
                ctzRequestPanel.add(confirmRequest);
                ctzRequestPanel.add(Box.createVerticalStrut(2));
                ctzRequestPanel.add(actionPanel);
                ctzRequestWrapperPanel.add(ctzRequestPanel);
                //Request queue
                JPanel rqQueueDisplay = new JPanel();
                rqQueueDisplay.setBorder(BorderFactory.createTitledBorder("Hàng đợi"));
                rqQueueDisplay.setLayout(new BorderLayout());

                ctzRequestWrapperPanel.add(rqQueueDisplay);
                dashboardPane.add(ctzRequestWrapperPanel, BorderLayout.EAST);

                //Request log
                JPanel rqLogDisplay = new JPanel();
                rqLogDisplay.setBorder(BorderFactory.createTitledBorder("Nhật ký tiếp công dân"));
                rqLogDisplay.setLayout(new BorderLayout());

                dashboardPane.add(rqLogDisplay, BorderLayout.CENTER);
                mainZone.add(dashboardPane);
            }
        });
    }
}
