package com.laioffer.job.db;

import com.laioffer.job.entity.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Create Java Class to Connect to MySQL
public class MySQLConnection {
    private Connection conn;

    public MySQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.URL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // close the connection
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Implement setFavoriteItems, unsetFavoriteItems and saveItems.
    // Syntax for INSERT
    // INSERT INTO table_name (column1, column2, column3, ...) VALUES (value1, value2, value3, ...);
    public void setFavoriteItems(String userId, Item item) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }
        saveItem(item);
        String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Syntax for DELETE
    // DELETE FROM table_name WHERE condition;
    public void unsetFavoriteItems(String userId, String itemId) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }
        String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveItem(Item item) {
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }
        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getLocation());
            statement.setString(4, item.getCompanyLogo());
            statement.setString(5, item.getUrl());
            statement.executeUpdate();

            sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
            statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            for (String keyword : item.getKeywords()) {
                statement.setString(2, keyword);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
