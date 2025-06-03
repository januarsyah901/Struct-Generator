package ui;

import model.Receipt;
import model.ReceiptItem;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;

public class ReceiptPrintUtility implements Printable {
    private Receipt receipt;

    public ReceiptPrintUtility(Receipt receipt) {
        this.receipt = receipt;
    }

    public static void printReceipt(Receipt receipt) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ReceiptPrintUtility(receipt));

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(null,
                        "Receipt sent to printer successfully!",
                        "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to print: " + e.getMessage(),
                        "Print Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // Set font
        Font regularFont = new Font("Arial", Font.PLAIN, 10);
        Font boldFont = new Font("Arial", Font.BOLD, 10);
        Font titleFont = new Font("Arial", Font.BOLD, 14);

        g2d.setFont(regularFont);

        float x = 0;
        float y = 0;
        float lineHeight = 15;
        float width = (float) pf.getImageableWidth();

        // Title
        g2d.setFont(titleFont);
        String title = "RECEIPT";
        FontMetrics fmTitle = g2d.getFontMetrics();
        x = (width - fmTitle.stringWidth(title)) / 2;
        g2d.drawString(title, x, y += lineHeight);

        g2d.setFont(regularFont);

        // Date and receipt number
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(receipt.getCreatedAt());
        FontMetrics fm = g2d.getFontMetrics();
        x = (width - fm.stringWidth(date)) / 2;
        g2d.drawString(date, x, y += lineHeight);

        String receiptNum = "Receipt #: " + receipt.getReceiptNumber();
        g2d.setFont(boldFont);
        x = (width - fm.stringWidth(receiptNum)) / 2;
        g2d.drawString(receiptNum, x, y += lineHeight);

        g2d.setFont(regularFont);
        y += lineHeight;

        // Customer info
        g2d.drawString("Customer: " + receipt.getCustomerName(), 0, y += lineHeight);
        g2d.drawString("Payment Method: " + receipt.getPaymentMethod(), 0, y += lineHeight);
        y += lineHeight/2;

        // Divider
        g2d.drawLine(0, (int)y, (int)width, (int)y);
        y += lineHeight;

        // Column headers
        g2d.setFont(boldFont);
        g2d.drawString("Item", 0, y);
        g2d.drawString("Qty", width/2, y);
        g2d.drawString("Price", width*3/4, y);
        g2d.drawString("Total", width-40, y);
        y += lineHeight;
        g2d.setFont(regularFont);

        // Items
        for (ReceiptItem item : receipt.getItems()) {
            g2d.drawString(item.getItemName(), 0, y);
            g2d.drawString(String.valueOf(item.getQuantity()), width/2, y);
            g2d.drawString(String.format("$%.2f", item.getUnitPrice()), width*3/4, y);
            g2d.drawString(String.format("$%.2f", item.getTotalPrice()), width-40, y);
            y += lineHeight;
        }

        // Divider
        g2d.drawLine(0, (int)y, (int)width, (int)y);
        y += lineHeight;

        // Total
        g2d.setFont(boldFont);
        String total = String.format("Total: $%.2f", receipt.getTotalAmount());
        g2d.drawString(total, width-fm.stringWidth(total)-10, y+=lineHeight);
        g2d.setFont(regularFont);

        // Notes
        if (receipt.getNotes() != null && !receipt.getNotes().isEmpty()) {
            y += lineHeight;
            g2d.setFont(boldFont);
            g2d.drawString("Notes:", 0, y+=lineHeight);
            g2d.setFont(regularFont);

            for (String line : receipt.getNotes().split("\n")) {
                g2d.drawString(line, 5, y+=lineHeight);
            }
        }

        // Footer
        y += lineHeight * 2;
        String footer = "Thank you for your business!";
        x = (width - fm.stringWidth(footer)) / 2;
        g2d.drawString(footer, x, y);

        return PAGE_EXISTS;
    }
}