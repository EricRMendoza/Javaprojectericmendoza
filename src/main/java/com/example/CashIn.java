package com.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CashIn {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: SQLite JDBC driver not found.");
            e.printStackTrace();
        }
    }

    private static final String DB_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:sqlite:gcash.db";

    private static final Logger logger = Logger.getLogger(CashIn.class.getName());

    public boolean cashIn(int userId, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount. Cash-in must be greater than 0.");
            return false;
        }

        String updateBalanceSql = "UPDATE balance SET amount = amount + ? WHERE user_ID = ?";
        String insertBalanceIfMissingSql = "INSERT INTO balance (amount, user_ID) VALUES (?, ?)";
        String insertTxnSql = "INSERT INTO \"transaction\" (amount, name, account_ID, date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateBalanceSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, userId);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertBalanceIfMissingSql)) {
                        insertStmt.setDouble(1, amount);
                        insertStmt.setInt(2, userId);
                        insertStmt.executeUpdate();
                    }
                }
            }

            try (PreparedStatement txnStmt = conn.prepareStatement(insertTxnSql)) {
                txnStmt.setDouble(1, amount);
                txnStmt.setString(2, "CashIn");
                txnStmt.setInt(3, userId);
                txnStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Cash-in successful: ₱" + String.format("%.2f", amount));
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during cash-in for userId=" + userId, e);
            System.out.println("Cash-in failed.");
            return false;
        }
    }
}