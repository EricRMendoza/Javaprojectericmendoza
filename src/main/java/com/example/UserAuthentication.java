package com.example;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAuthentication {
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

    private static int currentUserId = -1;
    private static final Logger logger = Logger.getLogger(UserAuthentication.class.getName());

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void initializeDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String usersSql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "number TEXT UNIQUE NOT NULL," +
                    "pin TEXT NOT NULL" +
                    ")";
            stmt.execute(usersSql);

            String balanceSql = "CREATE TABLE IF NOT EXISTS balance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL DEFAULT 0.0," +
                    "user_ID INTEGER NOT NULL," +
                    "FOREIGN KEY(user_ID) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(balanceSql);

            String txnSql = "CREATE TABLE IF NOT EXISTS transaction (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL," +
                    "name TEXT NOT NULL," +
                    "account_ID INTEGER NOT NULL," +
                    "date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "transferFromID INTEGER," +
                    "transferToID INTEGER," +
                    "FOREIGN KEY(account_ID) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(transferFromID) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(transferToID) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute("CREATE TABLE IF NOT EXISTS \"transaction\" (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL," +
                    "name TEXT NOT NULL," +
                    "account_ID INTEGER NOT NULL," +
                    "date TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "transferFromID INTEGER," +
                    "transferToID INTEGER," +
                    "FOREIGN KEY(account_ID) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(transferFromID) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(transferToID) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database initialization error", e);
        }
    }

    public static boolean register(String name, String email, String number, String pin) {
        if (name.isEmpty()
                || !email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")
                || number.length() != 11
                || !pin.matches("\\d{4}")) {
            System.out.println("Invalid input. Check field formats:");
            System.out.println(" • Name cannot be empty");
            System.out.println(" • Email must be valid");
            System.out.println(" • Number must be exactly 11 digits");
            System.out.println(" • PIN must be exactly 4 digits");
            return false;
        }

        String insertSql = "INSERT INTO users (name, email, number, pin) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, number);
            pstmt.setString(4, pin);
            pstmt.executeUpdate();
            System.out.println("Registration successful.");
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Registration failed. Email or number may already exist.", e);
            return false;
        }
    }

    public static boolean login(String emailOrNumber, String pin) {
        String selectSql = "SELECT id FROM users WHERE (email=? OR number=?) AND pin=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {

            pstmt.setString(1, emailOrNumber);
            pstmt.setString(2, emailOrNumber);
            pstmt.setString(3, pin);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentUserId = rs.getInt("id");
                    System.out.println("Login successful. User ID: " + currentUserId);
                    return true;
                } else {
                    System.out.println("Invalid login. Check your credentials.");
                    return false;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Login error", e);
            return false;
        }
    }

    public static boolean changePin() {
        return changePin(null, null);
    }

    public static boolean changePin(String oldPin, String newPin) {
        if (currentUserId == -1) {
            System.out.println("User not logged in.");
            return false;
        }
        if (!newPin.matches("\\d{4}")) {
            System.out.println("New PIN must be exactly 4 digits.");
            return false;
        }

        String updateSql = "UPDATE users SET pin=? WHERE id=? AND pin=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setString(1, newPin);
            pstmt.setInt(2, currentUserId);
            pstmt.setString(3, oldPin);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("PIN changed successfully.");
                return true;
            } else {
                System.out.println("Old PIN is incorrect.");
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error changing PIN", e);
            return false;
        }
    }

    public static void logout() {
        currentUserId = -1;
        System.out.println("User logged out.");
    }
}