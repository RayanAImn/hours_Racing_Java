package com.example.racingfx.ui;

import com.example.racingfx.dao.ReportsDao;
import com.example.racingfx.model.HorseTrainerInfo;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuestTab extends Tab {
  public GuestTab() {
    super("Guest");

    VBox root = new VBox(12);
    root.setPadding(new Insets(12));

    // Horses by owner lname
    TitledPane horsesPane = new TitledPane();
    horsesPane.setText("Horses by Owner Last Name");
    BorderPane horsesBox = new BorderPane();
    TextField ownerLname = new TextField(); ownerLname.setPromptText("Owner last name");
    Button search = new Button("Search");
    HBox controls = new HBox(8, ownerLname, search);
    TableView<HorseTrainerInfo> horsesTable = new TableView<>();
    TableColumn<HorseTrainerInfo,String> c1 = new TableColumn<>("Horse"); c1.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getHorseName()));
    TableColumn<HorseTrainerInfo,String> c2 = new TableColumn<>("Age"); c2.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(String.valueOf(v.getValue().getAge())));
    TableColumn<HorseTrainerInfo,String> c3 = new TableColumn<>("Trainer First"); c3.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrainerFirstName()));
    TableColumn<HorseTrainerInfo,String> c4 = new TableColumn<>("Trainer Last"); c4.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrainerLastName()));
    horsesTable.getColumns().addAll(c1,c2,c3,c4);
    horsesTable.setItems(FXCollections.observableArrayList());
    horsesBox.setTop(controls);
    horsesBox.setCenter(horsesTable);
    horsesPane.setContent(horsesBox);

    // Output area for generic JSON results
    TextArea output = new TextArea();
    output.setPromptText("Results JSON will appear here...");
    output.setPrefRowCount(8);

    // Winning trainers
    TitledPane winnersPane = new TitledPane(); winnersPane.setText("Winning Trainers");
    Button loadWinners = new Button("Load");
    VBox winnersBox = new VBox(8, loadWinners, new Label("See results area below"));
    winnersPane.setContent(winnersBox);

    // Trainer winnings
    TitledPane twPane = new TitledPane(); twPane.setText("Trainer Winnings");
    Button loadTW = new Button("Load");
    VBox twBox = new VBox(8, loadTW, new Label("See results area below"));
    twPane.setContent(twBox);

    // Track stats
    TitledPane tsPane = new TitledPane(); tsPane.setText("Track Stats");
    Button loadTS = new Button("Load");
    VBox tsBox = new VBox(8, loadTS, new Label("See results area below"));
    tsPane.setContent(tsBox);

    // Wire actions
    ReportsDao reports = new ReportsDao();

    search.setOnAction(e -> {
      try {
        List<Map<String,Object>> maps = reports.horsesByOwnerLastName(ownerLname.getText().trim());
        List<HorseTrainerInfo> list = new ArrayList<>();
        for (Map<String,Object> m : maps) {
          String horse = (String)m.getOrDefault("horseName", "");
          Integer age = m.get("age") == null ? null : ((Number)m.get("age")).intValue();
          String tf = (String)m.getOrDefault("trainerF", "");
          String tl = (String)m.getOrDefault("trainerL", "");
          list.add(new HorseTrainerInfo(horse, age, tf, tl));
        }
        horsesTable.setItems(FXCollections.observableArrayList(list));
        output.setText(toJson(maps));
      } catch (SQLException ex) {
        output.setText("Error: " + ex.getMessage());
      }
    });

    loadWinners.setOnAction(e -> {
      try { output.setText(toJson(reports.winningTrainers())); }
      catch (SQLException ex) { output.setText("Error: " + ex.getMessage()); }
    });

    loadTW.setOnAction(e -> {
      try { output.setText(toJson(reports.trainerWinnings())); }
      catch (SQLException ex) { output.setText("Error: " + ex.getMessage()); }
    });

    loadTS.setOnAction(e -> {
      try { output.setText(toJson(reports.trackStats())); }
      catch (SQLException ex) { output.setText("Error: " + ex.getMessage()); }
    });

    root.getChildren().addAll(horsesPane, winnersPane, twPane, tsPane, new Label("Results:"), output);
    setContent(new ScrollPane(root));
  }

  private static String toJson(List<Map<String,Object>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i=0;i<rows.size();i++) {
      if (i>0) sb.append(',');
      Map<String,Object> m = rows.get(i);
      sb.append("{");
      int j=0; for (Map.Entry<String,Object> e : m.entrySet()) {
        if (j++>0) sb.append(',');
        sb.append('"').append(e.getKey()).append('"').append(":");
        Object v = e.getValue();
        if (v==null) sb.append("null");
        else if (v instanceof Number || v instanceof Boolean) sb.append(v.toString());
        else sb.append('"').append(String.valueOf(v).replace("\"","\\\"")).append('"');
      }
      sb.append("}");
    }
    sb.append("]");
    return sb.toString();
  }
}
