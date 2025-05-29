package com.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CashTransfer {
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

    private static final Logger logger = Logger.getLogger(CashTransfer.class.getName());

    /**
     * Transfers the specified amount from fromUserId to toUserId.
     * Returns true if successful, false otherwise.
     */
    public boolean cashTransfer(int fromUserId, int toUserId, double amount) {
        // Validate inputs
        if (fromUserId == toUserId) {
            System.out.println("Cannot transfer to the same account.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("Transfer amount must be greater than 0.");
            return false;
        }

        // SQL statements
        String selectBalanceSql = "SELECT amount FROM balance WHERE user_ID = ?";
        String deductSql = "UPDATE balance SET amount = amount - ? WHERE user_ID = ?";
        String addSql = "UPDATE balance SET amount = amount + ? WHERE user_ID = ?";
        String insertBalanceForReceiverSql = "INSERT INTO balance (amount, user_ID) VALUES (?, ?)";
        String insertTxnSql = "INSERT INTO transaction (amount, name, account_ID, date, transferFromID, transferToID) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // 1) Check sender’s balance
            double senderBalance;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectBalanceSql)) {
                selectStmt.setInt(1, fromUserId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        senderBalance = rs.getDouble("amount");
                    } else {
                        System.out.println("Sender has no balance record.");
                        return false;
                    }
                }
            }
            if (senderBalance < amount) {
                System.out.println("Insufficient balance to transfer.");
                return false;
            }

            // 2) Deduct from sender
            try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                deductStmt.setDouble(1, amount);
                deductStmt.setInt(2, fromUserId);
                deductStmt.executeUpdate();
            }

            // 3) Add to receiver (or insert a new balance record if none exists)
            int rowsAdded;
            try (PreparedStatement addStmt = conn.prepareStatement(addSql)) {
                addStmt.setDouble(1, amount);
                addStmt.setInt(2, toUserId);
                rowsAdded = addStmt.executeUpdate();
            }
            if (rowsAdded == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertBalanceForReceiverSql)) {
                    insertStmt.setDouble(1, amount);
                    insertStmt.setInt(2, toUserId);
                    insertStmt.executeUpdate();
                }
            }

            // 4) Record transaction
            try (PreparedStatement txnStmt = conn.prepareStatement(insertTxnSql)) {
                txnStmt.setDouble(1, amount);
                txnStmt.setString(2, "Transfer");
                txnStmt.setInt(3, fromUserId);
                txnStmt.setInt(4, fromUserId);
                txnStmt.setInt(5, toUserId);
                txnStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Transfer successful: ₱" + String.format("%.2f", amount)
                    + " sent to user ID " + toUserId);
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during transfer from " + fromUserId + " to " + toUserId, e);
            return false;
        }
    }
}