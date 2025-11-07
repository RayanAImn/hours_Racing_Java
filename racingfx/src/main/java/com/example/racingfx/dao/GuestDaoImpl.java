package com.example.racingfx.dao;

import com.example.racingfx.db.Db;
import com.example.racingfx.model.HorseTrainerInfo;
import com.example.racingfx.model.TrainerWinnings;
import com.example.racingfx.model.TrackStats;
import com.example.racingfx.model.WinningTrainerInfo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GuestDaoImpl implements GuestDao {
  @Override
  public List<HorseTrainerInfo> horsesByOwnerLastName(String lastName) throws Exception {
    String sql = "" +
        "SELECT o.ownerId, o.fname, o.lname, h.horseName, h.age, t.fname AS tf, t.lname AS tl \n" +
        "FROM Owner o \n" +
        "INNER JOIN Owns ow ON ow.ownerId = o.ownerId \n" +
        "INNER JOIN Horse h ON h.horseId = ow.horseId \n" +
        "LEFT JOIN Trainer t ON CAST(t.stableId AS CHAR(30)) = h.stableId \n" +  // Cast to handle type mismatch
        "WHERE LOWER(TRIM(o.lname)) LIKE ?";

    List<HorseTrainerInfo> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
        String searchPattern = "%" + lastName.trim().toLowerCase() + "%";
        ps.setString(1, searchPattern);
        System.out.println("DEBUG: Executing query with pattern='" + searchPattern + "'");
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println("DEBUG: Found owner: " +
                    rs.getString("ownerId") + ", " +
                    rs.getString("fname") + " " +
                    rs.getString("lname") + ", horse: " +
                    rs.getString("horseName"));

                out.add(new HorseTrainerInfo(
                    rs.getString("horseName"),
                    rs.getObject("age") == null ? null : rs.getInt("age"),
                    rs.getString("tf"),
                    rs.getString("tl")
                ));
            }
            if (out.isEmpty()) {
                System.out.println("DEBUG: No results found in database");
            }
        }
    } catch (SQLException ex) {
        System.err.println("SQL Error: " + ex.getMessage());
        System.err.println("SQL State: " + ex.getSQLState());
        throw ex;
    }
    return out;
  }

  @Override
  public List<WinningTrainerInfo> winningTrainers() throws Exception {
    String sql = "" +
        "SELECT t.fname AS tf, t.lname AS tl, h.horseName, " +
        "r.raceId, r.raceName, r.trackName, r.raceDate, r.raceTime, rr.results \n" +
        "FROM Trainer t \n" +
        "INNER JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId \n" +
        "INNER JOIN RaceResults rr ON rr.horseId = h.horseId \n" +
        "INNER JOIN Race r ON r.raceId = rr.raceId \n" +
        "WHERE t.stableId IS NOT NULL \n" +
        "  AND (LOWER(TRIM(rr.results)) = 'first' \n" +
        "       OR LOWER(TRIM(rr.results)) = '1st' \n" +
        "       OR TRIM(rr.results) = '1' \n" +
        "       OR LOWER(TRIM(rr.results)) = 'win' \n" +
        "       OR LOWER(TRIM(rr.results)) = 'winner') \n" +
        "ORDER BY r.raceDate DESC, r.raceTime DESC";
    
    List<WinningTrainerInfo> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
        System.out.println("DEBUG: Executing winning trainers query");
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate d = rs.getDate("raceDate") == null ? null : rs.getDate("raceDate").toLocalDate();
                LocalTime t = rs.getTime("raceTime") == null ? null : rs.getTime("raceTime").toLocalTime();
                WinningTrainerInfo winner = new WinningTrainerInfo(
                    rs.getString("tf"),
                    rs.getString("tl"),
                    rs.getString("horseName"),
                    rs.getString("raceId"),
                    rs.getString("raceName"),
                    rs.getString("trackName"),
                    d, t
                );
                out.add(winner);
                System.out.println("DEBUG: Found winner - Trainer: " + 
                    rs.getString("tf") + " " + rs.getString("tl") + 
                    ", Horse: " + rs.getString("horseName") + 
                    ", Result: " + rs.getString("results"));
            }
        }
        System.out.println("DEBUG: Total winning trainers found: " + out.size());
    }
    return out;
  }

  @Override
  public List<TrainerWinnings> trainerWinnings() throws Exception {
    String sql = "" +
        "SELECT t.fname AS tf, t.lname AS tl, " +
        "COALESCE(SUM(rr.prize), 0.0) AS totalWinnings \n" +
        "FROM Trainer t \n" +
        "LEFT JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId \n" +
        "LEFT JOIN RaceResults rr ON rr.horseId = h.horseId \n" +
        "GROUP BY t.trainerId, t.fname, t.lname \n" +
        "ORDER BY totalWinnings DESC";
    
    List<TrainerWinnings> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
        System.out.println("DEBUG: Executing trainer winnings query");
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double winnings = rs.getDouble("totalWinnings");
                String fname = rs.getString("tf");
                String lname = rs.getString("tl");
                TrainerWinnings tw = new TrainerWinnings(fname, lname, winnings);
                out.add(tw);
                System.out.println("DEBUG: Trainer: " + fname + " " + lname + 
                    ", Winnings: $" + String.format("%.2f", winnings));
            }
        }
        System.out.println("DEBUG: Total trainers found: " + out.size());
    }
    return out;
  }

  @Override
  public List<TrackStats> trackStats() throws Exception {
    String sql = "" +
        "SELECT r.trackName, " +
        "COUNT(DISTINCT r.raceId) AS raceCount, " +
        "COUNT(DISTINCT rr.horseId) AS totalParticipants \n" +  // Changed to COUNT DISTINCT
        "FROM Race r \n" +
        "LEFT JOIN RaceResults rr ON rr.raceId = r.raceId \n" +
        "GROUP BY r.trackName \n" +
        "ORDER BY r.trackName";
    
    List<TrackStats> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        out.add(new TrackStats(
            rs.getString("trackName"), 
            rs.getInt("raceCount"), 
            rs.getInt("totalParticipants"))
        );
      }
    }
    return out;
  }
}
