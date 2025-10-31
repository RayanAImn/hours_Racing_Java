package com.example.racingfx.model;

public class RaceResult {
  private String raceId;
  private String horseId;
  private String results;
  private double prize;

  public RaceResult() {}

  public RaceResult(String raceId, String horseId, String results, double prize) {
    this.raceId = raceId;
    this.horseId = horseId;
    this.results = results;
    this.prize = prize;
  }

  public String getRaceId() { return raceId; }
  public void setRaceId(String raceId) { this.raceId = raceId; }
  public String getHorseId() { return horseId; }
  public void setHorseId(String horseId) { this.horseId = horseId; }
  public String getResults() { return results; }
  public void setResults(String results) { this.results = results; }
  public double getPrize() { return prize; }
  public void setPrize(double prize) { this.prize = prize; }
}

