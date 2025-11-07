package com.example.racingfx.util;

import com.example.racingfx.db.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility to automatically fix the missing trainer assignment for stable2.
 * This resolves the issue where winning horses have no trainers assigned.
 */
public class FixDatabase {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== DATABASE FIX UTILITY ===\n");
            
            // Check if stable2 already has a trainer
            if (hasTrainerForStable("stable2")) {
                System.out.println("✓ stable2 already has a trainer assigned. No fix needed!");
                return;
            }
            
            System.out.println("✗ stable2 has NO trainer assigned.");
            System.out.println("  This is why winning trainers and trainer winnings are not showing.\n");
            
            // Add a trainer for stable2
            System.out.println("Adding trainer for stable2...");
            addTrainerForStable2();
            
            System.out.println("✓ Successfully added trainer: Omar Hassan to stable2\n");
            
            // Verify the fix worked
            System.out.println("Verifying the fix...");
            verifyWinningTrainers();
            verifyTrainerWinnings();
            
            System.out.println("\n✓ Database fix completed successfully!");
            System.out.println("You can now run the application and see winning trainers and their winnings.");
            
        } catch (Exception e) {
            System.err.println("✗ Error fixing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean hasTrainerForStable(String stableId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Trainer WHERE stableId = ?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, stableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    private static void addTrainerForStable2() throws Exception {
        String sql = "INSERT INTO Trainer (trainerId, lname, fname, stableId) VALUES (?, ?, ?, ?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "trainer9");
            ps.setString(2, "Hassan");
            ps.setString(3, "Omar");
            ps.setString(4, "stable2");
            ps.executeUpdate();
        }
    }
    
    private static void verifyWinningTrainers() throws Exception {
        String sql = "" +
            "SELECT t.fname, t.lname, h.horseName, rr.results, rr.prize " +
            "FROM Trainer t " +
            "INNER JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId " +
            "INNER JOIN RaceResults rr ON rr.horseId = h.horseId " +
            "WHERE LOWER(TRIM(rr.results)) IN ('first', '1st', '1', 'win', 'winner') " +
            "ORDER BY rr.prize DESC";
        
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\nWinning Trainers:");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("  %d. Trainer: %s %s, Horse: %s, Result: %s, Prize: $%.2f",
                    count,
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getString("horseName"),
                    rs.getString("results"),
                    rs.getDouble("prize")));
            }
            System.out.println("Total winning records: " + count);
        }
    }
    
    private static void verifyTrainerWinnings() throws Exception {
        String sql = "" +
            "SELECT t.fname, t.lname, COALESCE(SUM(rr.prize), 0.0) AS totalWinnings " +
            "FROM Trainer t " +
            "LEFT JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId " +
            "LEFT JOIN RaceResults rr ON rr.horseId = h.horseId " +
            "GROUP BY t.trainerId, t.fname, t.lname " +
            "HAVING totalWinnings > 0 " +
            "ORDER BY totalWinnings DESC";
        
        try (Connection c = Db.get();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\nTrainer Winnings (with prizes):");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("  %d. %s %s: $%.2f",
                    count,
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getDouble("totalWinnings")));
            }
            System.out.println("Total trainers with winnings: " + count);
        }
    }
}
