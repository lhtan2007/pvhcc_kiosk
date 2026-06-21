package org.example.kiosk_client;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.kiosk_client.controller.DepartmentController;
import org.example.kiosk_client.helper.*;
import org.example.kiosk_client.view.ConfigDialog;
import org.example.kiosk_client.view.DepartmentView;
import org.example.shared.helper.CustomSVGTranscoder;
import org.example.shared.model.Department;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MainKiosk {
    private static Thread connector;
    private static KioskNetConnector connectorInstance;
    public static Thread getConnector() {
        return connector;
    }
    public static KioskNetConnector getConnectorInstance() {
        return connectorInstance;
    }
    public static void main(String[] args) {
        //FlatLaf
        System.setProperty("flatlaf.useWindowDecorations", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf() );
        }
        catch( Exception ex ) {
            System.err.println("Failed to initialize LaF");
        }
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 24));

        //Connection initializer
        connectorInstance = new KioskNetConnector();
        connector = new Thread(connectorInstance);
        connector.start();

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
                JFrame mainWindow = new JFrame();
                mainWindow.setVisible(true);
                mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                JPanel mainPane = (JPanel)mainWindow.getContentPane();
                mainPane.setPreferredSize(new Dimension(1024, 768));
                mainPane.setLayout(new BorderLayout());
                mainWindow.pack();
                mainWindow.setLocationRelativeTo(null);

                //Banner zone
                JPanel banner = new JPanel();
                banner.setLayout(new GridBagLayout());
                banner.setBackground(Color.red);
                GridBagConstraints banner_gbc = new GridBagConstraints();
                banner.setPreferredSize(new Dimension(-1, 150));
                CustomSVGTranscoder transcoder = new CustomSVGTranscoder();
                ImageIcon logo = transcoder.getIcon("mainicon.svg", 138, 128);
                JLabel bannerPlaceholder = new JLabel(logo);
                bannerPlaceholder.setForeground(new Color(255, 203, 5));
                bannerPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
                bannerPlaceholder.setText("<html>"
                        + "TRUNG TÂM PHỤC VỤ HÀNH CHÍNH CÔNG <br/>"
                        + "XÃ ...... <br/>"
                        + "</html>");
                bannerPlaceholder.setFont(svnKelsonBold);

                bannerPlaceholder.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            ConfigDialog configDialog = new ConfigDialog(mainWindow);
                            configDialog.setVisible(true);
                        }
                    }
                });

                banner_gbc.gridx = 0;
                banner_gbc.weightx = 0.8;
                banner_gbc.fill = GridBagConstraints.HORIZONTAL;
                banner.add(bannerPlaceholder, banner_gbc);
                banner_gbc.gridx = 1;
                banner_gbc.weightx = 0.2;
                banner_gbc.fill = GridBagConstraints.HORIZONTAL;
                ClockPanel clockPanel = new ClockPanel(banner, banner_gbc, new Font("Segoe UI", Font.BOLD, 24));
                clockPanel.execute();
                mainPane.add(banner, BorderLayout.NORTH);

                //General pane
                JPanel generalPane = new JPanel();
                generalPane.setLayout(new GridLayout(2,1));
                mainPane.add(generalPane, BorderLayout.CENTER);

                //Citizen information input zone
                JPanel citizenInfInput = new JPanel();
                citizenInfInput.setLayout(new BoxLayout(citizenInfInput, BoxLayout.Y_AXIS));
                citizenInfInput.add(Box.createVerticalGlue());
                JTextPane notice = new JTextPane();
                SimpleAttributeSet notice_sas = new SimpleAttributeSet();
                StyleConstants.setAlignment(notice_sas, StyleConstants.ALIGN_CENTER);
                StyledDocument notice_doc = notice.getStyledDocument();
                notice_doc.setParagraphAttributes(0, notice_doc.getLength(), notice_sas, false);
                notice.setEditable(false);
                notice.setCursor(null);
                notice.setOpaque(false);
                notice.setFocusable(false);
                notice.setFont(UIManager.getFont("Label.font"));
                notice.setForeground(UIManager.getColor("Label.foreground"));
                notice.setAlignmentX(Component.CENTER_ALIGNMENT);
                notice.setText("Công dân vui lòng nhập thông tin cá nhân\n và chọn cơ quan thực hiện thủ tục hành chính");
                notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, notice.getPreferredSize().height));
                citizenInfInput.add(notice);
                citizenInfInput.add(Box.createRigidArea(new Dimension(0, 50)));

                JPanel citizenInputFormWrapper = new JPanel();
                citizenInputFormWrapper.setLayout(new GridBagLayout());
                citizenInputFormWrapper.setOpaque(false);
                GridBagConstraints citizenInputFormWrapper_gbc = new GridBagConstraints();
                citizenInputFormWrapper_gbc.gridx = 0;
                citizenInputFormWrapper_gbc.weightx = 0.2;
                citizenInputFormWrapper_gbc.fill = GridBagConstraints.BOTH;
                citizenInputFormWrapper.add(Box.createHorizontalGlue(), citizenInputFormWrapper_gbc);
                JPanel citizenInputForm = new JPanel();
                citizenInputForm.setLayout(new GridLayout(2, 2, 30, 5));
                JLabel citizenFullName = new JLabel("Họ và tên");
                citizenInputForm.add(citizenFullName);
                JTextField citizenFullNameInput = new JTextField();
                citizenInputForm.add(citizenFullNameInput);
                JLabel citizenNationalId = new JLabel("Số định danh cá nhân");
                citizenInputForm.add(citizenNationalId);
                JTextField citizenNationalIdInput = new JTextField();
                ((AbstractDocument)citizenNationalIdInput.getDocument()).setDocumentFilter(new NationalIdFilter(citizenNationalIdInput));
                citizenInputForm.add(citizenNationalIdInput);
                citizenInputFormWrapper_gbc.gridx = 1;
                citizenInputFormWrapper_gbc.weightx = 0.6;
                citizenInputFormWrapper_gbc.fill = GridBagConstraints.HORIZONTAL;
                citizenInputFormWrapper.add(citizenInputForm, citizenInputFormWrapper_gbc);
                citizenInputFormWrapper_gbc.gridx = 2;
                citizenInputFormWrapper_gbc.weightx = 0.2;
                citizenInputFormWrapper_gbc.fill = GridBagConstraints.BOTH;
                citizenInputFormWrapper.add(Box.createHorizontalGlue(), citizenInputFormWrapper_gbc);
                citizenInputFormWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
                int maxFormWidth = 600;
                int maxWrapperWidth = (int)(maxFormWidth/0.6);
                int formHeight = citizenInputForm.getPreferredSize().height;
                citizenInputFormWrapper.setMaximumSize(new Dimension(maxWrapperWidth, formHeight));
                citizenInfInput.add(citizenInputFormWrapper);
                citizenInfInput.add(Box.createVerticalGlue());
                generalPane.add(citizenInfInput);

                //Department selector
                JPanel departmentListPanel = new JPanel();
                departmentListPanel.setLayout(new GridLayout(2, 0, 10, 10));

                JLabel loadingLabel = new JLabel("Đang tải danh sách cơ quan...");
                loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
                loadingLabel.setFont(UIManager.getFont("Label.font"));
                departmentListPanel.add(loadingLabel);

                JScrollPane departmentScrPane = new JScrollPane(departmentListPanel);
                departmentScrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                departmentScrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                departmentScrPane.getHorizontalScrollBar().setUnitIncrement(20);
                departmentScrPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                generalPane.add(departmentScrPane);

                final java.util.List<Department> currentDisplayedDepartments = new java.util.ArrayList<>();

                Timer departmentListTimer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SwingWorker<java.util.List<Department>, Void> departmentListWorker = new SwingWorker<java.util.List<Department>, Void>() {
                            @Override
                            protected java.util.List<Department> doInBackground() throws Exception {
                                return DepartmentHelper.fetchDepartmentList();
                            }
                            @Override
                            protected void done() {
                                try {
                                    java.util.List<Department> remoteDepartments = get();
                                    if (remoteDepartments == null || remoteDepartments.isEmpty()) {
                                        if (currentDisplayedDepartments.size() != 0) {
                                            departmentListPanel.removeAll();
                                            JLabel errorLabel = new JLabel("Không thể tải danh sách. Vui lòng thử lại sau.");
                                            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                                            errorLabel.setForeground(Color.RED);
                                            departmentListPanel.add(errorLabel);
                                            departmentListPanel.revalidate();
                                            departmentListPanel.repaint();
                                            currentDisplayedDepartments.clear();
                                        }
                                        return;
                                    }
                                    boolean isChanged = false;
                                    if(currentDisplayedDepartments.size() != remoteDepartments.size()) {
                                        isChanged = true;
                                    }
                                    else {
                                        for (int i = 0; i < remoteDepartments.size(); i++) {
                                            String currentId = currentDisplayedDepartments.get(i).getDepartmentId().toString();
                                            String remoteId = remoteDepartments.get(i).getDepartmentId().toString();
                                            if (!currentId.equals(remoteId)) {
                                                isChanged = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(isChanged) {
                                        departmentListPanel.removeAll();
                                        for (Department dept : remoteDepartments) {
                                            DepartmentView deptView = new DepartmentView(dept);
                                            deptView.setFocusable(false);
                                            new DepartmentController(citizenFullNameInput, citizenNationalIdInput, deptView);
                                            departmentListPanel.add(deptView);
                                        }
                                        departmentListPanel.revalidate();
                                        departmentListPanel.repaint();
                                        currentDisplayedDepartments.clear();
                                        currentDisplayedDepartments.addAll(remoteDepartments);
                                    }
                                }
                                catch(Exception ex) {
                                    System.err.println("Lỗi đồng bộ giao diện Kiosk: " + ex.getMessage());
                                }
                            }
                        };
                        departmentListWorker.execute();
                    }
                });

                departmentListTimer.setRepeats(true);
                departmentListTimer.start();

                //Safely shutdown
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    NetworkInitializer.getInstance().closeConnection();
                }));
            }
        });
    }
}
