package ui;

import javax.swing.*;
import java.awt.*;

public class ReceiptPreviewPanel extends JPanel {
    private JEditorPane previewPane;

    public ReceiptPreviewPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Receipt Preview"));

        previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        previewPane.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(previewPane);
        add(scrollPane, BorderLayout.CENTER);

        // Initial content
        updatePreview("<html><body><h2 style='text-align:center'>Receipt Preview</h2>"
                + "<p style='text-align:center'>Enter receipt details to see preview</p></body></html>");
    }

    public void updatePreview(String htmlContent) {
        previewPane.setText(htmlContent);
        // Scroll to top
        previewPane.setCaretPosition(0);
    }
}