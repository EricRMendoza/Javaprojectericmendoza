package com.example;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 1) Initialize the database and tables (users, balance, transaction).
        UserAuthentication.initializeDB();

        // 2) Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // 3) Instantiate service classes
        UserAuthentication auth = new UserAuthentication();
        CheckBalance checkBalance = new CheckBalance();
        CashIn cashIn = new CashIn();
        CashTransfer cashTransfer = new CashTransfer();
        Transactions transactions = new Transactions();

        int userId = -1; // Will hold the logged-in user ID

        // 4) Outer loop: Register / Login / Exit
        outerLoop: while (true) {
            System.out.println("\n=== Welcome to GCash App ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    // --- Register ---
                    System.out.print("Enter Name: ");
                    String name = scanner.nextLine();

                    System.out.print("Enter Email: ");
                    String email = scanner.nextLine();

                    System.out.print("Enter Contact Number (11 digits): ");
                    String number = scanner.nextLine();

                    System.out.print("Enter 4-digit PIN: ");
                    String pin = scanner.nextLine();

                    boolean registered = auth.register(name, email, number, pin);
                    if (registered) {
                        System.out.println("→ Registration succeeded! You can now login.");
                    } else {
                        System.out.println("→ Registration failed. Try again.");
                    }
                    break;

                case 2:
                    // --- Login ---
                    System.out.print("Enter Email or Number: ");
                    String loginKey = scanner.nextLine();

                    System.out.print("Enter PIN: ");
                    String loginPin = scanner.nextLine();

                    boolean loggedIn = auth.login(loginKey, loginPin);
                    if (loggedIn) {
                        userId = UserAuthentication.getCurrentUserId();
                        System.out.println("→ Login successful. Your User ID is " + userId);

                        // 5) Inner loop: banking menu
                        boolean sessionActive = true;
                        while (sessionActive) {
                            System.out.println("\n--- Main Menu ---");
                            System.out.println("1. Check Balance");
                            System.out.println("2. Cash In");
                            System.out.println("3. Cash Transfer");
                            System.out.println("4. View My Transactions");
                            System.out.println("5. Change PIN");
                            System.out.println("6. Logout");
                            System.out.print("Enter your choice: ");
                            int action = scanner.nextInt();
                            scanner.nextLine(); // consume newline

                            switch (action) {
                                case 1:
                                    double balance = CheckBalance.checkBalance(userId);
                                    System.out.printf("Current balance: ₱%.2f%n", balance);
                                    break;

                                case 2:
                                    System.out.print("Enter amount to cash in (PHP): ");
                                    double amountIn = scanner.nextDouble();
                                    scanner.nextLine();
                                    cashIn.cashIn(userId, amountIn);
                                    break;

                                case 3:
                                    System.out.print("Enter recipient User ID: ");
                                    int toUserId = scanner.nextInt();
                                    System.out.print("Enter amount to transfer (PHP): ");
                                    double amountTransfer = scanner.nextDouble();
                                    scanner.nextLine();
                                    cashTransfer.cashTransfer(userId, toUserId, amountTransfer);
                                    break;

                                case 4:
                                    transactions.viewUserAll(userId);
                                    break;

                                case 5:
                                    System.out.print("Enter current PIN: ");
                                    String oldPin = scanner.nextLine();
                                    System.out.print("Enter new 4-digit PIN: ");
                                    String newPin = scanner.nextLine();
                                    auth.changePin(oldPin, newPin);
                                    break;

                                case 6:
                                    auth.logout();
                                    sessionActive = false;
                                    System.out.println("→ You have been logged out.");
                                    break;

                                default:
                                    System.out.println("Invalid choice. Try again.");
                            }

                            // Ask if the user wants another transaction
                            if (sessionActive) {
                                System.out.print("\nDo you want to perform another transaction? (y/n): ");
                                String cont = scanner.nextLine().trim().toLowerCase();
                                if (!cont.equals("y")) {
                                    auth.logout();
                                    sessionActive = false;
                                    System.out.println("→ You have been logged out.");
                                }
                            }
                        }
                    } else {
                        System.out.println("→ Login failed. Check credentials and try again.");
                    }
                    break;

                case 3:
                    // --- Exit ---
                    System.out.println("Goodbye!");
                    break outerLoop;

                default:
                    System.out.println("Invalid input. Try again.");
            }
        }

        scanner.close();
    }
}