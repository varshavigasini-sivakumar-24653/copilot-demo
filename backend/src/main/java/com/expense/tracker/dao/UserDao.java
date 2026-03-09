package com.expense.tracker.dao;

import com.expense.tracker.model.User;
import com.expense.tracker.util.Database;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class UserDao {

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, name, picture_url, role, created_at FROM users WHERE email = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id, email, name, picture_url, role, created_at FROM users WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = Database.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public User insert(String email, String name, String pictureUrl, String role) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO users (id, email, name, picture_url, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, email);
            ps.setString(3, name);
            ps.setString(4, pictureUrl);
            ps.setString(5, role != null ? role : "USER");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findByEmail(email).orElseThrow();
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setEmail(rs.getString("email"));
        u.setName(rs.getString("name"));
        u.setPictureUrl(rs.getString("picture_url"));
        u.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        u.setCreatedAt(ts != null ? ts.toInstant() : null);
        return u;
    }

    public java.util.List<User> findAll() {
        String sql = "SELECT id, email, name, picture_url, role, created_at FROM users ORDER BY created_at DESC";
        java.util.List<User> list = new java.util.ArrayList<>();
        try (Connection c = Database.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
