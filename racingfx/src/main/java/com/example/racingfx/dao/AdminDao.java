package com.example.racingfx.dao;

import com.example.racingfx.db.Db;
import java.sql.*;
import java.util.List;

public class AdminDao {
  public void addRaceWithResults(String raceId, String raceName, String trackName,
                                 java.sql.Date raceDate, java.sql.Time raceTime,
                                 List<ResultRow> results) throws SQLException {
    try (Connection c = Db.get()) {
      c.setAutoCommit(false);
      try (PreparedStatement pr = c.prepareStatement(
            "INSERT INTO Race(raceId,raceName,trackName,raceDate,raceTime) VALUES(?,?,?,?,?)")) {
        pr.setString(1,raceId); pr.setString(2,raceName); pr.setString(3,trackName);
        pr.setDate(4,raceDate); pr.setTime(5,raceTime); pr.executeUpdate();
      }
      try (PreparedStatement pr = c.prepareStatement(
            "INSERT INTO RaceResults(raceId,horseId,results,prize) VALUES(?,?,?,?)")) {
        for (ResultRow r: results) {
          pr.setString(1,raceId); pr.setString(2,r.horseId()); pr.setString(3,r.place()); pr.setDouble(4,r.prize());
          pr.addBatch();
        }
        pr.executeBatch();
      }
      c.commit();
    }
  }

  public void deleteOwner(String ownerId) throws SQLException {
    try (Connection c = Db.get();
         CallableStatement cs = c.prepareCall("{CALL delete_owner_and_related(?)}")) {
      cs.setString(1, ownerId);
      cs.execute();
    }
  }

  public int moveHorse(String horseId, String newStableId) throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("UPDATE Horse SET stableId=? WHERE horseId=?")) {
      ps.setString(1,newStableId); ps.setString(2,horseId);
      return ps.executeUpdate();
    }
  }

  public int approveTrainer(String trainerId, String stableId) throws SQLException {
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("UPDATE Trainer SET stableId=? WHERE trainerId=?")) {
      ps.setString(1, stableId); ps.setString(2, trainerId);
      return ps.executeUpdate();
    }
  }

  // helper record for results
  public record ResultRow(String horseId, String place, double prize) {}
}
