package com.example.racingfx.model;

public class TrainerWinnings {
  private String trainerFirstName;
  private String trainerLastName;
  private double totalWinnings;

  public TrainerWinnings() {}

  public TrainerWinnings(String trainerFirstName, String trainerLastName, double totalWinnings) {
    this.trainerFirstName = trainerFirstName;
    this.trainerLastName = trainerLastName;
    this.totalWinnings = totalWinnings;
  }

  public String getTrainerFirstName() { return trainerFirstName; }
  public void setTrainerFirstName(String trainerFirstName) { this.trainerFirstName = trainerFirstName; }
  public String getTrainerLastName() { return trainerLastName; }
  public void setTrainerLastName(String trainerLastName) { this.trainerLastName = trainerLastName; }
  public double getTotalWinnings() { return totalWinnings; }
  public void setTotalWinnings(double totalWinnings) { this.totalWinnings = totalWinnings; }
}

