package com.example.racingfx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
  private static final String DEFAULT_URL = System.getenv().getOrDefault(
      "RACING_DB_URL",
      "jdbc:mysql://localhost:3306/RACING?useSSL=false&serverTimezone=UTC");
  private static final String DEFAULT_USER = System.getenv().getOrDefault(
      "RACING_DB_USER",
      "root");
  private static final String DEFAULT_PASS = System.getenv().getOrDefault(
      "RACING_DB_PASS",
      "YOUR_PASSWORD");

  private static String url = DEFAULT_URL;
  private static String user = DEFAULT_USER;
  private static String pass = DEFAULT_PASS;

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException ignored) {
      // mysql-connector-j uses service loader in modern versions, but load just in case
    }
  }

  private Database() {}

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, user, pass);
  }

  public static void configure(String jdbcUrl, String username, String password) {
    if (jdbcUrl != null && !jdbcUrl.isBlank()) url = jdbcUrl;
    if (username != null) user = username;
    if (password != null) pass = password;
  }
}

