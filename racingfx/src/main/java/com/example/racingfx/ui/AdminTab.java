package com.example.racingfx.ui;

import com.example.racingfx.dao.AdminDao;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminTab extends Tab {
  public AdminTab() {
    super("Admin");

    VBox root = new VBox(12);
    root.setPadding(new Insets(12));

    // Add Race with Results
    TitledPane addRacePane = new TitledPane();
    addRacePane.setText("Add Race with Results");
    GridPane addRaceGrid = new GridPane();
    addRaceGrid.setHgap(8); addRaceGrid.setVgap(8);
    TextField raceId = new TextField(); raceId.setPromptText("raceId");
    TextField raceName = new TextField(); raceName.setPromptText("raceName");
    TextField trackName = new TextField(); trackName.setPromptText("trackName");
    DatePicker raceDate = new DatePicker();
    TextField raceTime = new TextField(); raceTime.setPromptText("HH:MM:SS");
    TextArea resultsJson = new TextArea("[{\"horseId\":\"horse3\",\"place\":\"first\",\"prize\":12345}]");
    resultsJson.setPrefRowCount(4);
    Button addRaceBtn = new Button("Add Race");
    addRaceGrid.addRow(0, new Label("Race ID"), raceId);
    addRaceGrid.addRow(1, new Label("Name"), raceName);
    addRaceGrid.addRow(2, new Label("Track"), trackName);
    addRaceGrid.addRow(3, new Label("Date"), raceDate);
    addRaceGrid.addRow(4, new Label("Time"), raceTime);
    addRaceGrid.addRow(5, new Label("Results JSON"), resultsJson);
    addRaceGrid.add(addRaceBtn, 1, 6);
    addRacePane.setContent(addRaceGrid);

    // Delete Owner
    TitledPane delOwnerPane = new TitledPane();
    delOwnerPane.setText("Delete Owner");
    GridPane delGrid = new GridPane(); delGrid.setHgap(8); delGrid.setVgap(8);
    TextField ownerId = new TextField(); ownerId.setPromptText("ownerId");
    Button delBtn = new Button("Delete");
    delGrid.addRow(0, new Label("Owner ID"), ownerId);
    delGrid.add(delBtn, 1, 1);
    delOwnerPane.setContent(delGrid);

    // Move Horse
    TitledPane moveHorsePane = new TitledPane();
    moveHorsePane.setText("Move Horse To Stable");
    GridPane moveGrid = new GridPane(); moveGrid.setHgap(8); moveGrid.setVgap(8);
    TextField horseId = new TextField(); horseId.setPromptText("horseId");
    TextField newStableId = new TextField(); newStableId.setPromptText("new stableId");
    Button moveBtn = new Button("Move");
    moveGrid.addRow(0, new Label("Horse ID"), horseId);
    moveGrid.addRow(1, new Label("New Stable"), newStableId);
    moveGrid.add(moveBtn, 1, 2);
    moveHorsePane.setContent(moveGrid);

    // Approve Trainer
    TitledPane approvePane = new TitledPane();
    approvePane.setText("Approve Trainer To Stable");
    GridPane apprGrid = new GridPane(); apprGrid.setHgap(8); apprGrid.setVgap(8);
    TextField trainerId = new TextField(); trainerId.setPromptText("trainerId");
    TextField stableId = new TextField(); stableId.setPromptText("stableId");
    Button approveBtn = new Button("Approve");
    apprGrid.addRow(0, new Label("Trainer ID"), trainerId);
    apprGrid.addRow(1, new Label("Stable ID"), stableId);
    apprGrid.add(approveBtn, 1, 2);
    approvePane.setContent(apprGrid);

    // Status area
    TextArea logs = new TextArea(); logs.setPromptText("Status / logs..."); logs.setPrefRowCount(6);

    // Wire actions
    addRaceBtn.setOnAction(e -> {
      try {
        LocalDate d = raceDate.getValue();
        Date sqlDate = (d == null) ? null : Date.valueOf(d);
        Time sqlTime = (raceTime.getText() == null || raceTime.getText().isBlank()) ? null : Time.valueOf(raceTime.getText().trim());
        List<AdminDao.ResultRow> rows = parseResults(resultsJson.getText());
        new AdminDao().addRaceWithResults(
            raceId.getText().trim(), raceName.getText().trim(), trackName.getText().trim(), sqlDate, sqlTime, rows);
        logs.appendText("Added race " + raceId.getText().trim() + " with " + rows.size() + " results\n");
      } catch (Exception ex) {
        logs.appendText("Add race error: " + ex.getMessage() + "\n");
      }
    });

    delBtn.setOnAction(e -> {
      try {
        new AdminDao().deleteOwner(ownerId.getText().trim());
        logs.appendText("Deleted owner " + ownerId.getText().trim() + " (via procedure)\n");
      } catch (Exception ex) {
        logs.appendText("Delete owner error: " + ex.getMessage() + "\n");
      }
    });

    moveBtn.setOnAction(e -> {
      try {
        int n = new AdminDao().moveHorse(horseId.getText().trim(), newStableId.getText().trim());
        logs.appendText("Moved horse rows updated: " + n + "\n");
      } catch (Exception ex) {
        logs.appendText("Move horse error: " + ex.getMessage() + "\n");
      }
    });

    approveBtn.setOnAction(e -> {
      try {
        int n = new AdminDao().approveTrainer(trainerId.getText().trim(), stableId.getText().trim());
        logs.appendText("Approved trainer rows updated: " + n + "\n");
      } catch (Exception ex) {
        logs.appendText("Approve trainer error: " + ex.getMessage() + "\n");
      }
    });

    root.getChildren().addAll(addRacePane, delOwnerPane, moveHorsePane, approvePane, logs);
    setContent(new ScrollPane(root));
  }

  // Simple JSON parser for the expected array of objects
  private static List<AdminDao.ResultRow> parseResults(String json) {
    List<AdminDao.ResultRow> out = new ArrayList<>();
    if (json == null || json.isBlank()) return out;
    String s = json.trim();
    if (!s.startsWith("[") || !s.endsWith("]")) return out;
    s = s.substring(1, s.length()-1).trim();
    int depth = 0; StringBuilder cur = new StringBuilder(); List<String> objs = new ArrayList<>();
    for (int i=0;i<s.length();i++) {
      char ch = s.charAt(i);
      if (ch=='{') depth++;
      if (ch=='}') depth--;
      cur.append(ch);
      if (depth==0 && ch=='}') { objs.add(cur.toString()); cur.setLength(0); }
    }
    for (String obj : objs) {
      String body = obj.trim();
      if (body.startsWith("{")) body = body.substring(1);
      if (body.endsWith("}")) body = body.substring(0, body.length()-1);
      String horseId=null, place=null; double prize=0.0;
      String[] parts = body.split(",");
      for (String part : parts) {
        String[] kv = part.split(":",2);
        if (kv.length<2) continue;
        String key = strip(kv[0]);
        String val = strip(kv[1]);
        if ("horseId".equals(key)) horseId = val;
        else if ("place".equals(key)) place = val;
        else if ("prize".equals(key)) try { prize = Double.parseDouble(val); } catch (Exception ignored) {}
      }
      if (horseId != null && place != null) {
        out.add(new AdminDao.ResultRow(horseId, place, prize));
      }
    }
    return out;
  }

  private static String strip(String s) {
    String t = s.trim();
    if (t.startsWith("\"") && t.endsWith("\"")) t = t.substring(1, t.length()-1);
    return t;
  }
}
