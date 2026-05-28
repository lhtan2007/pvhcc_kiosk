package org.example.kiosk_client.helper;

import javax.swing.*;
import java.awt.*;

public class ValidationHelper {
    private static Popup currentPopup;
    public static void showErrorTooltip(JTextField nationalIdInput, String msg) {
        if(currentPopup != null) currentPopup.hide();
        JToolTip toolTip = new JToolTip();
        toolTip.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC, 14));
        toolTip.setTipText(msg);
        toolTip.setBackground(new Color(255, 230, 230));
        toolTip.setForeground(Color.RED);
        toolTip.setBorder(BorderFactory.createLineBorder(Color.RED));
        try {
            Point location = nationalIdInput.getLocationOnScreen();
            int x = location.x;
            // Đặt y ngay mép dưới của Textbox
            int y = location.y + nationalIdInput.getHeight();

            // 4. Hiển thị Popup
            PopupFactory popupFactory = PopupFactory.getSharedInstance();
            currentPopup = popupFactory.getPopup(nationalIdInput, toolTip, x, y);
            currentPopup.show();

            // 5. Tự động ẩn Tooltip sau 2.5 giây (2500ms) để không làm vướng víu giao diện
            Timer timer = new Timer(2500, e -> {
                if (currentPopup != null) {
                    currentPopup.hide();
                    currentPopup = null;
                }
            });
            timer.setRepeats(false);
            timer.start();

        }
        catch (IllegalComponentStateException e) {

        }
    }
}
