package com.example.racingfx.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class WinningTrainerInfo {
  private String trainerFirstName;
  private String trainerLastName;
  private String horseName;
  private String raceId;
  private String raceName;
  private String trackName;
  private LocalDate raceDate;
  private LocalTime raceTime;

  public WinningTrainerInfo() {}

  public WinningTrainerInfo(String trainerFirstName, String trainerLastName, String horseName, String raceId,
                             String raceName, String trackName, LocalDate raceDate, LocalTime raceTime) {
    this.trainerFirstName = trainerFirstName;
    this.trainerLastName = trainerLastName;
    this.horseName = horseName;
    this.raceId = raceId;
    this.raceName = raceName;
    this.trackName = trackName;
    this.raceDate = raceDate;
    this.raceTime = raceTime;
  }

  public String getTrainerFirstName() { return trainerFirstName; }
  public void setTrainerFirstName(String trainerFirstName) { this.trainerFirstName = trainerFirstName; }
  public String getTrainerLastName() { return trainerLastName; }
  public void setTrainerLastName(String trainerLastName) { this.trainerLastName = trainerLastName; }
  public String getHorseName() { return horseName; }
  public void setHorseName(String horseName) { this.horseName = horseName; }
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

