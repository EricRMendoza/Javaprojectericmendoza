package com.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Transactions {
    // Force the SQLite JDBC driver to load
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: SQLite JDBC driver not found. Ensure sqlite-jdbc-<version>.jar is on your classpath.");
            e.printStackTrace();
        }
    }

    // Use DB_URL from environment, or fallback to "jdbc:sqlite:gcash.db"
    private static final String DB_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:sqlite:gcash.db";

    private static final Logger logger = Logger.getLogger(Transactions.class.getName());

    /**
     * Prints all transactions in the `transaction` table, newest first.
     */
    public void viewAll() {
        String sql = "SELECT * FROM transaction ORDER BY date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("=== All Transactions ===");
            while (rs.next()) {
                displayTransaction(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all transactions", e);
        }
    }

    /**
     * Prints all transactions related to a given userId (as account owner, payer, or receiver), newest first.
     */
    public void viewUserAll(int userId) {
        String sql = "SELECT * FROM transaction " +
                "WHERE account_ID = ? OR transferFromID = ? OR transferToID = ? " +
                "ORDER BY date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("=== Transactions for User ID: " + userId + " ===");
                while (rs.next()) {
                    displayTransaction(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching transactions for userId=" + userId, e);
        }
    }

    /**
     * Prints a single transaction by its transactionId.
     */
    public void viewTransaction(int transactionId) {
        String sql = "SELECT * FROM transaction WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("=== Transaction ID: " + transactionId + " ===");
                    displayTransaction(rs);
                } else {
                    System.out.println("Transaction ID " + transactionId + " not found.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching transaction id=" + transactionId, e);
        }
    }

    /**
     * Helper to format and print a single ResultSet row of `transaction`.
     */
    private void displayTransaction(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double amount = rs.getDouble("amount");
        String date = rs.getString("date");
        int acctId = rs.getInt("account_ID");
        int fromId = rs.getInt("transferFromID");
        int toId = rs.getInt("transferToID");

        System.out.printf("ID:%d | %-10s | ₱%.2f | acct:%d | from:%d | to:%d | %s%n",
                id, name, amount, acctId, fromId, toId, date);
    }
}