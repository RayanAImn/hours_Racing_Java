package com.example.racingfx.dao;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.example.racingfx.db.Db;
public class AdminDao {
  // Insert using provided raceId
  public void addRaceWithResults(String raceId, String raceName, String trackName,
                                 java.sql.Date raceDate, java.sql.Time raceTime,
                                 List<ResultRow> results) throws SQLException {
    Connection c = Db.get();
    try {
      c.setAutoCommit(false);
      String rn = (raceName == null || raceName.trim().isEmpty()) ? null : raceName.trim();
      String tn = (trackName == null || trackName.trim().isEmpty()) ? null : trackName.trim();
      if (raceId != null && raceId.length() > 15) throw new SQLException("raceId length exceeds 15 characters");
      if (rn != null && rn.length() > 30) throw new SQLException("raceName length exceeds 30 characters");
      if (tn != null && tn.length() > 30) throw new SQLException("trackName length exceeds 30 characters");
      // FK pre-check for trackName
      if (tn != null) {
        try (PreparedStatement chk = c.prepareStatement("SELECT 1 FROM Track WHERE trackName = ?")) {
          chk.setString(1, tn);
          try (ResultSet rs = chk.executeQuery()) {
            if (!rs.next()) throw new SQLException("Track '" + tn + "' does not exist (FK violation)");
          }
        }
      }
      try (PreparedStatement pr = c.prepareStatement(
          "INSERT INTO Race(raceId,raceName,trackName,raceDate,raceTime) VALUES(?,?,?,?,?)")) {
        pr.setString(1, raceId);
        if (rn == null) pr.setNull(2, Types.VARCHAR); else pr.setString(2, rn);
        if (tn == null) pr.setNull(3, Types.VARCHAR); else pr.setString(3, tn);
        if (raceDate == null) pr.setNull(4, Types.DATE); else pr.setDate(4, raceDate);
        if (raceTime == null) pr.setNull(5, Types.TIME); else pr.setTime(5, raceTime);
        pr.executeUpdate();
      }
      if (results != null && !results.isEmpty()) {
        try (PreparedStatement pr = c.prepareStatement(
            "INSERT INTO RaceResults(raceId,horseId,results,prize) VALUES(?,?,?,?)")) {
          for (ResultRow r : results) {
            pr.setString(1, raceId);
            pr.setString(2, r.horseId());
            pr.setString(3, r.place());
            pr.setDouble(4, r.prize());
            pr.addBatch();
          }
          pr.executeBatch();
        }
      }
      c.commit();
    } catch (SQLException ex) {
      try { c.rollback(); } catch (SQLException ignored) {}
      throw ex;
    } finally {
      try { c.setAutoCommit(true); } catch (SQLException ignored) {}
      try { c.close(); } catch (SQLException ignored) {}
    }
  }
  // Generate raceId in backend and return it
  public String addRaceWithResults(String raceName, String trackName,
                                   java.sql.Date raceDate, java.sql.Time raceTime,
                                   List<ResultRow> results) throws SQLException {
    String raceId = generateRaceId();
    addRaceWithResults(raceId, raceName, trackName, raceDate, raceTime, results);
    return raceId;
  }
  public com.example.racingfx.model.Race findRace(String raceId) throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(
             "SELECT raceId, raceName, trackName, raceDate, raceTime FROM Race WHERE raceId = ?")) {
      ps.setString(1, raceId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          java.sql.Date d = rs.getDate("raceDate");
          java.sql.Time t = rs.getTime("raceTime");
          return new com.example.racingfx.model.Race(
              rs.getString("raceId"),
              rs.getString("raceName"),
              rs.getString("trackName"),
              d == null ? null : d.toLocalDate(),
              t == null ? null : t.toLocalTime()
          );
        }
        return null;
      }
    }
  }

  public List<String> listTrackNames() throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("SELECT trackName FROM Track ORDER BY trackName");
         ResultSet rs = ps.executeQuery()) {
      List<String> tracks = new ArrayList<>();
      while (rs.next()) {
        tracks.add(rs.getString("trackName"));
      }
      return tracks;
    }
  }

  public boolean ownerExists(String ownerId) throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("SELECT 1 FROM Owner WHERE ownerId = ?")) {
      ps.setString(1, ownerId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  public void deleteOwner(String ownerId) throws SQLException {
    try {
      callDeleteOwnerProcedure(ownerId);
    } catch (SQLException ex) {
      if (isMissingProcedure(ex)) {
        try {
          createDeleteOwnerProcedure();
          callDeleteOwnerProcedure(ownerId);
          return;
        } catch (SQLException inner) {
          manualDeleteOwner(ownerId);
          return;
        }
      }
      if (isForeignKeyViolation(ex)) {
        manualDeleteOwner(ownerId);
        return;
      }
      throw ex;
    }
  }

  private void callDeleteOwnerProcedure(String ownerId) throws SQLException {
    try (Connection c = Db.get();
         CallableStatement cs = c.prepareCall("{CALL delete_owner_and_related(?)}")) {
      cs.setString(1, ownerId);
      cs.execute();
    }
  }

  private boolean isMissingProcedure(SQLException ex) {
    return "42000".equals(ex.getSQLState()) &&
        ex.getMessage() != null &&
        ex.getMessage().toLowerCase().contains("delete_owner_and_related") &&
        ex.getMessage().toLowerCase().contains("does not exist");
  }

  private boolean isForeignKeyViolation(SQLException ex) {
    return "23000".equals(ex.getSQLState());
  }

  private void createDeleteOwnerProcedure() throws SQLException {
    String ddl = """
        CREATE PROCEDURE delete_owner_and_related(IN p_ownerId VARCHAR(15))
        BEGIN
          CREATE TEMPORARY TABLE IF NOT EXISTS tmp_horses (horseId VARCHAR(15) PRIMARY KEY);
          DELETE FROM tmp_horses;
          INSERT INTO tmp_horses (horseId)
            SELECT horseId FROM Owns WHERE ownerId = p_ownerId;

          IF EXISTS (SELECT 1 FROM tmp_horses) THEN
            DELETE FROM RaceResults WHERE horseId IN (SELECT horseId FROM tmp_horses);
            DELETE FROM Owns WHERE ownerId = p_ownerId;
            DELETE FROM Horse
              WHERE horseId IN (SELECT horseId FROM tmp_horses)
              AND NOT EXISTS (
                SELECT 1 FROM Owns o WHERE o.horseId = Horse.horseId
              );
          ELSE
            DELETE FROM Owns WHERE ownerId = p_ownerId;
          END IF;

          DELETE FROM Owner WHERE ownerId = p_ownerId;
          DROP TEMPORARY TABLE IF EXISTS tmp_horses;
        END
        """;
    try (Connection c = Db.get(); Statement st = c.createStatement()) {
      st.execute("DROP PROCEDURE IF EXISTS delete_owner_and_related");
      st.execute(ddl);
    }
  }

  private void manualDeleteOwner(String ownerId) throws SQLException {
    Connection c = Db.get();
    try {
      c.setAutoCommit(false);

      List<String> horseIds = new ArrayList<>();
      try (PreparedStatement ps = c.prepareStatement("SELECT horseId FROM Owns WHERE ownerId = ?")) {
        ps.setString(1, ownerId);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            horseIds.add(rs.getString("horseId"));
          }
        }
      }

      if (!horseIds.isEmpty()) {
        StringJoiner sj = new StringJoiner(",", "(", ")");
        for (int i = 0; i < horseIds.size(); i++) sj.add("?");
        String deleteResultsSql = "DELETE FROM RaceResults WHERE horseId IN " + sj;
        try (PreparedStatement ps = c.prepareStatement(deleteResultsSql)) {
          for (int i = 0; i < horseIds.size(); i++) {
            ps.setString(i + 1, horseIds.get(i));
          }
          ps.executeUpdate();
        }
      }

      try (PreparedStatement ps = c.prepareStatement("DELETE FROM Owns WHERE ownerId = ?")) {
        ps.setString(1, ownerId);
        ps.executeUpdate();
      }

      if (!horseIds.isEmpty()) {
        try (PreparedStatement ps = c.prepareStatement(
            "DELETE FROM Horse WHERE horseId = ? AND NOT EXISTS (SELECT 1 FROM Owns WHERE horseId = ?)")) {
          for (String horseId : horseIds) {
            ps.setString(1, horseId);
            ps.setString(2, horseId);
            ps.addBatch();
          }
          ps.executeBatch();
        }
      }

      try (PreparedStatement ps = c.prepareStatement("DELETE FROM Owner WHERE ownerId = ?")) {
        ps.setString(1, ownerId);
        ps.executeUpdate();
      }

      c.commit();
    } catch (SQLException ex) {
      try { c.rollback(); } catch (SQLException ignored) {}
      throw ex;
    } finally {
      try { c.setAutoCommit(true); } catch (SQLException ignored) {}
      try { c.close(); } catch (SQLException ignored) {}
    }
  }

  public int moveHorse(String horseId, String newStableId) throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("UPDATE Horse SET stableId=? WHERE horseId=?")) {
      ps.setString(1, newStableId);
      ps.setString(2, horseId);
      return ps.executeUpdate();
    }
  }

  public int approveTrainer(String trainerId, String stableId) throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("UPDATE Trainer SET stableId=? WHERE trainerId=?")) {
      ps.setString(1, stableId);
      ps.setString(2, trainerId);
      return ps.executeUpdate();
    }
  }
  private static final String ALPH = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static String generateRaceId() {
    StringBuilder sb = new StringBuilder(15);
    sb.append('R');
    for (int i = 1; i < 15; i++) {
      int idx = (int) (Math.random() * ALPH.length());
      sb.append(ALPH.charAt(idx));
    }
    return sb.toString();
  }
  // helper record for results
  public record ResultRow(String horseId, String place, double prize) {}
}
