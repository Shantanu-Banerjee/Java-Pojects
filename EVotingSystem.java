import java.awt.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

 class EVotingSystem {

    private JFrame mainFrame;
    private Connection connection;
    private Map<String, Integer> candidateVotes = new HashMap<>();
    private String currentUser = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new EVotingSystem().initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database connection failed.");
            }
        });
    }

    private void initialize() throws SQLException {
        // Database Connection
        String url = "jdbc:mysql://localhost:3306/evoting";
        String user = "root";
        String password = "Bsmps@1234"; 

        connection = DriverManager.getConnection(url, user, password);

        mainFrame = new JFrame("E-Voting System");
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createLoginPanel();

        mainFrame.setVisible(true);
    }

    private void createLoginPanel() {
        mainFrame.setJMenuBar(null); // Remove menu bar on logout

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
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
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        gbc.gridy = 3;
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
            registerUser(username, password);
        });

        backButton.addActionListener(e -> createLoginPanel());

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(registerPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void registerUser(String username, String password) {
        try {
            String hashedPassword = hashPassword(password);
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'voter')";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(mainFrame, "Registration successful!");
            createLoginPanel();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Registration failed.");
        }
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
                currentUser = username; // set session
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
            String checkVoteSql = "SELECT has_voted FROM users WHERE username = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkVoteSql);
            checkStmt.setString(1, currentUser);
            ResultSet checkResult = checkStmt.executeQuery();
    
            if (checkResult.next() && checkResult.getBoolean("has_voted")) {
                JOptionPane.showMessageDialog(mainFrame, "You have already voted!");
                createLoginPanel();
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error checking voting status.");
            return;
        }
    
        // Show voting panel
        JPanel voterPanel = new JPanel(new BorderLayout());
        JPanel votePanel = new JPanel(new GridLayout(0, 1));
    
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
    
            JButton voteButton = new JButton("Vote");
            voteButton.addActionListener(e -> {
                for (Component component : votePanel.getComponents()) {
                    if (component instanceof JRadioButton) {
                        JRadioButton radioButton = (JRadioButton) component;
                        if (radioButton.isSelected()) {
                            String selectedCandidate = radioButton.getText();
                            try {
                                connection.setAutoCommit(false); // Start transaction
    
                                // Increment vote count
                                String updateCandidateSql = "UPDATE candidates SET votes = votes + 1 WHERE candidate_name = ?";
                                PreparedStatement updateCandidateStmt = connection.prepareStatement(updateCandidateSql);
                                updateCandidateStmt.setString(1, selectedCandidate);
                                updateCandidateStmt.executeUpdate();
    
                                // Mark user as voted
                                String updateUserSql = "UPDATE users SET has_voted = TRUE WHERE username = ?";
                                PreparedStatement updateUserStmt = connection.prepareStatement(updateUserSql);
                                updateUserStmt.setString(1, currentUser);
                                updateUserStmt.executeUpdate();
    
                                connection.commit(); // Commit both updates
    
                                JOptionPane.showMessageDialog(mainFrame, "Vote cast successfully!");
                                createLoginPanel();
                                return;
    
                            } catch (SQLException ex) {
                                try {
                                    connection.rollback(); // Rollback if any error
                                } catch (SQLException rollbackEx) {
                                    rollbackEx.printStackTrace();
                                }
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(mainFrame, "Voting failed.");
                            } finally {
                                try {
                                    connection.setAutoCommit(true); // Reset to default
                                } catch (SQLException ex1) {
                                    ex1.printStackTrace();
                                }
                            }
                        }
                    }
                }
                JOptionPane.showMessageDialog(mainFrame, "Please select a candidate.");
            });
    
            voterPanel.add(votePanel, BorderLayout.CENTER);
            voterPanel.add(voteButton, BorderLayout.SOUTH);
            voterPanel.add(createLogoutButton(), BorderLayout.NORTH);
    
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(voterPanel);
            mainFrame.revalidate();
            mainFrame.repaint();
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error loading candidates.");
        }
    }
    
    private void createAdminPanel() {
        JMenuBar menuBar = new JMenuBar();
        JMenu resultsMenu = new JMenu("Results");
        JMenuItem viewResultsItem = new JMenuItem("View Results");
        resultsMenu.add(viewResultsItem);
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

        viewResultsItem.addActionListener(e -> showResults());

        JPanel adminPanel = new JPanel();
        adminPanel.add(new JLabel("Welcome, Admin"));

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(adminPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void showResults() {
        JPanel resultsPanel = new JPanel(new GridLayout(0, 2));

        try {
            String sql = "SELECT candidate_name, votes FROM candidates";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                resultsPanel.add(new JLabel(resultSet.getString("candidate_name")));
                resultsPanel.add(new JLabel(String.valueOf(resultSet.getInt("votes"))));
            }

            JFrame resultsFrame = new JFrame("Voting Results");
            resultsFrame.setSize(400, 300);
            resultsFrame.add(resultsPanel);
            resultsFrame.setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error retrieving results");
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


