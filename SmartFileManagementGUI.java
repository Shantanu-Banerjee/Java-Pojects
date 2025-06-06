import java.awt.*;
import java.io.*;
import java.sql.*;
import javax.swing.*;

interface FileOperations {
    void createFile(String fileName) throws IOException;
    void writeFile(String fileName, String content) throws IOException;
    String readFile(String fileName) throws IOException;
    void deleteFile(String fileName) throws IOException;
    void listFiles(String directoryPath);
}

class DBLogger {
    private final String url = "jdbc:mysql://localhost:3306/filemanagerdb";
    private final String user = "root"; // change if needed
    private final String password = "Bsmps@1234"; // replace with your MySQL password

    public DBLogger() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found.");
        }
    }

    public void log(String operation, String fileName, String status) {
        String query = "INSERT INTO FileLogs (operation, fileName, status) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, operation);
            stmt.setString(2, fileName);
            stmt.setString(3, status);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("⚠ JDBC Logging Failed: " + e.getMessage());
        }
    }
}

class SmartFileManager implements FileOperations {
    private final String baseDirectory;
    private final DBLogger dbLogger = new DBLogger();

    public SmartFileManager(String baseDirectory) {
        this.baseDirectory = baseDirectory;
        File dir = new File(baseDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void createFile(String fileName) throws IOException {
        File file = new File(baseDirectory + File.separator + fileName);
        if (file.createNewFile()) {
            JOptionPane.showMessageDialog(null, "✅ File created: " + file.getName());
            dbLogger.log("CREATE", fileName, "Success");
        } else {
            JOptionPane.showMessageDialog(null, "⚠ File already exists.");
            dbLogger.log("CREATE", fileName, "Already Exists");
        }
    }

    @Override
    public void writeFile(String fileName, String content) throws IOException {
        FileWriter writer = new FileWriter(baseDirectory + File.separator + fileName, true);
        writer.write(content + System.lineSeparator());
        writer.close();
        JOptionPane.showMessageDialog(null, "✅ Successfully wrote to the file.");
        dbLogger.log("WRITE", fileName, "Success");
    }

    @Override
    public String readFile(String fileName) throws IOException {
        File file = new File(baseDirectory + File.separator + fileName);
        if (!file.exists()) {
            dbLogger.log("READ", fileName, "File Not Found");
            throw new FileNotFoundException("File not found: " + fileName);
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        dbLogger.log("READ", fileName, "Success");
        return content.toString();
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        File file = new File(baseDirectory + File.separator + fileName);
        if (file.delete()) {
            JOptionPane.showMessageDialog(null, "✅ Deleted file: " + fileName);
            dbLogger.log("DELETE", fileName, "Success");
        } else {
            dbLogger.log("DELETE", fileName, "Failure");
            throw new IOException("Failed to delete the file.");
        }
    }

    @Override
    public void listFiles(String directoryPath) {
        File dir = new File(baseDirectory + File.separator + directoryPath);
        if (dir.exists() && dir.isDirectory()) {
            String[] files = dir.list();
            StringBuilder sb = new StringBuilder("📂 Files in directory:\n");
            if (files != null && files.length > 0) {
                for (String file : files) {
                    sb.append(" - ").append(file).append("\n");
                }
            } else {
                sb.append(" (No files found)");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
            dbLogger.log("LIST", directoryPath, "Success");
        } else {
            JOptionPane.showMessageDialog(null, "⚠ Directory does not exist.");
            dbLogger.log("LIST", directoryPath, "Directory Not Found");
        }
    }
}

public class SmartFileManagementGUI extends JFrame {
    private final SmartFileManager manager = new SmartFileManager("ManagedFiles");
    private final JTextField fileNameField = new JTextField(20);
    private final JTextField directoryField = new JTextField(20);
    private final JTextArea fileContentArea = new JTextArea(10, 30);
    private final JButton createBtn = new JButton("Create File");
    private final JButton writeBtn = new JButton("Write to File");
    private final JButton readBtn = new JButton("Read File");
    private final JButton deleteBtn = new JButton("Delete File");
    private final JButton listBtn = new JButton("List Files");
    private final JButton clearBtn = new JButton("Clear Content");

    public SmartFileManagementGUI() {
        setTitle("Smart File Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        topPanel.add(new JLabel("Enter File Name:"));
        topPanel.add(fileNameField);
        topPanel.add(new JLabel("Enter Directory Name (or '.' for base):"));
        topPanel.add(directoryField);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel();
        fileContentArea.setLineWrap(true);
        fileContentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(fileContentArea);
        centerPanel.add(scrollPane);
        add(centerPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createBtn);
        buttonPanel.add(writeBtn);
        buttonPanel.add(readBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(listBtn);
        buttonPanel.add(clearBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        createBtn.addActionListener(e -> {
            try {
                manager.createFile(fileNameField.getText().trim());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        writeBtn.addActionListener(e -> {
            try {
                manager.writeFile(fileNameField.getText().trim(), fileContentArea.getText().trim());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        readBtn.addActionListener(e -> {
            try {
                String content = manager.readFile(fileNameField.getText().trim());
                fileContentArea.setText(content);
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        deleteBtn.addActionListener(e -> {
            try {
                manager.deleteFile(fileNameField.getText().trim());
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        listBtn.addActionListener(e -> {
            manager.listFiles(directoryField.getText().trim());
        });

        clearBtn.addActionListener(e -> {
            fileContentArea.setText("");
        });

        setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, "❌ Error: " + message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartFileManagementGUI::new);
    }
}
