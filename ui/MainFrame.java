package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ReceiptEntryPanel receiptEntryPanel;
    private ReceiptPreviewPanel receiptPreviewPanel;

    public MainFrame() {
        setTitle("Receipt Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        receiptPreviewPanel = new ReceiptPreviewPanel();

        initComponents();
        layoutComponents();
        setupMenu();
    }

    private void initComponents() {
        receiptEntryPanel = new ReceiptEntryPanel(this);
        receiptPreviewPanel = new ReceiptPreviewPanel();
    }

    private void layoutComponents() {
        // Create a split pane with entry on left and preview on right
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(receiptEntryPanel),
                new JScrollPane(receiptPreviewPanel));

        splitPane.setDividerLocation(500);
        splitPane.setOneTouchExpandable(true);

        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem newReceiptItem = new JMenuItem("New Receipt");
        newReceiptItem.addActionListener(e -> receiptEntryPanel.newReceipt());

        JMenuItem saveReceiptItem = new JMenuItem("Save Receipt");
        saveReceiptItem.addActionListener(e -> receiptEntryPanel.saveReceipt());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newReceiptItem);
        fileMenu.add(saveReceiptItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Receipt menu
        JMenu receiptMenu = new JMenu("Receipts");

        JMenuItem viewAllItem = new JMenuItem("View All Receipts");
        viewAllItem.addActionListener(e -> {
            ReceiptListDialog dialog = new ReceiptListDialog(this);
            dialog.setVisible(true);
        });

        receiptMenu.add(viewAllItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Receipt Generator v1.0\n" +
                            "A simple application for generating receipts",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(receiptMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    public void updatePreview(String receiptContent) {
        if (receiptPreviewPanel != null) {
            receiptPreviewPanel.updatePreview(receiptContent);
        } else {
            System.err.println("Error: receiptPreviewPanel is not initialized!");
        }
    }
}