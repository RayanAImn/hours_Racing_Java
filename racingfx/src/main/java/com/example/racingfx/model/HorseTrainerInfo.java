package com.example.racingfx.model;

public class HorseTrainerInfo {
  private String horseName;
  private Integer age;
  private String trainerFirstName;
  private String trainerLastName;

  public HorseTrainerInfo() {}

  public HorseTrainerInfo(String horseName, Integer age, String trainerFirstName, String trainerLastName) {
    this.horseName = horseName;
    this.age = age;
    this.trainerFirstName = trainerFirstName;
    this.trainerLastName = trainerLastName;
  }

  public String getHorseName() { return horseName; }
  public void setHorseName(String horseName) { this.horseName = horseName; }
  public Integer getAge() { return age; }
  public void setAge(Integer age) { this.age = age; }
  public String getTrainerFirstName() { return trainerFirstName; }
  public void setTrainerFirstName(String trainerFirstName) { this.trainerFirstName = trainerFirstName; }
  public String getTrainerLastName() { return trainerLastName; }
  public void setTrainerLastName(String trainerLastName) { this.trainerLastName = trainerLastName; }
}

