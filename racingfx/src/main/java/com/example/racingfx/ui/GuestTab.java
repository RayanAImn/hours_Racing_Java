package com.example.racingfx.ui;

import com.example.racingfx.dao.GuestDao;
import com.example.racingfx.dao.GuestDaoImpl;
import com.example.racingfx.model.HorseTrainerInfo;
import com.example.racingfx.model.TrackStats;
import com.example.racingfx.model.TrainerWinnings;
import com.example.racingfx.model.WinningTrainerInfo;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

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
    TableColumn<HorseTrainerInfo,String> c2 = new TableColumn<>("Age"); c2.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getAge()==null?"":String.valueOf(v.getValue().getAge())));
    TableColumn<HorseTrainerInfo,String> c3 = new TableColumn<>("Trainer First"); c3.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrainerFirstName()));
    TableColumn<HorseTrainerInfo,String> c4 = new TableColumn<>("Trainer Last"); c4.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrainerLastName()));
    horsesTable.getColumns().addAll(c1,c2,c3,c4);
    horsesTable.setItems(FXCollections.observableArrayList());
    horsesBox.setTop(controls);
    horsesBox.setCenter(horsesTable);
    horsesPane.setContent(horsesBox);

    // Winning trainers table
    TitledPane winnersPane = new TitledPane(); winnersPane.setText("Winning Trainers");
    TableView<WinningTrainerInfo> winnersTable = new TableView<>();
    winnersTable.getColumns().addAll(
        makeCol("Trainer First", v -> v.getTrainerFirstName()),
        makeCol("Trainer Last", v -> v.getTrainerLastName()),
        makeCol("Horse", v -> v.getHorseName()),
        makeCol("Race", v -> v.getRaceName()),
        makeCol("Track", v -> v.getTrackName()),
        makeCol("Date", v -> v.getRaceDate()==null?"":v.getRaceDate().toString()),
        makeCol("Time", v -> v.getRaceTime()==null?"":v.getRaceTime().toString())
    );
    Button loadWinners = new Button("Load");
    VBox winnersBox = new VBox(8, loadWinners, winnersTable); winnersPane.setContent(winnersBox);

    // Trainer winnings table
    TitledPane twPane = new TitledPane(); twPane.setText("Trainer Winnings");
    TableView<TrainerWinnings> twTable = new TableView<>();
    TableColumn<TrainerWinnings,String> tw1 = new TableColumn<>("Trainer First"); tw1.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrainerFirstName()));
    TableColumn<TrainerWinnings,String> tw2 = new TableColumn<>("Trainer Last"); tw2.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrainerLastName()));
    TableColumn<TrainerWinnings,String> tw3 = new TableColumn<>("Total Winnings"); tw3.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(String.valueOf(v.getValue().getTotalWinnings())));
    twTable.getColumns().addAll(tw1,tw2,tw3);
    Button loadTW = new Button("Load");
    VBox twBox = new VBox(8, loadTW, twTable); twPane.setContent(twBox);

    // Track stats table
    TitledPane tsPane = new TitledPane(); tsPane.setText("Track Stats");
    TableView<TrackStats> tsTable = new TableView<>();
    TableColumn<TrackStats,String> ts1 = new TableColumn<>("Track"); ts1.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(v.getValue().getTrackName()));
    TableColumn<TrackStats,String> ts2 = new TableColumn<>("Races"); ts2.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(String.valueOf(v.getValue().getRaceCount())));
    TableColumn<TrackStats,String> ts3 = new TableColumn<>("Participants"); ts3.setCellValueFactory(v-> new javafx.beans.property.SimpleStringProperty(String.valueOf(v.getValue().getTotalParticipants())));
    tsTable.getColumns().addAll(ts1,ts2,ts3);
    Button loadTS = new Button("Load");
    VBox tsBox = new VBox(8, loadTS, tsTable); tsPane.setContent(tsBox);

    // DAO
    GuestDao guest = new GuestDaoImpl();

    search.setOnAction(e -> {
      try {
        List<HorseTrainerInfo> list = guest.horsesByOwnerLastName(ownerLname.getText().trim());
        horsesTable.setItems(FXCollections.observableArrayList(list));
      } catch (Exception ex) {
        showAlert("Error", ex.getMessage());
      }
    });

    loadWinners.setOnAction(e -> {
      try { winnersTable.setItems(FXCollections.observableArrayList(guest.winningTrainers())); }
      catch (Exception ex) { showAlert("Error", ex.getMessage()); }
    });

    loadTW.setOnAction(e -> {
      try { twTable.setItems(FXCollections.observableArrayList(guest.trainerWinnings())); }
      catch (Exception ex) { showAlert("Error", ex.getMessage()); }
    });

    loadTS.setOnAction(e -> {
      try { tsTable.setItems(FXCollections.observableArrayList(guest.trackStats())); }
      catch (Exception ex) { showAlert("Error", ex.getMessage()); }
    });

    root.getChildren().addAll(horsesPane, winnersPane, twPane, tsPane);
    setContent(new ScrollPane(root));
  }

  private static <T> TableColumn<T,String> makeCol(String name, java.util.function.Function<T,String> fn) {
    TableColumn<T,String> c = new TableColumn<>(name);
    c.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(fn.apply(v.getValue())));
    return c;
  }

  private static void showAlert(String title, String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    a.setHeaderText(title); a.showAndWait();
  }
}
