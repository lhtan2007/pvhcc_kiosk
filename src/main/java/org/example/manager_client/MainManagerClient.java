package org.example.manager_client;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonObject;
import org.example.manager_client.helper.ClockPanel;
import org.example.manager_client.helper.DataUpdater;
import org.example.manager_client.helper.XMLHelper;
import org.example.shared.helper.PwdHashHelper;
import org.example.manager_client.helper.NetworkInitializer;
import org.example.shared.helper.CustomSVGTranscoder;
import org.example.shared.model.Account;
import org.example.shared.model.CitizenRequest;
import org.example.shared.model.Department;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainManagerClient {
    private static final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(3);
    private static int currentInviteCount = 0;
    private static int role;
    private static JComboBox<Department> departmentList;
    private static String userName;
    private static final Map<Integer, JComponent[]> ROLE_PERMISSIONS = new HashMap<>();
    private static JPanel dashboardPane;
    private static JTextPane deptName;
    private static JLabel numOfProcessedRequestSrv, maxConcurrentRqInDaySrv;
    private static Department selectedDept;
    private static JLabel requestNumber, fullNameSrv, nationalIdSrv;
    private static JTable accountTable;
    public static CitizenRequest currentCtzRequest;
    public static DefaultTableModel rqLogTm, rqQueueTm, accountTm;
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    //Department updater
    private static final javax.swing.Timer departmentUpdaterTimer =
    new javax.swing.Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            DataUpdater.updateDepartment(departmentList);
        }
    });

    //Citizen's request updater
    private static final javax.swing.Timer ctzRequestUpdaterTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedDept = (Department)departmentList.getSelectedItem();
            if(selectedDept != null) {
                DataUpdater.updateRequest(selectedDept, requestNumber, fullNameSrv, nationalIdSrv);
                DataUpdater.updateDataToQueue(rqQueueTm, selectedDept);
                DataUpdater.updateDataToLog(rqLogTm, selectedDept);
                deptName.setText(selectedDept.getDepartmentName());
                numOfProcessedRequestSrv.setText(String.valueOf(selectedDept.getNumOfProcessedRequest()));
                maxConcurrentRqInDaySrv.setText(String.valueOf(selectedDept.getMaxConcurrentRequestInDay()));
            }
        }
    });

    //Account list updater
    private static final javax.swing.Timer accountListUpdaterTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            DataUpdater.updateAccount(accountTm, accountTable);
        }
    });

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
                mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                mainWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (userName == null || userName.trim().isEmpty()) {
                            mainWindow.dispose();
                            System.exit(0);
                            return;
                        }
                        mainWindow.setEnabled(false);
                        SwingWorker<JsonObject, Void> logoutWorker = new SwingWorker<JsonObject, Void>() {
                            @Override
                            protected JsonObject doInBackground() throws Exception {
                                JsonObject logoutRequest = new JsonObject();
                                logoutRequest.addProperty("clientType", "manager");
                                logoutRequest.addProperty("action", "LOGOUT");
                                logoutRequest.addProperty("data", userName);
                                return NetworkInitializer.getInstance().sendRequest(logoutRequest);
                            }
                            @Override
                            protected void done() {
                                try {
                                    JsonObject response = get();
                                    if(response == null) {
                                        JOptionPane.showMessageDialog(null,
                                                "Mất kết nối với máy chủ.", "Lỗi mạng",
                                                JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    if("error".equals(response.get("status").getAsString())) {
                                        mainWindow.dispose();
                                        System.exit(0);
                                    }
                                    else if("ok".equals(response.get("status").getAsString())) {
                                        boolean isLoggedOut = response.get("data").getAsBoolean();
                                        if(isLoggedOut) {
                                            mainWindow.dispose();
                                            System.exit(0);
                                        }
                                    }
                                    else {
                                        String error = response.get("message").getAsString();
                                        JOptionPane.showMessageDialog(null,
                                                "Lỗi hệ thống: " + error, "Thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(null,
                                            "Lỗi trong quá trình xử lý: " + e.getMessage(), "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                                finally {
                                    mainWindow.dispose();
                                    System.exit(0);
                                }
                            }
                        };
                        logoutWorker.execute();
                    }
                });
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
                            JOptionPane.showMessageDialog(null,
                                    "Thông tin đăng nhập không được bỏ trống. Xin vui lòng kiểm tra lại.",
                                    "Không thực hiện được yêu cầu", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        String hashedPwd = PwdHashHelper.hashPwd(new String(pwdChars));
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
                                        JOptionPane.showMessageDialog(null,
                                                "Mất kết nối với máy chủ.", "Lỗi mạng",
                                                JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    System.out.println("JSON: " + response);
                                    if("ok".equals(response.get("status").getAsString())) {
                                        JsonObject authData = response.getAsJsonObject("data");
                                        boolean isLoggedIn = authData.get("isLoggedIn").getAsBoolean();
                                        if(isLoggedIn) {
                                            role = authData.get("role").getAsInt();
                                            JComponent[] allowedJComponent = ROLE_PERMISSIONS.get(role);
                                            if(allowedJComponent != null) {
                                                for(JComponent component : allowedJComponent) {
                                                    component.setVisible(true);
                                                }
                                            }
                                            MainManagerClient.userName = authData.get("userName").getAsString();
                                            mainCardLayout.next(mainZone);
                                            departmentUpdaterTimer.start();
                                            ctzRequestUpdaterTimer.start();
                                            accountListUpdaterTimer.start();
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null,
                                                    "Thông tin đăng nhập không đúng hoặc tài khoản " +
                                                            "đang được sử dụng trên thiết bị khác. " +
                                                            "Vui lòng kiểm tra lại.", "Đăng nhập thất bại",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        usrNameInput.setText("");
                                        pwdInput.setText("");
                                    }
                                    else {
                                        String error = response.get("message").getAsString();
                                        JOptionPane.showMessageDialog(null,
                                                "Lỗi hệ thống: " + error, "Thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(null,
                                            "Lỗi trong quá trình xử lý: " + e.getMessage(), "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };
                        loginWorker.execute();
                    }
                });
                loginPane.add(loginButton);
                loginPane.add(Box.createVerticalGlue());
                mainZone.add(loginPane);

                dashboardPane = new JPanel();
                dashboardPane.setLayout(new BorderLayout());
                ClockPanel clock = new ClockPanel(dashboardPane,
                        UIManager.getFont("defaultFont").deriveFont(Font.BOLD | Font.ITALIC));
                clock.execute();

                //Button panel
                JPanel btnPanel = new JPanel();
                btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
                btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
                departmentList = new JComboBox<Department>();
                JButton importList = new JButton("Nhập danh sách đơn vị");
                JButton exportList = new JButton("Xuất danh sách đơn vị");
                JButton addDepartment = new JButton("Thêm đơn vị");
                JButton departmentInfo = new JButton("Thông tin đơn vị");
                JButton deleteDepartment = new JButton("Xóa đơn vị");
                JButton changePwd = new JButton("Đổi mật khẩu");
                JButton logout = new JButton("Đăng xuất");
                departmentList.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if(e.getStateChange() == ItemEvent.SELECTED) {
                            selectedDept = (Department)e.getItem();
                            if(selectedDept != null) {
                                deptName.setText(selectedDept.getDepartmentName());
                            }
                        }
                    }
                });
                importList.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        FileDialog fd = new FileDialog(mainWindow, "Nhập danh sách đơn vị", FileDialog.LOAD);
                        fd.setFile(LocalDateTime.now() + "_DSĐV.xml");
                        fd.setVisible(true);
                        String dir = fd.getDirectory();
                        String fn = fd.getFile();
                        if(!dir.equals("") && !fn.equals("") && dir != null && fn != null) {
                            File file = new File(dir, fn);
                            XMLHelper.importDepartmentsFromXML(file);
                        }
                    }
                });
                exportList.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        FileDialog fd = new FileDialog(mainWindow, "Lưu danh sách đơn vị", FileDialog.SAVE);
                        fd.setFile(LocalDateTime.now() + "_DSĐV.xml");
                        fd.setVisible(true);
                        String dir = fd.getDirectory();
                        String fn = fd.getFile();
                        if(!dir.equals("") && !fn.equals("") && dir != null && fn != null) {
                            if(!fn.toLowerCase().endsWith(".xml")) {
                                fn += ".xml";
                            }
                            File file = new File(dir, fn);
                            System.out.println(file.getAbsolutePath());
                            java.util.List<Department> dept = new ArrayList<Department>();
                            for(int i = 0; i < departmentList.getItemCount(); i++) {
                                Department d = departmentList.getItemAt(i);
                                dept.add(d);
                            }
                            XMLHelper.exportDepartmentsToXML(dept, file);
                        }
                    }
                });
                addDepartment.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JDialog addDepartmentDialog = new JDialog(mainWindow, "Thêm đơn vị", false);
                        JPanel addDepartmentPanel = (JPanel) addDepartmentDialog.getContentPane();
                        addDepartmentPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                        addDepartmentPanel.setLayout(new BoxLayout(addDepartmentPanel, BoxLayout.Y_AXIS));
                        JPanel fieldWrapper = new JPanel();
                        fieldWrapper.setLayout(new GridLayout(3, 2, 30, 10));
                        JLabel departmentName = new JLabel("Tên đơn vị");
                        fieldWrapper.add(departmentName);
                        JTextField departmentNameInput = new JTextField();
                        fieldWrapper.add(departmentNameInput);
                        JLabel maxConcurrentRequest = new JLabel("Số lượt tiếp công dân tối đa trong ngày");
                        fieldWrapper.add(maxConcurrentRequest);
                        JTextField maxConcurrentRequestInput = new JTextField();
                        fieldWrapper.add(maxConcurrentRequestInput);
                        addDepartmentPanel.add(fieldWrapper);
                        addDepartmentPanel.add(Box.createVerticalStrut(20));
                        JPanel btnWrapper = new JPanel();
                        btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
                        JButton ok = new JButton("OK");
                        JButton cancel = new JButton("Hủy bỏ");
                        btnWrapper.add(ok);
                        btnWrapper.add(Box.createHorizontalStrut(20));
                        btnWrapper.add(cancel);
                        addDepartmentPanel.add(btnWrapper);
                        ok.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ok.setEnabled(false);
                                DataUpdater.addDepartment(addDepartmentDialog, departmentNameInput.getText(),
                                        Integer.parseInt(maxConcurrentRequestInput.getText()));
                                addDepartmentDialog.dispose();
                            }
                        });
                        cancel.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                addDepartmentDialog.dispose();
                            }
                        });
                        addDepartmentDialog.pack();
                        addDepartmentDialog.setLocationRelativeTo(mainWindow);
                        addDepartmentDialog.setVisible(true);
                    }
                });
                departmentInfo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String deptId = ((Department)departmentList.getSelectedItem()).getDepartmentId().toString();
                        String orgDeptName = ((Department)departmentList.getSelectedItem()).getDepartmentName();
                        String orgDeptMaxConcurrentCtz =
                                String.valueOf(((Department)departmentList.getSelectedItem()).getMaxConcurrentRequestInDay());
                        JDialog departmentInfoDialog = new JDialog(mainWindow, "Thông tin đơn vị", false);
                        JPanel departmentInfoPanel = (JPanel) departmentInfoDialog.getContentPane();
                        departmentInfoPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                        departmentInfoPanel.setLayout(new BoxLayout(departmentInfoPanel, BoxLayout.Y_AXIS));
                        JPanel fieldWrapper = new JPanel();
                        fieldWrapper.setLayout(new GridLayout(3, 2, 30, 10));
                        JLabel departmentName = new JLabel("Tên đơn vị");
                        fieldWrapper.add(departmentName);
                        JTextField departmentNameInput = new JTextField();
                        departmentNameInput.setText(orgDeptName);
                        fieldWrapper.add(departmentNameInput);
                        JLabel maxConcurrentRequest = new JLabel("Số lượt tiếp công dân tối đa trong ngày");
                        fieldWrapper.add(maxConcurrentRequest);
                        JTextField maxConcurrentRequestInput = new JTextField();
                        maxConcurrentRequestInput.setText(orgDeptMaxConcurrentCtz);
                        fieldWrapper.add(maxConcurrentRequestInput);
                        departmentInfoPanel.add(fieldWrapper);
                        departmentInfoPanel.add(Box.createVerticalStrut(20));
                        JPanel btnWrapper = new JPanel();
                        btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
                        JButton exportListCtzRequest = new JButton("Xuất DS lượt tiếp công dân");
                        JButton ok = new JButton("OK");
                        JButton cancel = new JButton("Hủy bỏ");
                        btnWrapper.add(exportListCtzRequest);
                        btnWrapper.add(Box.createHorizontalStrut(20));
                        btnWrapper.add(ok);
                        btnWrapper.add(Box.createHorizontalStrut(20));
                        btnWrapper.add(cancel);
                        departmentInfoPanel.add(btnWrapper);
                        exportListCtzRequest.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                FileDialog fd = new FileDialog(mainWindow, "Lưu danh sách lượt tiếp công dân",
                                        FileDialog.SAVE);
                                fd.setFile(LocalDateTime.now() + "_"
                                        + ((Department) departmentList.getSelectedItem()).getDepartmentName()
                                        + "_DSTCD.xml");
                                fd.setVisible(true);
                                String dir = fd.getDirectory();
                                String fn = fd.getFile();
                                if(!dir.equals("") && !fn.equals("") && dir != null && fn != null) {
                                    if(!fn.toLowerCase().endsWith(".xml")) {
                                        fn += ".xml";
                                    }
                                    File file = new File(dir, fn);
                                    System.out.println(file.getAbsolutePath());
                                    java.util.List<CitizenRequest> ctzRequests = new ArrayList<>();
                                    for(int i = 0; i < rqLogTm.getRowCount(); i++) {
                                        CitizenRequest cr = new CitizenRequest();
                                        cr.setRequestDate((LocalDateTime)rqLogTm.getValueAt(i, 0));
                                        cr.setFullName((String)rqLogTm.getValueAt(i, 1));
                                        cr.setNationalId((String)rqLogTm.getValueAt(i, 2));
                                        cr.setRequestNumber((Integer)rqLogTm.getValueAt(i, 3));
                                        int status;
                                        if(rqLogTm.getValueAt(i, 4).equals("Đang chờ")) {
                                            status = 0;
                                        }
                                        else if(rqLogTm.getValueAt(i, 4).equals("Đã xử lý")) {
                                            status = 1;
                                        }
                                        else status = -1;
                                        cr.setProcessStatus(status);
                                        ctzRequests.add(cr);
                                    }
                                    XMLHelper.exportCitizenRequestsToXML(
                                            (Department)departmentList.getSelectedItem(),
                                            ctzRequests, file);
                                }
                                departmentInfoDialog.dispose();
                            }
                        });
                        ok.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ok.setEnabled(false);
                                DataUpdater.editDepartment(departmentInfoDialog, deptId, departmentNameInput.getText(),
                                        Integer.parseInt(maxConcurrentRequestInput.getText()));
                                departmentInfoDialog.dispose();
                            }
                        });
                        cancel.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                departmentInfoDialog.dispose();
                            }
                        });
                        departmentInfoDialog.pack();
                        departmentInfoDialog.setLocationRelativeTo(mainWindow);
                        departmentInfoDialog.setVisible(true);
                    }
                });
                deleteDepartment.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String deptId = ((Department)departmentList.getSelectedItem()).getDepartmentId().toString();
                        String[] option = {"OK", "Hủy bỏ"};
                        int sel_opt = JOptionPane.showOptionDialog(mainWindow, "Bạn có chắc chắn muốn xóa " +
                                "đơn vị này không?", "Xóa đơn vị", JOptionPane.DEFAULT_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, option, option[0]);
                        switch (sel_opt) {
                            case 0:
                                DataUpdater.deleteDepartment(mainWindow, deptId);
                                break;
                            case 1:
                            case JOptionPane.CLOSED_OPTION:
                                break;
                        }
                    }
                });
                changePwd.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JDialog changePwdDialog = new JDialog(mainWindow, "Đổi mật khẩu", false);
                        JPanel changePwdPanel = (JPanel)changePwdDialog.getContentPane();
                        changePwdPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        changePwdPanel.setLayout(new BoxLayout(changePwdPanel, BoxLayout.Y_AXIS));
                        JPanel fieldWrapper = new JPanel();
                        fieldWrapper.setLayout(new GridLayout(3, 2, 30, 10));
                        JLabel accName = new JLabel("Tên tài khoản");
                        fieldWrapper.add(accName);
                        JTextField accNameSrv = new JTextField(userName);
                        fieldWrapper.add(accNameSrv);
                        JLabel newPwd = new JLabel("Mật khẩu mới");
                        fieldWrapper.add(newPwd);
                        JPasswordField newPwdInput = new JPasswordField();
                        fieldWrapper.add(newPwdInput);
                        JLabel retypePwd = new JLabel("Nhập lại mật khẩu mới");
                        fieldWrapper.add(retypePwd);
                        JPasswordField retypePwdInput = new JPasswordField();
                        fieldWrapper.add(retypePwdInput);
                        changePwdPanel.add(fieldWrapper);
                        changePwdPanel.add(Box.createVerticalStrut(20));
                        JPanel btnWrapper = new JPanel();
                        btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
                        JButton ok = new JButton("OK");
                        JButton cancel = new JButton("Hủy bỏ");
                        btnWrapper.add(ok);
                        btnWrapper.add(Box.createHorizontalStrut(20));
                        btnWrapper.add(cancel);
                        changePwdPanel.add(btnWrapper);
                        ok.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ok.setEnabled(false);
                                DataUpdater.changePassword(changePwdDialog, accNameSrv.getText(),
                                        new String(newPwdInput.getPassword()), new String(retypePwdInput.getPassword()));
                                changePwdDialog.dispose();
                            }
                        });
                        cancel.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                changePwdDialog.dispose();
                            }
                        });
                        changePwdDialog.pack();
                        changePwdDialog.setLocationRelativeTo(mainWindow);
                        changePwdDialog.setVisible(true);
                    }
                });
                logout.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        logout.setEnabled(false);
                        SwingWorker<JsonObject, Void> logoutWorker = new SwingWorker<JsonObject, Void>() {
                            @Override
                            protected JsonObject doInBackground() throws Exception {
                                JsonObject logoutRequest = new JsonObject();
                                logoutRequest.addProperty("clientType", "manager");
                                logoutRequest.addProperty("action", "LOGOUT");
                                logoutRequest.addProperty("data", userName);
                                return NetworkInitializer.getInstance().sendRequest(logoutRequest);
                            }
                            @Override
                            protected void done() {
                                try {
                                    JsonObject response = get();
                                    if(response == null) {
                                        JOptionPane.showMessageDialog(null,
                                                "Mất kết nối với máy chủ.", "Lỗi mạng",
                                                JOptionPane.ERROR_MESSAGE);
                                        logout.setEnabled(true);
                                        return;
                                    }
                                    System.out.println("JSON: " + response);
                                    if("ok".equals(response.get("status").getAsString())) {
                                        boolean isLoggedOut = response.get("data").getAsBoolean();
                                        if(isLoggedOut) {
                                            JComponent[] allowedJComponent = ROLE_PERMISSIONS.get(role);
                                            if(allowedJComponent != null) {
                                                for(JComponent component : allowedJComponent) {
                                                    component.setVisible(false);
                                                }
                                            }
                                            mainCardLayout.next(mainZone);
                                            logout.setEnabled(true);
                                        }
                                    }
                                    else {
                                        String error = response.get("message").getAsString();
                                        JOptionPane.showMessageDialog(null,
                                                "Lỗi hệ thống: " + error, "Thất bại",
                                                JOptionPane.ERROR_MESSAGE);
                                        logout.setEnabled(true);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(null,
                                            "Lỗi trong quá trình xử lý: " + e.getMessage(), "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };
                        logoutWorker.execute();
                    }
                });
                btnPanel.add(departmentList);
                btnPanel.add(importList);
                btnPanel.add(exportList);
                btnPanel.add(addDepartment);
                btnPanel.add(departmentInfo);
                btnPanel.add(deleteDepartment);
                btnPanel.add(changePwd);
                btnPanel.add(logout);
                importList.setVisible(false);
                exportList.setVisible(false);
                addDepartment.setVisible(false);
                deleteDepartment.setVisible(false);
                JScrollPane btnScr = new JScrollPane(btnPanel);
                btnScr.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                btnScr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                btnScr.setBorder(BorderFactory.createEmptyBorder());
                dashboardPane.add(btnScr, BorderLayout.NORTH);

                //Department information and account manager panel
                JPanel deptPanel = new JPanel();
                deptPanel.setLayout(new BoxLayout(deptPanel, BoxLayout.Y_AXIS));
                JPanel deptInfoPanel = new JPanel();
                deptInfoPanel.setLayout(new BoxLayout(deptInfoPanel, BoxLayout.Y_AXIS));
                deptInfoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin đơn vị"));
                deptInfoPanel.setMinimumSize(new Dimension(400, 250));
                deptInfoPanel.setPreferredSize(new Dimension(400, 250));
                deptInfoPanel.setMaximumSize(deptInfoPanel.getPreferredSize());
                deptInfoPanel.add(Box.createVerticalStrut(30));
                deptName = new JTextPane();
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
                deptInfoPanel.add(deptName);
                JPanel deptDetailPanel = new JPanel();
                GridBagLayout deptDetailPanelGbl = new GridBagLayout();
                GridBagConstraints deptDetailPanelGbc = new GridBagConstraints();
                deptDetailPanel.setLayout(deptDetailPanelGbl);
                JLabel numOfProcessedRequest = new JLabel("Số lượt công dân đến làm thủ tục đã xử lý");
                numOfProcessedRequestSrv = new JLabel();
                JLabel maxConcurrentRqInDay = new JLabel("Số lượt tiếp công dân tối đa trong ngày");
                maxConcurrentRqInDaySrv = new JLabel();
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

                JPanel deptAccountManagerPanel = new JPanel();
                deptAccountManagerPanel.setLayout(new BorderLayout());
                deptAccountManagerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                deptAccountManagerPanel.setPreferredSize(new Dimension(400, 400));
                deptAccountManagerPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
                deptAccountManagerPanel.setBorder(BorderFactory.createTitledBorder("Quản lý tài khoản"));
                deptAccountManagerPanel.setVisible(false);

                JPanel accountButtonWrapper = new JPanel();
                accountButtonWrapper.setLayout(new BoxLayout(accountButtonWrapper, BoxLayout.X_AXIS));
                JButton createAccount = new JButton("Tạo tài khoản");
                JButton deleteAccount = new JButton("Xóa tài khoản");
                deleteAccount.setEnabled(false);
                accountButtonWrapper.add(createAccount);
                accountButtonWrapper.add(deleteAccount);
                createAccount.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JDialog createAccountDialog = new JDialog(mainWindow, "Tạo tài khoản", true);
                        JPanel createAccountPanel = (JPanel)createAccountDialog.getContentPane();
                        createAccountPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        createAccountPanel.setLayout(new BoxLayout(createAccountPanel, BoxLayout.Y_AXIS));
                        JPanel fieldWrapper = new JPanel();
                        fieldWrapper.setLayout(new GridLayout(3, 2, 30, 15)); // 2 dòng, 2 cột
                        JLabel lblUsername = new JLabel("Tên đăng nhập");
                        JTextField txtUsernameInput = new JTextField(15);
                        JLabel lblPassword = new JLabel("Mật khẩu");
                        JPasswordField txtPasswordInput = new JPasswordField(15);
                        JLabel role = new JLabel("Vai trò");
                        JComboBox<String> roleInput = new JComboBox<>();
                        roleInput.addItem("Quản trị viên");
                        roleInput.addItem("Người dùng");
                        roleInput.setSelectedIndex(0);
                        fieldWrapper.add(lblUsername);
                        fieldWrapper.add(txtUsernameInput);
                        fieldWrapper.add(lblPassword);
                        fieldWrapper.add(txtPasswordInput);
                        fieldWrapper.add(role);
                        fieldWrapper.add(roleInput);
                        createAccountPanel.add(fieldWrapper);
                        createAccountPanel.add(Box.createVerticalStrut(20));
                        JPanel btnWrapper = new JPanel();
                        btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
                        JButton btnOk = new JButton("OK");
                        JButton btnCancel = new JButton("Hủy bỏ");
                        btnWrapper.add(btnOk);
                        btnWrapper.add(Box.createHorizontalStrut(20));
                        btnWrapper.add(btnCancel);
                        createAccountPanel.add(btnWrapper);
                        btnCancel.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                createAccountDialog.dispose();
                            }
                        });
                        btnOk.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String username = txtUsernameInput.getText().trim();
                                char[] passwordChars = txtPasswordInput.getPassword();
                                String password = new String(passwordChars).trim();
                                String rawRole = roleInput.getSelectedItem().toString();
                                System.out.println(rawRole);
                                int role = -1;
                                switch(rawRole) {
                                    case "Quản trị viên":
                                        role = 0;
                                        break;
                                    case "Người dùng":
                                        role = 1;
                                        break;
                                }
                                if(username.isEmpty()) {
                                    JOptionPane.showMessageDialog(createAccountDialog,
                                            "Tên đăng nhập không được bỏ trống. Vui lòng kiểm tra lại.",
                                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                if(password.isEmpty()) {
                                    JOptionPane.showMessageDialog(createAccountDialog,
                                            "Mật khẩu không được bỏ trống. Vui lòng kiểm tra lại.",
                                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                btnOk.setEnabled(false);
                                String hashedPassword = PwdHashHelper.hashPwd(password);
                                DataUpdater.createAccount(mainWindow, username, hashedPassword, role);
                                createAccountDialog.dispose();
                            }
                        });
                        createAccountDialog.pack();
                        createAccountDialog.setResizable(false);
                        createAccountDialog.setLocationRelativeTo(mainWindow);
                        createAccountDialog.setVisible(true);
                    }
                });
                deleteAccount.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DataUpdater.deleteAccount(mainWindow, accountTable, accountTm); //
                    }
                });
                deptAccountManagerPanel.add(accountButtonWrapper, BorderLayout.NORTH);

                String[] accountTblColNames = {"Tên đăng nhập", "Vai trò"};
                accountTm = new DefaultTableModel(accountTblColNames, 0) {
                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return false;
                    }
                    @Override
                    public Class getColumnClass(int columnIndex) {
                        if(columnIndex == 1) return Integer.class;
                        return String.class;
                    }
                };
                accountTable = new JTable(accountTm);
                TableColumnModel accountTblColModel = accountTable.getColumnModel();
                accountTable.setAutoCreateRowSorter(true);
                accountTable.setRowHeight(25);
                accountTable.getTableHeader().setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD));
                accountTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        deleteAccount.setEnabled(accountTable.getSelectedRow() != -1);
                    }
                });
                accountTblColModel.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    protected void setValue(Object value) {
                        if(value instanceof Integer) {
                            if(((Integer)value).intValue() == 0) {
                                setText("Quản trị viên");
                            }
                            else {
                                setText("Người dùng");
                            }
                        }
                        else {
                            super.setValue(value);
                        }
                    }
                });
                accountTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        int selectedRowView = accountTable.getSelectedRow();
                        if (selectedRowView != -1) {
                            int selectedRowModel = accountTable.convertRowIndexToModel(selectedRowView);
                            String username = (String)accountTm.getValueAt(selectedRowModel, 0);
                            Integer role = (Integer)accountTm.getValueAt(selectedRowModel, 1);
                            Account selectedAccount = new Account(username, null, role);
                            System.out.println(username);
                        }
                    }
                });
                JScrollPane accountTableScr = new JScrollPane(accountTable);
                deptAccountManagerPanel.add(accountTableScr, BorderLayout.CENTER);

                deptPanel.add(deptInfoPanel);
                deptPanel.add(deptAccountManagerPanel);
                dashboardPane.add(deptPanel, BorderLayout.WEST);
                //Citizen's request approval panel
                JPanel ctzRequestWrapperPanel = new JPanel();
                ctzRequestWrapperPanel.setLayout(new BoxLayout(ctzRequestWrapperPanel, BoxLayout.Y_AXIS));
                ctzRequestWrapperPanel.setPreferredSize(new Dimension(300, 0));

                JPanel ctzRequestPanel = new JPanel();
                ctzRequestPanel.setLayout(new BoxLayout(ctzRequestPanel, BoxLayout.Y_AXIS));
                ctzRequestPanel.setBorder(BorderFactory.createTitledBorder("Lượt hiện tại"));
                ctzRequestPanel.add(Box.createVerticalStrut(50));
                requestNumber = new JLabel();
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
                fullNameSrv = new JLabel();
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
                nationalIdSrv = new JLabel();
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
                            System.out.println("Xin mời công dân " + currentCtzRequest.getFullName() + ", số thứ tự "
                                + currentCtzRequest.getRequestNumber() + " đến thực hiện thủ tục.");
                            //TODO: Replace console output by TTS
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
                        backgroundExecutor.submit(() -> {
                            DataUpdater.confirmAdmittedRequest(currentCtzRequest);
                        });
                    }
                });
                confirmCancelRq.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentInviteCount = 0;
                        backgroundExecutor.submit(() -> {
                            DataUpdater.confirmCancelledRequest(currentCtzRequest);
                        });
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

                String[] rqQueueTableColumnNames = {"STT", "Thời gian"};
                rqQueueTm = new DefaultTableModel(rqQueueTableColumnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                    @Override
                    public Class<?> getColumnClass(int col) {
                        if(col == 0) return Integer.class;
                        if(col == 1) return LocalDateTime.class;
                        return String.class;
                    }
                };
                JTable rqQueueTable = new JTable(rqQueueTm);
                rqQueueTable.setAutoCreateRowSorter(true);
                rqQueueTable.setFocusable(false);
                rqQueueTable.setRowSelectionAllowed(false);
                rqQueueTable.setCellSelectionEnabled(false);
                rqQueueTable.setRowHeight(25);
                rqQueueTable.getTableHeader().setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD));
                TableColumnModel rqQueueTblCm = rqQueueTable.getColumnModel();
                rqQueueTblCm.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    protected void setValue(Object val) {
                        if(val instanceof LocalDateTime) {
                            setText(((LocalDateTime)val).format(formatter));
                        }
                        else {
                            super.setValue(val);
                        }
                    }
                });
                rqQueueTblCm.getColumn(0).setMinWidth(50);
                rqQueueTblCm.getColumn(0).setPreferredWidth(50);
                rqQueueTblCm.getColumn(0).setMaxWidth(50);

                JScrollPane rqQueueScrPane = new JScrollPane(rqQueueTable);
                rqQueueDisplay.add(rqQueueScrPane, BorderLayout.CENTER);
                ctzRequestWrapperPanel.add(rqQueueDisplay);
                dashboardPane.add(ctzRequestWrapperPanel, BorderLayout.EAST);

                //Request log
                JPanel rqLogDisplay = new JPanel();
                rqLogDisplay.setBorder(BorderFactory.createTitledBorder("Nhật ký tiếp công dân"));
                rqLogDisplay.setLayout(new BorderLayout());

                String[] rqLogTableColumnNames = {"Thời gian", "Họ và tên", "Số ĐDCN", "STT", "Trạng thái"};
                rqLogTm = new DefaultTableModel(rqLogTableColumnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                    @Override
                    public Class<?> getColumnClass(int col) {
                        if(col == 0) return LocalDateTime.class;
                        if(col == 3) return Integer.class;
                        return String.class;
                    }
                };
                JTable rqLogTable = new JTable(rqLogTm);
                TableColumnModel rqLogTblCm = rqLogTable.getColumnModel();
                rqLogTable.setFocusable(false);
                rqLogTable.setRowSelectionAllowed(false);
                rqLogTable.setCellSelectionEnabled(false);
                rqLogTable.setAutoCreateRowSorter(true);
                rqLogTable.setRowHeight(25);
                rqLogTable.getTableHeader().setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD));
                rqLogTblCm.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    protected void setValue(Object val) {
                        if(val instanceof LocalDateTime) {
                            setText(((LocalDateTime)val).format(formatter));
                        }
                        else {
                            super.setValue(val);
                        }
                    }
                });
                JScrollPane rqLogScrPane = new JScrollPane(rqLogTable);
                rqLogDisplay.add(rqLogScrPane, BorderLayout.CENTER);
                dashboardPane.add(rqLogDisplay, BorderLayout.CENTER);
                mainZone.add(dashboardPane);
                ROLE_PERMISSIONS.put(0, new JComponent[]{importList, exportList, addDepartment, deleteDepartment,
                        deptAccountManagerPanel});
            }
        });
    }
}
