package org.example.kiosk_client.helper;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TicketPrintHelper implements Printable {
    private final String departmentName;
    private final LocalDateTime requestDate;
    private final String fullName;
    private final String nationalId;
    private final String requestNumber;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    public TicketPrintHelper(String requestNumber, String departmentName, LocalDateTime requestDate, String fullName, String nationalId) {
        this.requestNumber = requestNumber;
        this.departmentName = departmentName;
        this.requestDate = requestDate;
        this.fullName = fullName;
        this.nationalId = nationalId;
    }
    private enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT,
        JUSTIFY
    }
    private void drawTextAsShape(Graphics2D g2d, String text, float x, float y) {
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = g2d.getFont().createGlyphVector(frc, text);
        Shape textShape = gv.getOutline(x, y);
        g2d.fill(textShape);
    }
    private void drawMultiLineString(Graphics2D g2d, String text, int startX, int startY, int maxWidth, TextAlignment align, int lineSpacing) {
        FontMetrics metrics = g2d.getFontMetrics();
        int lineHeight = metrics.getHeight() + lineSpacing;
        java.util.List<String> wrappedLines = new ArrayList<>();
        java.util.List<Boolean> isLastLineInParagraph = new ArrayList<>();
        String[] explicitLines = text.split("\n");
        for (String explicitLine : explicitLines) {
            if (explicitLine.isEmpty()) {
                wrappedLines.add("");
                isLastLineInParagraph.add(true);
                continue;
            }
            String[] words = explicitLine.split(" ");
            StringBuilder currentLine = new StringBuilder(words[0]);
            for (int i = 1; i < words.length; i++) {
                String word = words[i];
                String testLine = currentLine.toString() + " " + word;
                if (metrics.stringWidth(testLine) <= maxWidth) {
                    currentLine.append(" ").append(word);
                }
                else {
                    wrappedLines.add(currentLine.toString());
                    isLastLineInParagraph.add(false);
                    currentLine = new StringBuilder(word);
                }
            }
            wrappedLines.add(currentLine.toString());
            isLastLineInParagraph.add(true);
        }
        for (int i = 0; i < wrappedLines.size(); i++) {
            String line = wrappedLines.get(i).trim();
            boolean isLastLine = isLastLineInParagraph.get(i);
            if (line.isEmpty()) {
                startY += lineHeight;
                continue;
            }
            int currentX = startX;
            if (align == TextAlignment.CENTER) {
                currentX = startX + (maxWidth - metrics.stringWidth(line)) / 2;
            }
            else if (align == TextAlignment.RIGHT) {
                currentX = startX + (maxWidth - metrics.stringWidth(line));
            }
            if (align == TextAlignment.JUSTIFY) {
                if(isLastLine) {
                    drawTextAsShape(g2d, line, startX, startY);
                }
                else {
                    String[] words = line.split(" ");
                    if (words.length <= 1) {
                        drawTextAsShape(g2d, line, startX, startY);
                    }
                    else {
                        int totalWordsWidth = 0;
                        for (String word : words) {
                            totalWordsWidth += metrics.stringWidth(word);
                        }
                        int totalSpaceToFill = maxWidth - totalWordsWidth;
                        float spaceBetweenWords = (float) totalSpaceToFill / (words.length - 1);
                        float justifyX = startX;
                        for (String word : words) {
                            drawTextAsShape(g2d, word, (int) justifyX, startY);
                            justifyX += metrics.stringWidth(word) + spaceBetweenWords;
                        }
                    }
                }
            }
            else {
                drawTextAsShape(g2d, line, currentX, startY);
            }
            startY += lineHeight;
        }
    }
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if(pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D)graphics;
        g2d.setColor(Color.BLACK);
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        int printWidth = (int)pageFormat.getImageableWidth();

        Font orgTitleFont = new Font("Times New Roman", Font.BOLD, 8);
        Font reqNumFont = new Font("Times New Roman", Font.BOLD, 36);
        Font ticketInfFont = new Font("Times New Roman", Font.BOLD, 10);
        Font citizenInfFont = new Font("Times New Roman", Font.ITALIC, 10);

        g2d.setFont(orgTitleFont);
        String title = "TRUNG TÂM PHỤC VỤ HÀNH CHÍNH CÔNG\nXÃ ..........";
        drawMultiLineString(g2d, title, 0, 10, printWidth, TextAlignment.CENTER, 1);

        g2d.setFont(reqNumFont);
        drawMultiLineString(g2d, requestNumber, 0, 80, printWidth, TextAlignment.CENTER, 1);

        g2d.setFont(ticketInfFont);
        String ticketInf = "Đơn vị/Lĩnh vực: " + departmentName + "\n" +
                "Thời gian cấp phiếu: " + requestDate.format(formatter);
        drawMultiLineString(g2d, ticketInf, 0, 120, printWidth, TextAlignment.CENTER, 1);

        g2d.setFont(citizenInfFont);
        String notice = "Phiếu này chỉ cấp một lần và có giá trị cho một lượt làm việc với cơ quan ghi trên phiếu của " +
                "công dân có thông tin sau đây khi được thông báo:";
        String citizenInf = "Họ và tên: " + fullName + "\n" +
                "Số ĐDCN: " + nationalId;
        drawMultiLineString(g2d, notice + "\n" + citizenInf, 0, 200, printWidth, TextAlignment.JUSTIFY, 1);

        return PAGE_EXISTS;
    }

    public static void executePrint(String requestNumber, String departmentName, LocalDateTime requestDate, String fullName, String nationalId) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = new Paper();
        double paperwidth = 74/25.4*72;
        double paperheight = 105/25.4*72;
        paper.setSize(paperwidth, paperheight);
        double margin = 5/25.4*72;
        paper.setImageableArea(margin, margin, paperwidth - 2*margin, paperheight - 2*margin);
        pageFormat.setPaper(paper);
        printerJob.setPrintable(new TicketPrintHelper(requestNumber, departmentName, requestDate, fullName, nationalId), pageFormat);
        try {
            printerJob.print();
        }
        catch(PrinterException e) {
            System.err.println("Lỗi máy in: " + e.getMessage());
        }
    }
}
