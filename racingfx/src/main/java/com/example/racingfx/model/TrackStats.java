package com.example.racingfx.model;

public class TrackStats {
  private String trackName;
  private int raceCount;
  private int totalParticipants;

  public TrackStats() {}

  public TrackStats(String trackName, int raceCount, int totalParticipants) {
    this.trackName = trackName;
    this.raceCount = raceCount;
    this.totalParticipants = totalParticipants;
  }

  public String getTrackName() { return trackName; }
  public void setTrackName(String trackName) { this.trackName = trackName; }
  public int getRaceCount() { return raceCount; }
  public void setRaceCount(int raceCount) { this.raceCount = raceCount; }
  public int getTotalParticipants() { return totalParticipants; }
  public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }
}

