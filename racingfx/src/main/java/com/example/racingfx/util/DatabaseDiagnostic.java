package com.example.racingfx.util;

import com.example.racingfx.db.Db;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Diagnostic utility to check database contents and help debug query issues.
 */
public class DatabaseDiagnostic {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== DATABASE DIAGNOSTIC TOOL ===\n");
            
            checkTrainers();
            checkHorses();
            checkRaceResults();
            checkTrainerHorseRelationship();
            checkWinningResults();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void checkTrainers() throws Exception {
        System.out.println("--- TRAINERS ---");
        String sql = "SELECT trainerId, fname, lname, stableId FROM Trainer";
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("Trainer %d: ID=%s, Name=%s %s, StableID=%s",
                    count,
                    rs.getString("trainerId"),
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getString("stableId")));
            }
            System.out.println("Total trainers: " + count + "\n");
        }
    }
    
    private static void checkHorses() throws Exception {
        System.out.println("--- HORSES ---");
        String sql = "SELECT horseId, horseName, stableId FROM Horse LIMIT 10";
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("Horse %d: ID=%s, Name=%s, StableID=%s",
                    count,
                    rs.getString("horseId"),
                    rs.getString("horseName"),
                    rs.getString("stableId")));
            }
            System.out.println("Total horses shown: " + count + "\n");
        }
    }
    
    private static void checkRaceResults() throws Exception {
        System.out.println("--- RACE RESULTS ---");
        String sql = "SELECT rr.raceId, rr.horseId, rr.results, rr.prize, h.horseName " +
                     "FROM RaceResults rr " +
                     "INNER JOIN Horse h ON h.horseId = rr.horseId " +
                     "LIMIT 20";
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("Result %d: RaceID=%s, Horse=%s, Result='%s', Prize=$%.2f",
                    count,
                    rs.getString("raceId"),
                    rs.getString("horseName"),
                    rs.getString("results"),
                    rs.getDouble("prize")));
            }
            System.out.println("Total results shown: " + count + "\n");
        }
    }
    
    private static void checkTrainerHorseRelationship() throws Exception {
        System.out.println("--- TRAINER-HORSE RELATIONSHIP ---");
        String sql = "SELECT t.trainerId, t.fname, t.lname, t.stableId AS trainerStable, " +
                     "h.horseId, h.horseName, h.stableId AS horseStable " +
                     "FROM Trainer t " +
                     "LEFT JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId " +
                     "LIMIT 10";
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("Match %d: Trainer=%s %s (Stable=%s), Horse=%s (Stable=%s)",
                    count,
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getString("trainerStable"),
                    rs.getString("horseName"),
                    rs.getString("horseStable")));
            }
            System.out.println("Total matches shown: " + count + "\n");
        }
    }
    
    private static void checkWinningResults() throws Exception {
        System.out.println("--- WINNING RESULTS (First Place) ---");
        String sql = "SELECT rr.results, COUNT(*) as count, rr.horseId, h.horseName " +
                     "FROM RaceResults rr " +
                     "INNER JOIN Horse h ON h.horseId = rr.horseId " +
                     "GROUP BY rr.results, rr.horseId, h.horseName " +
                     "ORDER BY rr.results";
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Unique result values in database:");
            while (rs.next()) {
                System.out.println(String.format("  Result='%s', Horse=%s, Count=%d",
                    rs.getString("results"),
                    rs.getString("horseName"),
                    rs.getInt("count")));
            }
            System.out.println();
        }
        
        // Check for first place specifically
        String sql2 = "SELECT COUNT(*) as count FROM RaceResults WHERE " +
                      "LOWER(TRIM(results)) = 'first' OR " +
                      "LOWER(TRIM(results)) = '1st' OR " +
                      "TRIM(results) = '1' OR " +
                      "LOWER(TRIM(results)) = 'win' OR " +
                      "LOWER(TRIM(results)) = 'winner'";
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql2)) {
            if (rs.next()) {
                System.out.println("Total first-place results: " + rs.getInt("count") + "\n");
            }
        }
    }
}
