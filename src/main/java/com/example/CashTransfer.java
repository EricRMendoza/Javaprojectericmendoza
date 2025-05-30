package com.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CashTransfer {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: SQLite JDBC driver not found. Ensure sqlite-jdbc-<version>.jar is on your classpath.");
            e.printStackTrace();
        }
    }

    private static final String DB_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:sqlite:gcash.db";

    private static final Logger logger = Logger.getLogger(CashTransfer.class.getName());

    /**
     * Transfers amount from userId to toUserId.
     * Returns true if successful, false otherwise.
     */
    public boolean cashTransfer(int userId, int toUserId, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount. Transfer amount must be greater than zero.");
            return false;
        }
        if (userId == toUserId) {
            System.out.println("Cannot transfer to the same account.");
            return false;
        }

        String selectBalanceSql = "SELECT amount FROM balance WHERE user_ID = ?";
        String updateBalanceSql = "UPDATE balance SET amount = ? WHERE user_ID = ?";
        String insertBalanceSql = "INSERT INTO balance (amount, user_ID) VALUES (?, ?)";
        String insertTxnSql = "INSERT INTO \"transaction\" (amount, name, account_ID, transferFromID, transferToID, date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            double fromBalance;

            // Check sender's balance
            try (PreparedStatement pstmt = conn.prepareStatement(selectBalanceSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        fromBalance = rs.getDouble("amount");
                    } else {
                        System.out.println("Sender has no balance record.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            if (fromBalance < amount) {
                System.out.println("Insufficient balance.");
                conn.rollback();
                return false;
            }

            // Deduct from sender
            double newFromBalance = fromBalance - amount;
            try (PreparedStatement pstmt = conn.prepareStatement(updateBalanceSql)) {
                pstmt.setDouble(1, newFromBalance);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }

            // Add to receiver balance (insert if missing)
            double toBalance = 0;
            boolean toUserHasBalance = false;
            try (PreparedStatement pstmt = conn.prepareStatement(selectBalanceSql)) {
                pstmt.setInt(1, toUserId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        toBalance = rs.getDouble("amount");
                        toUserHasBalance = true;
                    }
                }
            }

            if (toUserHasBalance) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateBalanceSql)) {
                    pstmt.setDouble(1, toBalance + amount);
                    pstmt.setInt(2, toUserId);
                    pstmt.executeUpdate();
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(insertBalanceSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, toUserId);
                    pstmt.executeUpdate();
                }
            }

            // Record the transaction
            try (PreparedStatement pstmt = conn.prepareStatement(insertTxnSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, "Transfer");
                pstmt.setInt(3, userId);
                pstmt.setInt(4, userId);
                pstmt.setInt(5, toUserId);
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.printf("Transfer successful: ₱%.2f transferred from User %d to User %d%n", amount, userId, toUserId);
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during transfer from " + userId + " to " + toUserId, e);
            return false;
        }
    }
}