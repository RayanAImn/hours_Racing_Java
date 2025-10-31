package com.example.racingfx.dao;

import com.example.racingfx.model.HorseTrainerInfo;
import com.example.racingfx.model.TrainerWinnings;
import com.example.racingfx.model.TrackStats;
import com.example.racingfx.model.WinningTrainerInfo;

import java.util.List;

public interface GuestDao {
  List<HorseTrainerInfo> horsesByOwnerLastName(String lastName) throws Exception;
  List<WinningTrainerInfo> winningTrainers() throws Exception;
  List<TrainerWinnings> trainerWinnings() throws Exception;
  List<TrackStats> trackStats() throws Exception;
}

