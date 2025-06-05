package ui;

import model.Receipt;
import model.ReceiptItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;

public class ReceiptViewDialog extends JDialog {
    private Receipt receipt;
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ReceiptViewDialog(Frame parent, Receipt receipt) {
        super(parent, "Receipt Details", true);
        this.receipt = receipt;

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // Receipt info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Receipt Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Receipt number
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Receipt Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        infoPanel.add(new JLabel(receipt.getReceiptNumber()), gbc);

        // Date
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        infoPanel.add(new JLabel(DATE_FORMAT.format(receipt.getCreatedAt())), gbc);

        // Customer
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Customer:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        infoPanel.add(new JLabel(receipt.getCustomerName()), gbc);

        // Payment method
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        infoPanel.add(new JLabel("Payment Method:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        infoPanel.add(new JLabel(receipt.getPaymentMethod()), gbc);

        // Notes
        if (receipt.getNotes() != null && !receipt.getNotes().isEmpty()) {
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0.0;
            infoPanel.add(new JLabel("Notes:"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;

            JTextArea notesArea = new JTextArea(receipt.getNotes());
            notesArea.setEditable(false);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            JScrollPane notesScrollPane = new JScrollPane(notesArea);
            notesScrollPane.setPreferredSize(new Dimension(200, 60));
            infoPanel.add(notesScrollPane, gbc);
        }

        // Items table
        tableModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Item Name", "Quantity", "Unit Price", "Total Price"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        itemsTable = new JTable(tableModel);

        // Add items to table
        for (ReceiptItem item : receipt.getItems()) {
            tableModel.addRow(new Object[] {
                    item.getItemName(),
                    item.getQuantity(),
                    String.format("$%.2f", item.getUnitPrice()),
                    String.format("$%.2f", item.getTotalPrice())
            });
        }

        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("Receipt Items"));

        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel("Total: " + String.format("$%.2f", receipt.getTotalAmount()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalPanel.add(totalLabel);

        // Button panel
        JPanel buttonPanel = new JPanel();

        JButton printButton = new JButton("Print Receipt");
        printButton.addActionListener(e -> ReceiptPrintUtility.printReceipt(receipt));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);

        // Add components to dialog
        add(infoPanel, BorderLayout.NORTH);
        add(itemsScrollPane, BorderLayout.CENTER);
        add(totalPanel, BorderLayout.AFTER_LINE_ENDS);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}