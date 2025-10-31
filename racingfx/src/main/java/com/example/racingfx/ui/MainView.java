package com.example.racingfx.ui;

import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane {
  public MainView() {
    TabPane tabs = new TabPane();
    tabs.getTabs().addAll(new AdminTab(), new GuestTab());
    setCenter(tabs);
  }
}

