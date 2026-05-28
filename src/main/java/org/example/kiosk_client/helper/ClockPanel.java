package org.example.kiosk_client.helper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClockPanel extends SwingWorker<Void, String> {
    private final DateTimeFormatter df;
    private final DateTimeFormatter dt;
    private final JLabel currentTime;

    public ClockPanel(JPanel parent_panel, GridBagConstraints layoutConstraint, Font font) {
        df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dt = DateTimeFormatter.ofPattern("HH:mm:ss");
        JPanel clockPanel = new JPanel();
        clockPanel.setBackground(Color.red);
        clockPanel.setLayout(new BorderLayout());
        currentTime = new JLabel();
        currentTime.setHorizontalAlignment(SwingConstants.CENTER);
        currentTime.setFont(font);
        currentTime.setForeground(new Color(255, 203, 5));
        clockPanel.add(currentTime, BorderLayout.CENTER);
        parent_panel.add(clockPanel, layoutConstraint);
    }

    @Override
    protected Void doInBackground() throws Exception {
        while(!isCancelled()) {
            String currentDOW = this.getDow();
            String currentDate = this.getCurrentDate();
            String currentTime = this.getCurrentTime();
            String res = "<html><div style='text-align: center;'>"
                    + currentDOW + "<br>"
                    + currentDate + "<br>"
                    + currentTime
                    + "</div></html>";
            publish(res);
            Thread.sleep(1000);
        }
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        try {
            String latestTime = chunks.get(chunks.size() - 1);
            currentTime.setText(latestTime);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getDow() {
        LocalDate today = LocalDate.now();
        int dow = today.getDayOfWeek().getValue();
        if(dow == 1) return "Thứ Hai";
        else if(dow == 2) return "Thứ Ba";
        else if(dow == 3) return "Thứ Tư";
        else if(dow == 4) return "Thứ Năm";
        else if(dow == 5) return "Thứ Sáu";
        else if(dow == 6) return "Thứ Bảy";
        else return "Chủ nhật";
    }
    public String getCurrentDate() {
        return LocalDate.now().format(df);
    }
    public String getCurrentTime() {
        return LocalTime.now().format(dt);
    }
}
