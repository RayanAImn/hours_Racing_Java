package com.example.racingfx.db;

import java.sql.*;

public class Db {
private static final String URL="jdbc:mysql://localhost:3306/RACING?useSSL=false&serverTimezone=UTC";
private static final String USER="root";
private static final String PASS="YOUR_PASSWORD";
public static Connection get() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
}
