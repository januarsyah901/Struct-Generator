package ui;

import db.ReceiptDAO;
import model.Receipt;
import model.ReceiptItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class ReceiptEntryPanel extends JPanel {
    private MainFrame parentFrame;
    private Receipt currentReceipt;
    private ReceiptDAO receiptDAO;

    // Form components
    private JTextField customerNameField;
    private JTextField receiptNumberField;
    private JComboBox<String> paymentMethodCombo;
    private JTextArea notesArea;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    public ReceiptEntryPanel(MainFrame parent) {
        this.parentFrame = parent;
        this.receiptDAO = new ReceiptDAO();
        this.currentReceipt = new Receipt();
        this.currentReceipt.generateReceiptNumber();

        setLayout(new BorderLayout());

        initComponents();
        layoutComponents();
        setupListeners();
        updatePreview();
    }

    private void initComponents() {
        // Initialize receipt header components
        customerNameField = new JTextField(20);
        receiptNumberField = new JTextField(currentReceipt.getReceiptNumber());
        receiptNumberField.setEditable(false);

        paymentMethodCombo = new JComboBox<>(new String[] {
                "Cash", "Credit Card", "Bank Transfer", "Digital Wallet"
        });

        notesArea = new JTextArea(3, 20);

        // Initialize items table
        tableModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Item Name", "Quantity", "Unit Price", "Total Price", "Action"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 3; // Make total price column non-editable
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        itemsTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        // Initialize total label
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
    }

    private void layoutComponents() {
        // Receipt header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Receipt Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Receipt number
        gbc.gridx = 0;
        gbc.gridy = 0;
        headerPanel.add(new JLabel("Receipt Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        headerPanel.add(receiptNumberField, gbc);

        // Date
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        headerPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        JTextField dateField = new JTextField(sdf.format(new Date()));
        dateField.setEditable(false);
        headerPanel.add(dateField, gbc);

        // Customer name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        headerPanel.add(new JLabel("Customer Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        headerPanel.add(customerNameField, gbc);

        // Payment method
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        headerPanel.add(new JLabel("Payment Method:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        headerPanel.add(paymentMethodCombo, gbc);

        // Notes
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        headerPanel.add(new JLabel("Notes:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        headerPanel.add(new JScrollPane(notesArea), gbc);

        // Items panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items"));

        // Add button
        JButton addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(e -> addNewItem());

        itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        itemsPanel.add(addItemButton, BorderLayout.SOUTH);

        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(totalLabel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Save Receipt");
        saveButton.addActionListener(e -> saveReceipt());

        JButton printButton = new JButton("Print Receipt");
        printButton.addActionListener(e -> printReceipt());

        JButton clearButton = new JButton("New Receipt");
        clearButton.addActionListener(e -> newReceipt());

        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(printButton);

        // Main panel layout
        add(headerPanel, BorderLayout.NORTH);
        add(itemsPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

private void setupListeners() {
    // Update preview when any field changes
    customerNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            updatePreview();
        }
    });

    notesArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            updatePreview();
        }
    });

    paymentMethodCombo.addActionListener(e -> updatePreview());
}

    public void addNewItem() {
        ItemDialog dialog = new ItemDialog(SwingUtilities.getWindowAncestor(this));
        if (dialog.showDialog()) {
            ReceiptItem item = dialog.getItem();
            currentReceipt.addItem(item);
            addItemToTable(item);
            updateTotal();
            updatePreview();
        }
    }

    public void removeItem(int row) {
        if (row >= 0 && row < currentReceipt.getItems().size()) {
            currentReceipt.getItems().remove(row);
            tableModel.removeRow(row);
            currentReceipt.calculateTotal();
            updateTotal();
            updatePreview();
        }
    }

    private void addItemToTable(ReceiptItem item) {
        Vector<Object> row = new Vector<>();
        row.add(item.getItemName());
        row.add(item.getQuantity());
        row.add(item.getUnitPrice());
        row.add(item.getTotalPrice());
        row.add("Remove");

        tableModel.addRow(row);
    }

    private void updateTotal() {
        totalLabel.setText(String.format("Total: $%.2f", currentReceipt.getTotalAmount()));
    }

    public void saveReceipt() {
        if (validateForm()) {
            updateReceiptFromForm();

            if (receiptDAO.saveReceipt(currentReceipt)) {
                JOptionPane.showMessageDialog(this,
                        "Receipt saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                newReceipt();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save receipt. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void newReceipt() {
        currentReceipt = new Receipt();
        currentReceipt.generateReceiptNumber();

        customerNameField.setText("");
        receiptNumberField.setText(currentReceipt.getReceiptNumber());
        paymentMethodCombo.setSelectedIndex(0);
        notesArea.setText("");

        tableModel.setRowCount(0);
        updateTotal();
        updatePreview();
    }

    private void printReceipt() {
        if (validateForm()) {
            updateReceiptFromForm();
            ReceiptPrintUtility.printReceipt(currentReceipt);
        }
    }

    private boolean validateForm() {
        if (customerNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a customer name.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (currentReceipt.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one item to the receipt.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void updateReceiptFromForm() {
        currentReceipt.setCustomerName(customerNameField.getText().trim());
        currentReceipt.setPaymentMethod((String) paymentMethodCombo.getSelectedItem());
        currentReceipt.setNotes(notesArea.getText().trim());
    }

    private void updatePreview() {
        updateReceiptFromForm();

        StringBuilder preview = new StringBuilder();
        preview.append("<html><body style='font-family: Arial; width: 300px;'>");

        // Header
        preview.append("<div style='text-align: center;'>");
        preview.append("<h2>RECEIPT</h2>");
        preview.append("<p>").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("</p>");
        preview.append("<p><b>Receipt #: ").append(currentReceipt.getReceiptNumber()).append("</b></p>");
        preview.append("</div>");

        // Customer info
        preview.append("<div>");
        preview.append("<p><b>Customer:</b> ").append(currentReceipt.getCustomerName().isEmpty() ? "[Customer Name]" : currentReceipt.getCustomerName()).append("</p>");
        preview.append("<p><b>Payment Method:</b> ").append(currentReceipt.getPaymentMethod()).append("</p>");
        preview.append("</div>");

        // Divider
        preview.append("<hr>");

        // Items
        preview.append("<table style='width: 100%;'>");
        preview.append("<tr>");
        preview.append("<th style='text-align: left;'>Item</th>");
        preview.append("<th>Qty</th>");
        preview.append("<th>Price</th>");
        preview.append("<th style='text-align: right;'>Total</th>");
        preview.append("</tr>");

        for (ReceiptItem item : currentReceipt.getItems()) {
            preview.append("<tr>");
            preview.append("<td>").append(item.getItemName()).append("</td>");
            preview.append("<td style='text-align: center;'>").append(item.getQuantity()).append("</td>");
            preview.append("<td style='text-align: right;'>").append(String.format("$%.2f", item.getUnitPrice())).append("</td>");
            preview.append("<td style='text-align: right;'>").append(String.format("$%.2f", item.getTotalPrice())).append("</td>");
            preview.append("</tr>");
        }

        preview.append("</table>");

        // Divider
        preview.append("<hr>");

        // Total
        preview.append("<div style='text-align: right;'>");
        preview.append("<p><b>Total: ").append(String.format("$%.2f", currentReceipt.getTotalAmount())).append("</b></p>");
        preview.append("</div>");

        // Notes
        if (!currentReceipt.getNotes().isEmpty()) {
            preview.append("<div>");
            preview.append("<p><b>Notes:</b><br>").append(currentReceipt.getNotes().replace("\n", "<br>")).append("</p>");
            preview.append("</div>");
        }

        // Footer
        preview.append("<div style='text-align: center; margin-top: 20px;'>");
        preview.append("<p>Thank you for your business!</p>");
        preview.append("</div>");

        preview.append("</body></html>");

        parentFrame.updatePreview(preview.toString());
    }

    // Inner class for Item Dialog
    private class ItemDialog extends JDialog {
        private JTextField itemNameField;
        private JSpinner quantitySpinner;
        private JTextField priceField;
        private boolean confirmed = false;
        private ReceiptItem item = new ReceiptItem();

        public ItemDialog(Window parent) {
            super(parent, "Add Item", ModalityType.APPLICATION_MODAL);

            setLayout(new BorderLayout());

            // Create form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Item name
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Item Name:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            itemNameField = new JTextField(20);
            formPanel.add(itemNameField, gbc);

            // Quantity
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            formPanel.add(new JLabel("Quantity:"), gbc);

            gbc.gridx = 1;
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 999, 1);
            quantitySpinner = new JSpinner(spinnerModel);
            formPanel.add(quantitySpinner, gbc);

            // Unit price
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(new JLabel("Unit Price:"), gbc);

            gbc.gridx = 1;
            priceField = new JTextField(10);
            formPanel.add(priceField, gbc);

            // Buttons panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                if (validateItem()) {
                    confirmed = true;
                    dispose();
                }
            });

            buttonPanel.add(cancelButton);
            buttonPanel.add(okButton);

            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
        }

        private boolean validateItem() {
            if (itemNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an item name", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "Price must be greater than zero", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                item.setItemName(itemNameField.getText().trim());
                item.setQuantity((Integer) quantitySpinner.getValue());
                item.setUnitPrice(price);
                item.calculateTotal();

                return true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        public boolean showDialog() {
            setVisible(true);
            return confirmed;
        }

        public ReceiptItem getItem() {
            return item;
        }
    }

    // Inner class for table button renderer
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            return this;
        }
    }

    // Inner class for table button editor
    private static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;
        private ReceiptEntryPanel panel;

        public ButtonEditor(JCheckBox checkBox, ReceiptEntryPanel panel) {
            super(checkBox);
            this.panel = panel;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.label = value.toString();
            this.row = row;
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                panel.removeItem(row);
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    // Simple document listener for easier event handling
    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }
    }
}