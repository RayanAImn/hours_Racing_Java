package com.example.racingfx.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Race {
  private String raceId;
  private String raceName;
  private String trackName;
  private LocalDate raceDate;
  private LocalTime raceTime;

  public Race() {}

  public Race(String raceId, String raceName, String trackName, LocalDate raceDate, LocalTime raceTime) {
    this.raceId = raceId;
    this.raceName = raceName;
    this.trackName = trackName;
    this.raceDate = raceDate;
    this.raceTime = raceTime;
  }

  public String getRaceId() { return raceId; }
  public void setRaceId(String raceId) { this.raceId = raceId; }
  public String getRaceName() { return raceName; }
  public void setRaceName(String raceName) { this.raceName = raceName; }
  public String getTrackName() { return trackName; }
  public void setTrackName(String trackName) { this.trackName = trackName; }
  public LocalDate getRaceDate() { return raceDate; }
  public void setRaceDate(LocalDate raceDate) { this.raceDate = raceDate; }
  public LocalTime getRaceTime() { return raceTime; }
  public void setRaceTime(LocalTime raceTime) { this.raceTime = raceTime; }
}

