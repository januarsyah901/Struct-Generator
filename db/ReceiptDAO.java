package db;

import model.Receipt;
import model.ReceiptItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDAO {

    // Save a complete receipt with its items
    public boolean saveReceipt(Receipt receipt) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            // Insert customer if new
            int customerId = saveCustomer(connection, receipt.getCustomerName());
            receipt.setCustomerId(customerId);

            // Insert receipt
            String sql = "INSERT INTO receipts (receipt_number, customer_id, total_amount, " +
                    "payment_method, notes, created_at) VALUES (?, ?, ?, ?, ?, ?)";

            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, receipt.getReceiptNumber());
            pstmt.setInt(2, receipt.getCustomerId());
            pstmt.setBigDecimal(3, receipt.getTotalAmount());
            pstmt.setString(4, receipt.getPaymentMethod());
            pstmt.setString(5, receipt.getNotes());
            pstmt.setTimestamp(6, new Timestamp(receipt.getCreatedAt().getTime()));

            int result = pstmt.executeUpdate();

            // Get generated receipt ID
            generatedKeys = pstmt.getGeneratedKeys();
            int receiptId;
            if (generatedKeys.next()) {
                receiptId = generatedKeys.getInt(1);
                receipt.setId(receiptId);

                // Insert receipt items
                for (ReceiptItem item : receipt.getItems()) {
                    saveReceiptItem(connection, receiptId, item);
                }

                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Save or get customer ID
    private int saveCustomer(Connection connection, String customerName) throws SQLException {
        // First try to find if customer exists
        String findSql = "SELECT id FROM customers WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(findSql)) {
            pstmt.setString(1, customerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // If not found, insert new customer
        String insertSql = "INSERT INTO customers (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customerName);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating customer failed, no ID obtained.");
            }
        }
    }

    // Save a receipt item
    private void saveReceiptItem(Connection connection, int receiptId, ReceiptItem item) throws SQLException {
        String sql = "INSERT INTO receipt_items (receipt_id, item_name, quantity, unit_price, total_price) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, receiptId);
            pstmt.setString(2, item.getItemName());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getUnitPrice());
            pstmt.setBigDecimal(5, item.getTotalPrice());

            pstmt.executeUpdate();
        }
    }

    // Get all receipts
    public List<Receipt> getAllReceipts() {
        List<Receipt> receipts = new ArrayList<>();
        String sql = "SELECT r.*, c.name as customer_name FROM receipts r " +
                "LEFT JOIN customers c ON r.customer_id = c.id " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Receipt receipt = new Receipt();
                receipt.setId(rs.getInt("id"));
                receipt.setReceiptNumber(rs.getString("receipt_number"));
                receipt.setCustomerId(rs.getInt("customer_id"));
                receipt.setCustomerName(rs.getString("customer_name"));
                receipt.setTotalAmount(rs.getBigDecimal("total_amount"));
                receipt.setPaymentMethod(rs.getString("payment_method"));
                receipt.setNotes(rs.getString("notes"));
                receipt.setCreatedAt(rs.getTimestamp("created_at"));

                // Get receipt items
                receipt.setItems(getReceiptItems(receipt.getId()));

                receipts.add(receipt);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return receipts;
    }

    // Get items for a receipt
    public List<ReceiptItem> getReceiptItems(int receiptId) {
        List<ReceiptItem> items = new ArrayList<>();
        String sql = "SELECT * FROM receipt_items WHERE receipt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, receiptId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ReceiptItem item = new ReceiptItem();
                item.setId(rs.getInt("id"));
                item.setReceiptId(rs.getInt("receipt_id"));
                item.setItemName(rs.getString("item_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));

                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    // Get a single receipt by ID
    public Receipt getReceiptById(int id) {
        Receipt receipt = null;
        String sql = "SELECT r.*, c.name as customer_name FROM receipts r " +
                "LEFT JOIN customers c ON r.customer_id = c.id " +
                "WHERE r.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                receipt = new Receipt();
                receipt.setId(rs.getInt("id"));
                receipt.setReceiptNumber(rs.getString("receipt_number"));
                receipt.setCustomerId(rs.getInt("customer_id"));
                receipt.setCustomerName(rs.getString("customer_name"));
                receipt.setTotalAmount(rs.getBigDecimal("total_amount"));
                receipt.setPaymentMethod(rs.getString("payment_method"));
                receipt.setNotes(rs.getString("notes"));
                receipt.setCreatedAt(rs.getTimestamp("created_at"));

                // Get receipt items
                receipt.setItems(getReceiptItems(receipt.getId()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return receipt;
    }
}