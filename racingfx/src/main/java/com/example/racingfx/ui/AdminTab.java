package com.example.racingfx.ui;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import com.example.racingfx.dao.AdminDao;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminTab extends Tab {
  public AdminTab() {
    super("Admin");

    HBox columns = new HBox(12);
    columns.setPadding(new Insets(12));
    VBox formColumn = new VBox(12);
    formColumn.setFillWidth(true);
    VBox outputColumn = new VBox(12);
    outputColumn.setFillWidth(true);
    HBox.setHgrow(formColumn, Priority.ALWAYS);
    HBox.setHgrow(outputColumn, Priority.ALWAYS);

    // Add Race (results optional)
    TitledPane addRacePane = new TitledPane();
    addRacePane.setText("Add Race");
    GridPane addRaceGrid = new GridPane();
    addRaceGrid.setHgap(8); addRaceGrid.setVgap(8);
    TextField raceName = new TextField(); raceName.setPromptText("raceName");
    ComboBox<String> trackName = new ComboBox<>();
    trackName.setPromptText("Select track");
    trackName.setMaxWidth(Double.MAX_VALUE);
    DatePicker raceDate = new DatePicker();
    raceDate.setEditable(false);
    TextField raceTime = new TextField(); raceTime.setPromptText("HH:MM:SS");
    Button addRaceBtn = new Button("Add Race");
    addRaceGrid.addRow(0, new Label("Name"), raceName);
    addRaceGrid.addRow(1, new Label("Track"), trackName);
    addRaceGrid.addRow(2, new Label("Date"), raceDate);
    addRaceGrid.addRow(3, new Label("Time"), raceTime);
    addRaceGrid.add(addRaceBtn, 1, 4);
    addRacePane.setContent(addRaceGrid);
    addRacePane.setMaxWidth(Double.MAX_VALUE);

    // Delete Owner
    TitledPane delOwnerPane = new TitledPane();
    delOwnerPane.setText("Delete Owner");
    GridPane delGrid = new GridPane(); delGrid.setHgap(8); delGrid.setVgap(8);
    TextField ownerId = new TextField(); ownerId.setPromptText("ownerId");
    Button delBtn = new Button("Delete");
    delGrid.addRow(0, new Label("Owner ID"), ownerId);
    delGrid.add(delBtn, 1, 1);
    delOwnerPane.setContent(delGrid);
    delOwnerPane.setMaxWidth(Double.MAX_VALUE);

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
    moveHorsePane.setMaxWidth(Double.MAX_VALUE);

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
    approvePane.setMaxWidth(Double.MAX_VALUE);

    // Recently added / verification table
    TitledPane recentPane = new TitledPane(); recentPane.setText("Recently Added Race");
    TableView<com.example.racingfx.model.Race> recentTable = new TableView<>();
    recentTable.getColumns().addAll(
        makeCol("Race ID", com.example.racingfx.model.Race::getRaceId),
        makeCol("Name", com.example.racingfx.model.Race::getRaceName),
        makeCol("Track", com.example.racingfx.model.Race::getTrackName),
        makeCol("Date", r-> r.getRaceDate()==null?"":r.getRaceDate().toString()),
        makeCol("Time", r-> r.getRaceTime()==null?"":r.getRaceTime().toString())
    );
    Button refreshRecent = new Button("Refresh");
    VBox recentBox = new VBox(8, refreshRecent, recentTable);
    recentPane.setContent(recentBox);
    recentPane.setMaxWidth(Double.MAX_VALUE);

    // Status area
    TextArea logs = new TextArea();
    logs.setPromptText("Status / logs...");
    logs.setPrefRowCount(6);
    logs.setWrapText(true);
    logs.setMaxWidth(Double.MAX_VALUE);

    loadTrackOptions(trackName, logs);

    // Wire actions
    addRaceBtn.setOnAction(e -> {
      try {
        LocalDate d = raceDate.getValue();
        Date sqlDate = (d == null) ? null : Date.valueOf(d);
        AdminDao dao = new AdminDao();
        String raceNameVal = raceName.getText() == null ? "" : raceName.getText().trim();
        String trackVal = trackName.getValue();
        if (raceNameVal.isBlank()) {
          logs.appendText("Race name is required.\n");
          return;
        }
        if (trackVal == null || trackVal.isBlank()) {
          logs.appendText("Select a track before adding the race.\n");
          return;
        }
        if (sqlDate == null) {
          logs.appendText("Select a race date.\n");
          return;
        }
        Time sqlTime = parseTimeOrNull(raceTime.getText(), logs);
        if (raceTime.getText() != null && !raceTime.getText().isBlank() && sqlTime == null) {
          return;
        }
        trackVal = trackVal.trim();
        String newRaceId = dao.addRace(
            raceNameVal, trackVal, sqlDate, sqlTime);
        logs.appendText("Added race " + newRaceId + "\n");
        // Load and show the inserted row
        com.example.racingfx.model.Race rec = dao.findRace(newRaceId);
        if (rec != null) {
          recentTable.setItems(javafx.collections.FXCollections.observableArrayList(java.util.List.of(rec)));
        }
      } catch (Exception ex) {
        if (ex instanceof java.sql.SQLException sqle) {
          logs.appendText("Add race error: " + sqle.getMessage() +
              " | SQLState=" + sqle.getSQLState() + " Code=" + sqle.getErrorCode() + "\n");
        } else {
          logs.appendText("Add race error: " + ex.getMessage() + "\n");
        }
      }
    });

    delBtn.setOnAction(e -> {
      String id = ownerId.getText() == null ? "" : ownerId.getText().trim();
      if (id.isEmpty()) {
        logs.appendText("Owner ID is required.\n");
        return;
      }
      try {
        AdminDao dao = new AdminDao();
        if (!dao.ownerExists(id)) {
          logs.appendText("No owner related to this ID.\n");
          return;
        }
        dao.deleteOwner(id);
        logs.appendText("Deleted owner " + id + " (via procedure)\n");
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

    refreshRecent.setOnAction(e -> logs.appendText("Use Add Race to populate the recent table.\n"));

    formColumn.getChildren().addAll(addRacePane, delOwnerPane, moveHorsePane, approvePane);
    outputColumn.getChildren().addAll(recentPane, logs);
    VBox.setVgrow(recentPane, Priority.ALWAYS);
    columns.getChildren().addAll(formColumn, outputColumn);

    ScrollPane scroller = new ScrollPane(columns);
    scroller.setFitToWidth(true);
    scroller.setFitToHeight(true);
    setContent(scroller);
  }

  private static <T> TableColumn<T,String> makeCol(String name, java.util.function.Function<T,String> fn) {
    TableColumn<T,String> c = new TableColumn<>(name);
    c.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(fn.apply(v.getValue())));
    return c;
  }

  private Time parseTimeOrNull(String text, TextArea logs) {
    if (text == null || text.isBlank()) return null;
    String trimmed = text.trim();
    String[] parts = trimmed.split(":");
    if (parts.length != 3) {
      logs.appendText("Invalid time. Use HH:MM:SS format.\n");
      return null;
    }
    try {
      int hh = Integer.parseInt(parts[0]);
      int mm = Integer.parseInt(parts[1]);
      int ss = Integer.parseInt(parts[2]);
      if (hh < 0 || hh > 23 || mm < 0 || mm > 59 || ss < 0 || ss > 59) {
        logs.appendText("Invalid time. Hours 0-23, minutes/seconds 0-59.\n");
        return null;
      }
      return Time.valueOf(String.format("%02d:%02d:%02d", hh, mm, ss));
    } catch (NumberFormatException ex) {
      logs.appendText("Invalid time. Use HH:MM:SS format.\n");
      return null;
    }
  }

  private void loadTrackOptions(ComboBox<String> trackField, TextArea logs) {
    try {
      java.util.List<String> tracks = new AdminDao().listTrackNames();
      trackField.setItems(javafx.collections.FXCollections.observableArrayList(tracks));
      trackField.setDisable(tracks.isEmpty());
      trackField.getSelectionModel().clearSelection();
      if (tracks.isEmpty()) {
        logs.appendText("No tracks found. Add tracks before creating races.\n");
      }
    } catch (Exception ex) {
      trackField.setDisable(true);
      logs.appendText("Load tracks error: " + ex.getMessage() + "\n");
    }
  }

  // Removed JSON input for results to simplify race creation
}