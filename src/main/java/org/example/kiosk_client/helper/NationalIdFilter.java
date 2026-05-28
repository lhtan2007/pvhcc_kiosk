package org.example.kiosk_client.helper;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class NationalIdFilter extends DocumentFilter {
    private final int maxLength;
    private final String regexPattern;
    private final JTextField nationalIdInput;

    public NationalIdFilter(JTextField nationalIdInput) {
        this.maxLength = 12;
        this.regexPattern = "^\\d*$";
        this.nationalIdInput = nationalIdInput;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;
        replace(fb, offset, 0, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) return;

        if (!text.matches(regexPattern)) {
            Toolkit.getDefaultToolkit().beep();
            ValidationHelper.showErrorTooltip(nationalIdInput, "Số định danh cá nhân chỉ bao gồm các chữ số từ 0-9.");
            return;
        }

        int currentLength = fb.getDocument().getLength();
        int futureLength = currentLength - length + text.length();

        if (futureLength <= maxLength) {
            super.replace(fb, offset, length, text, attrs);
        }
        else {
            Toolkit.getDefaultToolkit().beep();
            ValidationHelper.showErrorTooltip(nationalIdInput, "Số định danh cá nhân không dài hơn 12 chữ số.");
            int allowedLength = maxLength - (currentLength - length);
            if (allowedLength > 0) {
                super.replace(fb, offset, length, text.substring(0, allowedLength), attrs);
            }
        }
    }
}
