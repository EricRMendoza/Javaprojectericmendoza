package com.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckBalance {
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

    private static final Logger logger = Logger.getLogger(CheckBalance.class.getName());

    /**
     * Returns the current balance for the specified user ID.
     * If no record exists in the 'balance' table, returns 0.0.
     */
    public static double checkBalance(int userId) {
        double balance = 0.0;
        String sql = "SELECT amount FROM balance WHERE user_ID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    balance = rs.getDouble("amount");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking balance for userId=" + userId, e);
        }
        return balance;
    }
}