package com.example.racingfx.dao;
import com.example.racingfx.db.Db;
import java.sql.*;
import java.util.*;

public class ReportsDao {
  public List<Map<String,Object>> horsesByOwnerLastName(String lname) throws SQLException {
    String sql = """
      SELECT h.horseName, h.age, t.fname AS trainerF, t.lname AS trainerL
      FROM Owner o
      JOIN Owns ow ON o.ownerId = ow.ownerId
      JOIN Horse h ON h.horseId = ow.horseId
      LEFT JOIN Trainer t ON t.stableId = h.stableId
      WHERE o.lname = ?
      ORDER BY h.horseName
    """;
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1,lname);
      try (ResultSet rs = ps.executeQuery()) { return rows(rs); }
    }
  }

  public List<Map<String,Object>> winningTrainers() throws SQLException {
    String sql = """
      SELECT DISTINCT tr.fname AS trainerF, tr.lname AS trainerL,
             h.horseName, r.raceName, r.trackName, r.raceDate
      FROM Trainer tr
      JOIN Horse h ON h.stableId = tr.stableId
      JOIN RaceResults rr ON rr.horseId = h.horseId AND rr.results = 'first'
      JOIN Race r ON r.raceId = rr.raceId
      ORDER BY tr.lname, tr.fname, r.raceDate DESC
    """;
    try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      return rows(rs);
    }
  }

  public List<Map<String,Object>> trainerWinnings() throws SQLException {
    String sql = """
      SELECT tr.fname AS trainerF, tr.lname AS trainerL,
             COALESCE(SUM(rr.prize),0) AS totalWinnings
      FROM Trainer tr
      LEFT JOIN Horse h ON h.stableId = tr.stableId
      LEFT JOIN RaceResults rr ON rr.horseId = h.horseId
      GROUP BY tr.trainerId
      ORDER BY totalWinnings DESC
    """;
    try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      return rows(rs);
    }
  }

  public List<Map<String,Object>> trackStats() throws SQLException {
    String sql = """
      SELECT r.trackName,
             COUNT(DISTINCT r.raceId) AS racesCount,
             COUNT(DISTINCT rr.horseId) AS horsesCount
      FROM Race r
      LEFT JOIN RaceResults rr ON rr.raceId = r.raceId
      GROUP BY r.trackName
      ORDER BY r.trackName
    """;
    try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      return rows(rs);
    }
  }

  private static List<Map<String,Object>> rows(ResultSet rs) throws SQLException {
    List<Map<String,Object>> out = new ArrayList<>();
    ResultSetMetaData md = rs.getMetaData();
    int n = md.getColumnCount();
    while (rs.next()) {
      Map<String,Object> m = new LinkedHashMap<>();
      for (int i=1;i<=n;i++) m.put(md.getColumnLabel(i), rs.getObject(i));
      out.add(m);
    }
    return out;
  }
}

