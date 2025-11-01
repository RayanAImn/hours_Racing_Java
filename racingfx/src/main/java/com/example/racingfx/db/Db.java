package com.example.racingfx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
private static final String URL="jdbc:mysql://localhost:3306/RACING?useSSL=false&serverTimezone=UTC";
private static final String USER="root";
private static final String PASS="123456789";
public static Connection get() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
}
  