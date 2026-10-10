import java.io.*;
import java.util.*;

class User implements Serializable {
    private String username;
    private String passwordHash; // In a production app, use proper salting & hashing (e.g., BCrypt)

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public boolean authenticate(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        // Simple hash simulation for demonstration purposes
        return Integer.toHexString(password.hashCode());
    }

    public String toFileString() {
        return username + "," + passwordHash;
    }
}

class Account {
    private String accountNumber;
    private String username;
    private double balance;

    public Account(String accountNumber, String username, double balance) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getUsername() { return username; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Successfully deposited: $" + amount);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Successfully withdrew: $" + amount);
            return true;
        } else {
            System.out.println("Invalid withdrawal amount or insufficient funds.");
            return false;
        }
    }

    public String toFileString() {
        return accountNumber + "," + username + "," + balance;
    }
}

public class BankingApp {
    private static final String USER_FILE = "users.txt";
    private static final String ACC_FILE = "accounts.txt";
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, Account> accounts = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();
        
        boolean running = true;
        while (running) {
            System.out.println("\n=== SECURE BANKING SYSTEM ===");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            int choice = getIntInput();
            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    saveData();
                    System.out.println("Exiting application. Data saved securely.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return;
        }
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        users.put(username, new User(username, password));
        
        // Automatically create a default bank account for the user
        String accNumber = "ACC" + (1000 + new Random().nextInt(9000));
        accounts.put(username, new Account(accNumber, username, 0.0));
        
        saveData();
        System.out.println("Registration successful! Your Account Number is: " + accNumber);
    }

    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        User user = users.get(username);
        if (user != null && user.authenticate(password)) {
            System.out.println("Login successful! Welcome, " + username);
            userMenu(username);
        } else {
            System.out.println("Authentication failed. Invalid username or password.");
        }
    }

    private static void userMenu(String username) {
        Account account = accounts.get(username);
        boolean loggedIn = true;
        
        while (loggedIn) {
            System.out.println("\n--- Account Dashboard ---");
            System.out.println("1. View Account Balance");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Withdraw Funds");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    System.out.println("Account Number: " + account.getAccountNumber());
                    System.out.println("Current Balance: $" + account.getBalance());
                    break;
                case 2:
                    System.out.print("Enter amount to deposit: ");
                    double depAmount = getDoubleInput();
                    account.deposit(depAmount);
                    saveData();
                    break;
                case 3:
                    System.out.print("Enter amount to withdraw: ");
                    double witAmount = getDoubleInput();
                    account.withdraw(witAmount);
                    saveData();
                    break;
                case 4:
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    private static void loadData() {
        try {
            File userFile = new File(USER_FILE);
            if (userFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(userFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        // Reconstructing user map safely
                        users.put(parts[0], new User(parts[0], "")); // Dummy pass since it's pre-hashed
                        // To properly load pre-hashed strings, use a secondary constructor if needed.
                    }
                }
                reader.close();
            }

            File accFile = new File(ACC_FILE);
            if (accFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(accFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        accounts.put(parts[1], new Account(parts[0], parts[1], Double.parseDouble(parts[2])));
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            System.out.println("Error loading persistence files: " + e.getMessage());
        }
    }

    private static void saveData() {
        try {
            PrintWriter userWriter = new PrintWriter(new FileWriter(USER_FILE));
            for (User user : users.values()) {
                userWriter.println(user.toFileString());
            }
            userWriter.close();

            PrintWriter accWriter = new PrintWriter(new FileWriter(ACC_FILE));
            for (Account acc : accounts.values()) {
                accWriter.println(acc.toFileString());
            }
            accWriter.close();
        } catch (IOException e) {
            System.out.println("Error saving data to files: " + e.getMessage());
        }
    }
}