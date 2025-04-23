import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class RestaurantGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Customer customer;
    private Menu menu;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private Connection connection;
    private Statement statement;
    private List<String> selectedItems = new ArrayList<>();
    private List<Integer> selectedPrices = new ArrayList<>();
    private List<Integer> itemQuantities = new ArrayList<>();  // Track quantities
    private Map<String, Integer> itemIndexMap = new HashMap<>();  // Track item positions
    private DefaultListModel<String> orderListModel = new DefaultListModel<>();
    private JTextArea billArea;
    
    public RestaurantGUI() {
        // Set up the main frame
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Initialize database connection
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/restaurant",
                "postgres", "@Rishred1"
            );
            connection.setAutoCommit(false);
            statement = connection.createStatement();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + e.getMessage());
        }

        // Initialize card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        // Create and add different panels
        createLoginPanel();
        createMenuPanel();
        createOrderPanel();
        createBillPanel();

        // Show login panel first
        cardLayout.show(mainPanel, "login");
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Restaurant Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Username field
        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        // Password field
        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(e -> handleLogin());

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(e -> handleRegister());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        mainPanel.add(loginPanel, "login");
    }

    private void createMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout(10, 10));
        JLabel titleLabel = new JLabel("Menu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        menuPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel menuItemsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        menuItemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(menuItemsPanel);
        menuPanel.add(scrollPane, BorderLayout.CENTER);

        try {
            menu = new Menu();
            menu.display(connection, statement);
            
            List<String> items = menu.getMenuItems();
            List<Integer> prices = menu.getPrices();
            
            for (int i = 0; i < items.size(); i++) {
                final int index = i;
                final String itemName = items.get(i);
                itemIndexMap.put(itemName, i);
                
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                
                // Item details panel (name and price)
                JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel itemLabel = new JLabel(items.get(i) + " - $" + prices.get(i));
                itemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                detailsPanel.add(itemLabel);
                itemPanel.add(detailsPanel, BorderLayout.CENTER);
                
                // Quantity control panel
                JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                JButton minusButton = new JButton("-");
                JLabel quantityLabel = new JLabel("0");
                JButton plusButton = new JButton("+");
                
                // Style the buttons
                Dimension buttonSize = new Dimension(40, 30);
                minusButton.setPreferredSize(buttonSize);
                plusButton.setPreferredSize(buttonSize);
                quantityLabel.setPreferredSize(new Dimension(30, 30));
                quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
                quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
                
                // Add item action
                plusButton.addActionListener(e -> {
                    int currentQty = Integer.parseInt(quantityLabel.getText());
                    currentQty++;
                    quantityLabel.setText(String.valueOf(currentQty));
                    
                    // Add item to order
                    selectedItems.add(itemName);
                    selectedPrices.add(prices.get(index));
                    updateOrderList();
                    updateBillPanel();
                });
                
                // Remove item action
                minusButton.addActionListener(e -> {
                    int currentQty = Integer.parseInt(quantityLabel.getText());
                    if (currentQty > 0) {
                        currentQty--;
                        quantityLabel.setText(String.valueOf(currentQty));
                        
                        // Remove last occurrence of item
                        int itemIndex = selectedItems.lastIndexOf(itemName);
                        if (itemIndex != -1) {
                            selectedItems.remove(itemIndex);
                            selectedPrices.remove(itemIndex);
                            updateOrderList();
                            updateBillPanel();
                        }
                    }
                });
                
                quantityPanel.add(minusButton);
                quantityPanel.add(quantityLabel);
                quantityPanel.add(plusButton);
                itemPanel.add(quantityPanel, BorderLayout.EAST);
                
                menuItemsPanel.add(itemPanel);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading menu: " + e.getMessage());
        }

        JButton orderButton = new JButton("View Order");
        orderButton.setFont(new Font("Arial", Font.BOLD, 14));
        orderButton.addActionListener(e -> cardLayout.show(mainPanel, "order"));
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(orderButton);
        menuPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(menuPanel, "menu");
    }

    private void createOrderPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Your Order", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        orderPanel.add(titleLabel, BorderLayout.NORTH);

        // Create order list with selection
        JList<String> orderList = new JList<>(orderListModel);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(orderList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        orderPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton backButton = new JButton("Back to Menu");
        JButton removeButton = new JButton("Remove Selected");
        JButton confirmButton = new JButton("Confirm Order");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 12);
        Dimension buttonSize = new Dimension(120, 30);
        
        backButton.setFont(buttonFont);
        backButton.setPreferredSize(buttonSize);
        
        removeButton.setFont(buttonFont);
        removeButton.setPreferredSize(buttonSize);
        
        confirmButton.setFont(buttonFont);
        confirmButton.setPreferredSize(buttonSize);
        
        // Remove button action
        removeButton.addActionListener(e -> {
            int selectedIndex = orderList.getSelectedIndex();
            if (selectedIndex != -1) {
                String removedItem = selectedItems.get(selectedIndex);
                selectedItems.remove(selectedIndex);
                selectedPrices.remove(selectedIndex);
                orderListModel.remove(selectedIndex);
                updateBillPanel();
                JOptionPane.showMessageDialog(this, 
                    removedItem + " removed from order!", 
                    "Item Removed", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select an item to remove!", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        confirmButton.addActionListener(e -> {
            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please add items to your order first!", 
                    "Empty Order", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                cardLayout.show(mainPanel, "bill");
            }
        });

        // Add buttons to panel
        buttonsPanel.add(backButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(confirmButton);
        orderPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(orderPanel, "order");
    }

    private void createBillPanel() {
        JPanel billPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bill", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        billPanel.add(titleLabel, BorderLayout.NORTH);

        // Bill details area
        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        updateBillPanel();
        
        JScrollPane scrollPane = new JScrollPane(billArea);
        billPanel.add(scrollPane, BorderLayout.CENTER);

        // Payment button panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton payButton = new JButton("Pay and Exit");
        payButton.addActionListener(e -> handlePayment());
        bottomPanel.add(payButton);
        billPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(billPanel, "bill");
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        customer = new Customer(username, password);
        if (customer.login(customer, connection, statement, null)) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            cardLayout.show(mainPanel, "menu");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username and password cannot be empty!", 
                "Registration Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Check if username already exists
            Statement checkStmt = connection.createStatement();
            ResultSet rs = checkStmt.executeQuery(
                "SELECT username FROM customer WHERE username='" + username + "'");
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, 
                    "Username already exists! Please choose another.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create new customer
            customer = new Customer(username, password);
            customer.Create(connection, statement, username, password);

            JOptionPane.showMessageDialog(this, 
                "Registration successful! Please login.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);

            // Clear fields
            usernameField.setText("");
            passwordField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error during registration: " + e.getMessage(), 
                "Registration Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePayment() {
        // Implement payment logic
        JOptionPane.showMessageDialog(this, "Thank you for your order!");
        System.exit(0);
    }

    private void updateBillPanel() {
        StringBuilder billText = new StringBuilder();
        billText.append("Your Order:\n\n");
        
        // Get item counts
        Map<String, Integer> itemCount = new HashMap<>();
        for (String item : selectedItems) {
            itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
        }

        billText.append(String.format("Total Items Selected: %d\n\n", selectedItems.size()));
        billText.append(String.format("%-4s %-25s %10s %12s\n", "Qty", "Item", "Price", "Total"));
        billText.append("----------------------------------------------------\n");
        
        int grandTotal = 0;
        // Display items with quantities and subtotals
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            String item = entry.getKey();
            int quantity = entry.getValue();
            int index = itemIndexMap.get(item);
            int unitPrice = menu.getPrices().get(index);
            int itemTotal = unitPrice * quantity;
            grandTotal += itemTotal;
            
            billText.append(String.format("%-4d %-25s $%8d $%10d\n", 
                quantity, 
                item, 
                unitPrice,
                itemTotal));
        }
        
        billText.append("\n====================================================\n");
        billText.append(String.format("Grand Total: $%d", grandTotal));
        
        if (billArea != null) {
            billArea.setText(billText.toString());
        }
    }

    private void updateOrderList() {
        orderListModel.clear();
        Map<String, Integer> itemCount = new HashMap<>();
        
        // Count quantities of each item
        for (String item : selectedItems) {
            itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
        }
        
        // Add items with quantities to the order list
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            String item = entry.getKey();
            int quantity = entry.getValue();
            int index = itemIndexMap.get(item);
            int price = menu.getPrices().get(index);
            orderListModel.addElement(String.format("%dx %s - $%d", quantity, item, price * quantity));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantGUI gui = new RestaurantGUI();
            gui.setVisible(true);
        });
    }
}