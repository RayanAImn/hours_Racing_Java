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
        "SELECT h.horseName, h.age, t.fname AS tf, t.lname AS tl \n" +
        "FROM Owner o \n" +
        "JOIN Owns ow ON ow.ownerId = o.ownerId \n" +
        "JOIN Horse h ON h.horseId = ow.horseId \n" +
        "LEFT JOIN Trainer t ON t.stableId = h.stableId \n" +
        "WHERE o.lname = ?";
    List<HorseTrainerInfo> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, lastName);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(new HorseTrainerInfo(
              rs.getString("horseName"),
              rs.getObject("age") == null ? null : rs.getInt("age"),
              rs.getString("tf"),
              rs.getString("tl")
          ));
        }
      }
    }
    return out;
  }

  @Override
  public List<WinningTrainerInfo> winningTrainers() throws Exception {
    String sql = "" +
        "SELECT DISTINCT t.fname AS tf, t.lname AS tl, h.horseName, r.raceId, r.raceName, r.trackName, r.raceDate, r.raceTime \n" +
        "FROM Trainer t \n" +
        "JOIN Horse h ON h.stableId = t.stableId \n" +
        "JOIN RaceResults rr ON rr.horseId = h.horseId \n" +
        "JOIN Race r ON r.raceId = rr.raceId \n" +
        "WHERE rr.results = 'first'"; // standardized to 'first'
    List<WinningTrainerInfo> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        LocalDate d = rs.getDate("raceDate") == null ? null : rs.getDate("raceDate").toLocalDate();
        LocalTime t = rs.getTime("raceTime") == null ? null : rs.getTime("raceTime").toLocalTime();
        out.add(new WinningTrainerInfo(
            rs.getString("tf"), rs.getString("tl"), rs.getString("horseName"),
            rs.getString("raceId"), rs.getString("raceName"), rs.getString("trackName"), d, t
        ));
      }
    }
    return out;
  }

  @Override
  public List<TrainerWinnings> trainerWinnings() throws Exception {
    String sql = "" +
        "SELECT t.fname AS tf, t.lname AS tl, COALESCE(SUM(rr.prize), 0) AS totalWinnings \n" +
        "FROM Trainer t \n" +
        "LEFT JOIN Horse h ON h.stableId = t.stableId \n" +
        "LEFT JOIN RaceResults rr ON rr.horseId = h.horseId \n" +
        "GROUP BY t.trainerId, t.fname, t.lname \n" +
        "ORDER BY totalWinnings DESC";
    List<TrainerWinnings> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        out.add(new TrainerWinnings(rs.getString("tf"), rs.getString("tl"), rs.getDouble("totalWinnings")));
      }
    }
    return out;
  }

  @Override
  public List<TrackStats> trackStats() throws Exception {
    String sql = "" +
        "SELECT r.trackName, COUNT(DISTINCT r.raceId) AS raceCount, COALESCE(COUNT(rr.horseId),0) AS totalParticipants \n" +
        "FROM Race r \n" +
        "LEFT JOIN RaceResults rr ON rr.raceId = r.raceId \n" +
        "GROUP BY r.trackName";
    List<TrackStats> out = new ArrayList<>();
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        out.add(new TrackStats(
            rs.getString("trackName"), rs.getInt("raceCount"), rs.getInt("totalParticipants"))
        );
      }
    }
    return out;
  }
}
