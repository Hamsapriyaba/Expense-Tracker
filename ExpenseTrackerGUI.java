import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

public class ExpenseTrackerGUI extends JFrame {

    private static final String EXPENSES_FILE = "expenses.txt";
    private static final String USERS_FILE = "users.txt";

    private JTextField dateField;
    private JTextField categoryField;
    private JTextField amountField;
    private JTextArea expenseTextArea;
    private JLabel totalExpenseLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private List<Expense> expenses;
    private Map<String, Double> categoryWiseExpenses;
    private String currentUser;

    public ExpenseTrackerGUI() {
        setTitle("Expense Tracker");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        expenses = new ArrayList<>();
        categoryWiseExpenses = new HashMap<>();

        showLoginScreen();
    }

    private void showLoginScreen() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });
        loginPanel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        loginPanel.add(registerButton);

        getContentPane().add(loginPanel);
    }

    private void showExpenseTrackerScreen() {
        getContentPane().removeAll();
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 2, 5, 5));

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        dateField = new JTextField();
        panel.add(dateLabel);
        panel.add(dateField);

        JLabel categoryLabel = new JLabel("Category:");
        categoryField = new JTextField();
        panel.add(categoryLabel);
        panel.add(categoryField);

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();
        panel.add(amountLabel);
        panel.add(amountField);

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });
        panel.add(addButton);

        JButton viewCategoryWiseButton = new JButton("View Category-wise");
        viewCategoryWiseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewCategoryWiseExpenses();
            }
        });
        panel.add(viewCategoryWiseButton);

        JButton saveButton = new JButton("Save Expenses");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExpensesToFile();
            }
        });
        panel.add(saveButton);

        totalExpenseLabel = new JLabel("Total Expenses: $0.00");
        panel.add(totalExpenseLabel);

        expenseTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(expenseTextArea);
        panel.add(scrollPane);

        getContentPane().add(panel);
        loadExpensesFromFile();
        revalidate();
        repaint();
    }

    private void addExpense() {
        String date = dateField.getText();
        String category = categoryField.getText();
        String amountText = amountField.getText();
        try {
            double amount = Double.parseDouble(amountText);
            Expense expense = new Expense(date, category, amount);
            expenses.add(expense);

            String expenseDetails = "Date: " + date + ", Category: " + category + ", Amount: $" + String.format("%.2f", amount) + "\n";
            expenseTextArea.append(expenseDetails);

            double totalExpenses = updateTotalExpenses(amount);
            totalExpenseLabel.setText("Total Expenses: $" + String.format("%.2f", totalExpenses));

            updateCategoryWiseExpenses(category, amount);

            dateField.setText("");
            categoryField.setText("");
            amountField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewCategoryWiseExpenses() {
        StringBuilder message = new StringBuilder("Category-wise Expenses:\n");
        for (Map.Entry<String, Double> entry : categoryWiseExpenses.entrySet()) {
            message.append(entry.getKey()).append(": $").append(String.format("%.2f", entry.getValue())).append("\n");
        }
        JOptionPane.showMessageDialog(this, message.toString(), "Category-wise Expenses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadExpensesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EXPENSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String date = parts[0];
                String category = parts[1];
                double amount = Double.parseDouble(parts[2]);

                Expense expense = new Expense(date, category, amount);
                expenses.add(expense);

                String expenseDetails = "Date: " + date + ", Category: " + category + ", Amount: $" + String.format("%.2f", amount) + "\n";
                expenseTextArea.append(expenseDetails);

                double totalExpenses = updateTotalExpenses(amount);
                totalExpenseLabel.setText("Total Expenses: $" + String.format("%.2f", totalExpenses));

                updateCategoryWiseExpenses(category, amount);
            }
            System.out.println("Expenses loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error loading expenses from file: " + e.getMessage());
        }
    }

    private double updateTotalExpenses(double amount) {
        String totalText = totalExpenseLabel.getText();
        String amountString = totalText.substring(totalText.indexOf('$') + 1);
        double total = Double.parseDouble(amountString) + amount;
        return total;
    }

    private void updateCategoryWiseExpenses(String category, double amount) {
        categoryWiseExpenses.put(category, categoryWiseExpenses.getOrDefault(category, 0.0) + amount);
    }

    private void saveExpensesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
            for (Expense expense : expenses) {
                writer.println(expense.getDate() + "," + expense.getCategory() + "," + expense.getAmount());
            }
            System.out.println("Expenses saved to file.");
        } catch (IOException e) {
            System.out.println("Error saving expenses to file: " + e.getMessage());
        }
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            writer.println(username + "," + password);
            JOptionPane.showMessageDialog(this, "Registration successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error registering user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    currentUser = username;
                    JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    showExpenseTrackerScreen();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error logging in: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ExpenseTrackerGUI gui = new ExpenseTrackerGUI();
                gui.setVisible(true);
            }
        });
    }

    static class Expense {
        private String date;
        private String category;
        private double amount;

        public Expense(String date, String category, double amount) {
            this.date = date;
            this.category = category;
            this.amount = amount;
        }

        public String getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }
    }
}
