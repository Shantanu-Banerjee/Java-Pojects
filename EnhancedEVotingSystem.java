import java.awt.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import java.security.SecureRandom;
import java.math.BigInteger;

// Blockchain Service Interface
interface BlockchainService {
    String castVote(String voterAddress, String candidateName, String signature) throws BlockchainException;
    boolean verifyVoter(String voterAddress) throws BlockchainException;
    Map<String, Integer> getVoteResults() throws BlockchainException;
    String registerVoterOnBlockchain(String voterHash) throws BlockchainException;
    boolean isBlockchainConnected();
}

// Mock Blockchain Implementation (replace with actual Web3j integration)
class MockBlockchainService implements BlockchainService {
    private Map<String, String> voterRegistry = new HashMap<>();
    private Map<String, Integer> blockchainVotes = new HashMap<>();
    private Map<String, Boolean> votedAddresses = new HashMap<>();
    private boolean connected = true;
    
    @Override
    public String registerVoterOnBlockchain(String voterHash) throws BlockchainException {
        if (!connected) throw new BlockchainException("Blockchain not connected");
        
        // Simulate blockchain address generation
        String address = "0x" + generateRandomHex(40);
        voterRegistry.put(voterHash, address);
        
        // Simulate transaction hash
        return "0x" + generateRandomHex(64);
    }
    
    @Override
    public String castVote(String voterAddress, String candidateName, String signature) throws BlockchainException {
        if (!connected) throw new BlockchainException("Blockchain not connected");
        
        if (votedAddresses.getOrDefault(voterAddress, false)) {
            throw new BlockchainException("Address already voted on blockchain");
        }
        
        // Record vote on blockchain
        blockchainVotes.put(candidateName, blockchainVotes.getOrDefault(candidateName, 0) + 1);
        votedAddresses.put(voterAddress, true);
        
        // Simulate transaction hash
        return "0x" + generateRandomHex(64);
    }
    
    @Override
    public boolean verifyVoter(String voterAddress) throws BlockchainException {
        if (!connected) throw new BlockchainException("Blockchain not connected");
        return voterRegistry.containsValue(voterAddress) && !votedAddresses.getOrDefault(voterAddress, false);
    }
    
    @Override
    public Map<String, Integer> getVoteResults() throws BlockchainException {
        if (!connected) throw new BlockchainException("Blockchain not connected");
        return new HashMap<>(blockchainVotes);
    }
    
    @Override
    public boolean isBlockchainConnected() {
        return connected;
    }
    
    private String generateRandomHex(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }
}

// Custom Exception for Blockchain operations
class BlockchainException extends Exception {
    public BlockchainException(String message) {
        super(message);
    }
    
    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class EnhancedEVotingSystem {

    private JFrame mainFrame;
    private Connection connection;
    private Map<String, Integer> candidateVotes = new HashMap<>();
    private String currentUser = null;
    private BlockchainService blockchainService;
    private JLabel blockchainStatusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new EnhancedEVotingSystem().initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database connection failed.");
            }
        });
    }

    private void initialize() throws SQLException {
        // Initialize blockchain service
        blockchainService = new MockBlockchainService(); // Replace with actual implementation
        
        // Database Connection
        String url = "jdbc:mysql://localhost:3306/evoting";
        String user = "root";
        String password = "Bsmps@123"; 

        connection = DriverManager.getConnection(url, user, password);
        
        // Create enhanced database schema
        createEnhancedSchema();

        mainFrame = new JFrame("Blockchain-Enhanced E-Voting System");
        mainFrame.setSize(900, 700);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createLoginPanel();

        mainFrame.setVisible(true);
    }
    
    private void createEnhancedSchema() throws SQLException {
        // Add blockchain-related columns to existing tables
        try {
            String alterUsersSql = "ALTER TABLE users ADD COLUMN IF NOT EXISTS blockchain_address VARCHAR(42), " +
                                 "ADD COLUMN IF NOT EXISTS blockchain_tx_hash VARCHAR(66), " +
                                 "ADD COLUMN IF NOT EXISTS voter_hash VARCHAR(64)";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(alterUsersSql);
            
            // Create blockchain sync table
            String createSyncTableSql = "CREATE TABLE IF NOT EXISTS blockchain_sync (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                      "last_block_number BIGINT DEFAULT 0, " +
                                      "last_sync_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                      ")";
            stmt.executeUpdate(createSyncTableSql);
            
            // Initialize sync record if not exists
            String initSyncSql = "INSERT IGNORE INTO blockchain_sync (id, last_block_number) VALUES (1, 0)";
            stmt.executeUpdate(initSyncSql);
            
        } catch (SQLException e) {
            System.out.println("Schema enhancement completed or already exists");
        }
    }

    private void createLoginPanel() {
        mainFrame.setJMenuBar(null);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Add blockchain status indicator
        blockchainStatusLabel = new JLabel();
        updateBlockchainStatus();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 10, 5);
        loginPanel.add(blockchainStatusLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        gbc.gridy = 4;
        loginPanel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            authenticateUser(username, password);
        });

        registerButton.addActionListener(e -> createRegisterPanel());

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(loginPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }
    
    private void updateBlockchainStatus() {
        boolean connected = blockchainService.isBlockchainConnected();
        String status = connected ? "🟢 Blockchain Connected" : "🔴 Blockchain Disconnected";
        Color color = connected ? Color.GREEN : Color.RED;
        
        blockchainStatusLabel.setText(status);
        blockchainStatusLabel.setForeground(color);
    }

    private void createRegisterPanel() {
        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        registerPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        registerPanel.add(usernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        registerPanel.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerPanel.add(registerButton, gbc);
        gbc.gridy = 3;
        registerPanel.add(backButton, gbc);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            registerUserWithBlockchain(username, password);
        });

        backButton.addActionListener(e -> createLoginPanel());

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(registerPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void registerUserWithBlockchain(String username, String password) {
        // Show progress dialog
        JDialog progressDialog = new JDialog(mainFrame, "Registering on Blockchain", true);
        JLabel progressLabel = new JLabel("Registering voter on blockchain...");
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(progressLabel, BorderLayout.CENTER);
        progressDialog.add(progressBar, BorderLayout.SOUTH);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(mainFrame);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String hashedPassword = hashPassword(password);
                    String voterHash = hashPassword(username + System.currentTimeMillis());
                    
                    // Register on blockchain first
                    String blockchainTxHash = blockchainService.registerVoterOnBlockchain(voterHash);
                    
                    // Then register in SQL database
                    String sql = "INSERT INTO users (username, password, role, voter_hash, blockchain_tx_hash) VALUES (?, ?, 'voter', ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, hashedPassword);
                    preparedStatement.setString(3, voterHash);
                    preparedStatement.setString(4, blockchainTxHash);
                    preparedStatement.executeUpdate();
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(mainFrame, 
                            "Registration successful!\nYour identity has been registered on the blockchain.");
                        createLoginPanel();
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, 
                            "Registration failed. Please try again.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Registration failed: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }

    private void authenticateUser(String username, String password) {
        try {
            String hashedPassword = hashPassword(password);
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("role");
                currentUser = username;
                if ("admin".equals(role)) {
                    createAdminPanel();
                } else {
                    createVoterPanel();
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Invalid username or password.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Authentication failed.");
        }
    }

    private void createVoterPanel() {
        try {
            // Check if user has already voted
            String checkVoteSql = "SELECT has_voted, blockchain_address, voter_hash FROM users WHERE username = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkVoteSql);
            checkStmt.setString(1, currentUser);
            ResultSet checkResult = checkStmt.executeQuery();
    
            if (!checkResult.next()) {
                JOptionPane.showMessageDialog(mainFrame, "User not found!");
                createLoginPanel();
                return;
            }
            
            if (checkResult.getBoolean("has_voted")) {
                JOptionPane.showMessageDialog(mainFrame, "You have already voted!");
                createLoginPanel();
                return;
            }
            
            String blockchainAddress = checkResult.getString("blockchain_address");
            String voterHash = checkResult.getString("voter_hash");
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error checking voting status.");
            return;
        }
    
        // Show enhanced voting panel
        JPanel voterPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new FlowLayout());
        
        // Add blockchain status and logout
        updateBlockchainStatus();
        headerPanel.add(blockchainStatusLabel);
        headerPanel.add(createLogoutButton());
        
        JPanel votePanel = new JPanel(new GridLayout(0, 1));
        JLabel instructionLabel = new JLabel("Select your candidate and cast your vote:");
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        votePanel.add(instructionLabel);
    
        try {
            String sql = "SELECT candidate_name FROM candidates";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
    
            ButtonGroup group = new ButtonGroup();
    
            while (resultSet.next()) {
                String candidateName = resultSet.getString("candidate_name");
                JRadioButton radioButton = new JRadioButton(candidateName);
                group.add(radioButton);
                votePanel.add(radioButton);
            }
    
            JButton voteButton = new JButton("Cast Vote on Blockchain");
            voteButton.setBackground(new Color(0, 123, 255));
            voteButton.setForeground(Color.WHITE);
            voteButton.setFont(new Font("Arial", Font.BOLD, 12));
            
            voteButton.addActionListener(e -> {
                for (Component component : votePanel.getComponents()) {
                    if (component instanceof JRadioButton) {
                        JRadioButton radioButton = (JRadioButton) component;
                        if (radioButton.isSelected()) {
                            String selectedCandidate = radioButton.getText();
                            castVoteWithBlockchain(selectedCandidate);
                            return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(mainFrame, "Please select a candidate.");
            });
    
            voterPanel.add(headerPanel, BorderLayout.NORTH);
            voterPanel.add(votePanel, BorderLayout.CENTER);
            voterPanel.add(voteButton, BorderLayout.SOUTH);
    
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(voterPanel);
            mainFrame.revalidate();
            mainFrame.repaint();
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error loading candidates.");
        }
    }
    
    private void castVoteWithBlockchain(String selectedCandidate) {
        // Show progress dialog
        JDialog progressDialog = new JDialog(mainFrame, "Recording Vote", true);
        JLabel progressLabel = new JLabel("Recording your vote on the blockchain...");
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(progressLabel, BorderLayout.CENTER);
        progressDialog.add(progressBar, BorderLayout.SOUTH);
        progressDialog.setSize(350, 100);
        progressDialog.setLocationRelativeTo(mainFrame);
        
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    // Get voter's blockchain info
                    String getUserSql = "SELECT blockchain_address, voter_hash FROM users WHERE username = ?";
                    PreparedStatement getUserStmt = connection.prepareStatement(getUserSql);
                    getUserStmt.setString(1, currentUser);
                    ResultSet userResult = getUserStmt.executeQuery();
                    
                    if (!userResult.next()) {
                        throw new Exception("Voter not found");
                    }
                    
                    String voterAddress = userResult.getString("blockchain_address");
                    if (voterAddress == null) {
                        // Generate blockchain address if not exists
                        voterAddress = "0x" + hashPassword(currentUser + System.currentTimeMillis()).substring(0, 40);
                        String updateAddressSql = "UPDATE users SET blockchain_address = ? WHERE username = ?";
                        PreparedStatement updateStmt = connection.prepareStatement(updateAddressSql);
                        updateStmt.setString(1, voterAddress);
                        updateStmt.setString(2, currentUser);
                        updateStmt.executeUpdate();
                    }
                    
                    // Cast vote on blockchain
                    String signature = hashPassword(selectedCandidate + currentUser + System.currentTimeMillis());
                    String blockchainTxHash = blockchainService.castVote(voterAddress, selectedCandidate, signature);
                    
                    // Update SQL database
                    connection.setAutoCommit(false);
                    
                    // Increment vote count
                    String updateCandidateSql = "UPDATE candidates SET votes = votes + 1 WHERE candidate_name = ?";
                    PreparedStatement updateCandidateStmt = connection.prepareStatement(updateCandidateSql);
                    updateCandidateStmt.setString(1, selectedCandidate);
                    updateCandidateStmt.executeUpdate();
                    
                    // Mark user as voted
                    String updateUserSql = "UPDATE users SET has_voted = TRUE, blockchain_tx_hash = ? WHERE username = ?";
                    PreparedStatement updateUserStmt = connection.prepareStatement(updateUserSql);
                    updateUserStmt.setString(1, blockchainTxHash);
                    updateUserStmt.setString(2, currentUser);
                    updateUserStmt.executeUpdate();
                    
                    connection.commit();
                    
                    return blockchainTxHash;
                    
                } catch (Exception ex) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    throw ex;
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException ex1) {
                        ex1.printStackTrace();
                    }
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    String txHash = get();
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Vote successfully recorded on blockchain!\n" +
                        "Transaction Hash: " + txHash.substring(0, 16) + "...",
                        "Vote Recorded", JOptionPane.INFORMATION_MESSAGE);
                    createLoginPanel();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Failed to record vote: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    private void createAdminPanel() {
        JMenuBar menuBar = new JMenuBar();
        JMenu resultsMenu = new JMenu("Results");
        JMenuItem viewResultsItem = new JMenuItem("View Database Results");
        JMenuItem viewBlockchainResultsItem = new JMenuItem("View Blockchain Results");
        JMenuItem compareResultsItem = new JMenuItem("Compare Results");
        
        resultsMenu.add(viewResultsItem);
        resultsMenu.add(viewBlockchainResultsItem);
        resultsMenu.add(compareResultsItem);
        menuBar.add(resultsMenu);

        JMenu sessionMenu = new JMenu("Session");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            currentUser = null;
            createLoginPanel();
            mainFrame.setJMenuBar(null);
        });
        sessionMenu.add(logoutItem);
        menuBar.add(sessionMenu);

        mainFrame.setJMenuBar(menuBar);

        viewResultsItem.addActionListener(e -> showResults(false));
        viewBlockchainResultsItem.addActionListener(e -> showResults(true));
        compareResultsItem.addActionListener(e -> compareResults());

        JPanel adminPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, Admin - Blockchain-Enhanced E-Voting System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        
        updateBlockchainStatus();
        JPanel statusPanel = new JPanel();
        statusPanel.add(blockchainStatusLabel);
        
        adminPanel.add(welcomeLabel, BorderLayout.CENTER);
        adminPanel.add(statusPanel, BorderLayout.SOUTH);

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(adminPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void showResults(boolean fromBlockchain) {
        JPanel resultsPanel = new JPanel(new GridLayout(0, 2));
        String title = fromBlockchain ? "Blockchain Results" : "Database Results";

        try {
            if (fromBlockchain) {
                Map<String, Integer> blockchainResults = blockchainService.getVoteResults();
                for (Map.Entry<String, Integer> entry : blockchainResults.entrySet()) {
                    resultsPanel.add(new JLabel(entry.getKey()));
                    resultsPanel.add(new JLabel(String.valueOf(entry.getValue())));
                }
            } else {
                String sql = "SELECT candidate_name, votes FROM candidates";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()) {
                    resultsPanel.add(new JLabel(resultSet.getString("candidate_name")));
                    resultsPanel.add(new JLabel(String.valueOf(resultSet.getInt("votes"))));
                }
            }

            JFrame resultsFrame = new JFrame(title);
            resultsFrame.setSize(400, 300);
            resultsFrame.add(resultsPanel);
            resultsFrame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error retrieving " + title);
        }
    }
    
    private void compareResults() {
        try {
            // Get database results
            Map<String, Integer> dbResults = new HashMap<>();
            String sql = "SELECT candidate_name, votes FROM candidates";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            
            while (resultSet.next()) {
                dbResults.put(resultSet.getString("candidate_name"), resultSet.getInt("votes"));
            }
            
            // Get blockchain results
            Map<String, Integer> blockchainResults = blockchainService.getVoteResults();
            
            // Create comparison panel
            JPanel comparisonPanel = new JPanel(new GridLayout(0, 3));
            comparisonPanel.add(new JLabel("Candidate"));
            comparisonPanel.add(new JLabel("Database Votes"));
            comparisonPanel.add(new JLabel("Blockchain Votes"));
            
            for (String candidate : dbResults.keySet()) {
                comparisonPanel.add(new JLabel(candidate));
                
                int dbVotes = dbResults.getOrDefault(candidate, 0);
                int blockchainVotes = blockchainResults.getOrDefault(candidate, 0);
                
                JLabel dbLabel = new JLabel(String.valueOf(dbVotes));
                JLabel blockchainLabel = new JLabel(String.valueOf(blockchainVotes));
                
                // Color code mismatches
                if (dbVotes != blockchainVotes) {
                    dbLabel.setForeground(Color.RED);
                    blockchainLabel.setForeground(Color.RED);
                } else {
                    dbLabel.setForeground(Color.GREEN);
                    blockchainLabel.setForeground(Color.GREEN);
                }
                
                comparisonPanel.add(dbLabel);
                comparisonPanel.add(blockchainLabel);
            }
            
            JFrame comparisonFrame = new JFrame("Database vs Blockchain Comparison");
            comparisonFrame.setSize(500, 400);
            comparisonFrame.add(new JScrollPane(comparisonPanel));
            comparisonFrame.setVisible(true);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error comparing results");
        }
    }

    private JButton createLogoutButton() {
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            createLoginPanel();
        });
        return logoutButton;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}