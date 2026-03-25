package pr.sambogram;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/sambogram_db";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT id FROM Clients WHERE name = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getChatHistory(int limit) {
        List<String> history = new ArrayList<>();

        String sql = "SELECT h.senderName, t.message FROM History h " +
                "JOIN Text t ON h.textID = t.textID " +
                "ORDER BY h.date ASC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("senderName");
                String message = rs.getString("message");
                history.add(sender + ": " + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }


    public static void saveTextMessage(String sender, String recipient, String message) {
        String insertTextSQL = "INSERT INTO Text (message) VALUES (?)";
        String insertHistorySQL = "INSERT INTO History (senderName, recipientName, textID) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement textStmt = conn.prepareStatement(insertTextSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement historyStmt = conn.prepareStatement(insertHistorySQL)) {

            textStmt.setString(1, message);
            textStmt.executeUpdate();

            ResultSet rs = textStmt.getGeneratedKeys();
            if (rs.next()) {
                int textId = rs.getInt(1);
                historyStmt.setString(1, sender);
                historyStmt.setString(2, recipient != null ? recipient : "ALL");
                historyStmt.setInt(3, textId);
                historyStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}