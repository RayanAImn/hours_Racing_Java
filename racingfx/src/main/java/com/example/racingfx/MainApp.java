package com.example.racingfx;

import com.example.racingfx.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
  @Override public void start(Stage stage) {
    MainView view = new MainView();
    stage.setScene(new Scene(view, 960, 640));
    stage.setTitle("RacingFX");
    stage.show();
  }
  public static void main(String[] args){ launch(args); }
}
