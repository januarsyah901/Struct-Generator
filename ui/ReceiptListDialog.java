package ui;

import db.ReceiptDAO;
import model.Receipt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReceiptListDialog extends JDialog {
    private ReceiptDAO receiptDAO;
    private JTable receiptsTable;
    private DefaultTableModel tableModel;
    private List<Receipt> receipts;
    private MainFrame parentFrame;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ReceiptListDialog(MainFrame parent) {
        super(parent, "Receipt History", true);
        this.parentFrame = parent;
        this.receiptDAO = new ReceiptDAO();

        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        loadReceipts();
    }

    private void initComponents() {
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Receipt #", "Customer", "Date", "Total Amount", "Payment Method"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        receiptsTable = new JTable(tableModel);
        receiptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add double-click listener to view receipt details
        receiptsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = receiptsTable.getSelectedRow();
                    if (row >= 0 && row < receipts.size()) {
                        viewReceiptDetails(receipts.get(row));
                    }
                }
            }
        });

        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(receiptsTable);

        // Create button panel
        JPanel buttonPanel = new JPanel();

        JButton viewButton = new JButton("View Details");
        viewButton.addActionListener(e -> {
            int row = receiptsTable.getSelectedRow();
            if (row >= 0 && row < receipts.size()) {
                viewReceiptDetails(receipts.get(row));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a receipt to view",
                        "Selection Required",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton printButton = new JButton("Print Receipt");
        printButton.addActionListener(e -> {
            int row = receiptsTable.getSelectedRow();
            if (row >= 0 && row < receipts.size()) {
                ReceiptPrintUtility.printReceipt(receipts.get(row));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a receipt to print",
                        "Selection Required",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadReceipts());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(viewButton);
        buttonPanel.add(printButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        // Info panel at the top
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Double-click on a receipt to view details"));

        // Add components to dialog
        add(infoPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadReceipts() {
        // Clear table
        tableModel.setRowCount(0);

        // Get receipts from database
        receipts = receiptDAO.getAllReceipts();

        // Add receipts to table
        for (Receipt receipt : receipts) {
            tableModel.addRow(new Object[] {
                    receipt.getReceiptNumber(),
                    receipt.getCustomerName(),
                    DATE_FORMAT.format(receipt.getCreatedAt()),
                    String.format("$%.2f", receipt.getTotalAmount()),
                    receipt.getPaymentMethod()
            });
        }

        // Show message if no receipts
        if (receipts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No receipts found in the database.",
                    "No Records",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewReceiptDetails(Receipt receipt) {
        ReceiptViewDialog viewDialog = new ReceiptViewDialog(parentFrame, receipt);
        viewDialog.setVisible(true);
    }
}