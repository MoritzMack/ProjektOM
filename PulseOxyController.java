import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PulseOxyController {

    @FXML
    private TextField tPatientID;
    @FXML
    private Button bPatientID;
    @FXML
    private LineChart<String, Number> cPulse;
    @FXML
    private LineChart<String, Number> cSpO2;
    @FXML
    private Button bPulseAlarm;
    @FXML
    private Button bPulse5minSnooze;
    @FXML
    private Button bPulseDeactivateAlarm;
    @FXML
    private Label lPulseAlarm;
    @FXML
    private Label lPulse5minSnooze;
    @FXML
    private Label lPulseDeactivateAlarm;
    @FXML
    private Button bSpO2Alarm;
    @FXML
    private Button bSpO25minSnooze;
    @FXML
    private Button bSpO2DeactivateAlarm;
    @FXML
    private Label lSpO2Alarm;
    @FXML
    private Label lSpO25minSnooze;
    @FXML
    private Label lSpO2DeactivateAlarm;
    @FXML
    private Label tTime;
    @FXML
    private TextField tPulseUpperLimit;
    @FXML
    private TextField tPulseLowerLimit;
    @FXML
    private Button bPulseUpperLimit;
    @FXML
    private Button bPulseLowerLimit;
    @FXML
    private TextField tSpO2LowerLimit;
    @FXML
    private Button bSpO2LowerLimit;
    @FXML
    private ImageView iLogo;

    private double pulseUpperLimit;
    private double pulseLowerLimit;
    private double spO2LowerLimit;

    private XYChart.Series<String, Number> pulseDataSeries;
    private XYChart.Series<String, Number> spO2DataSeries;
    private XYChart.Series<String, Number> pulseUpperLimitSeries;
    private XYChart.Series<String, Number> pulseLowerLimitSeries;
    private XYChart.Series<String, Number> spO2LowerLimitSeries;
    private Timeline pulseSnoozeTimeline;
    private Timeline spO2SnoozeTimeline;
    private Timeline pulseAlarmFlashTimeline;
    private Timeline spO2AlarmFlashTimeline;
    private int pulseSnoozeCountdownSeconds = 300; // 5 minutes in seconds
    private int spO2SnoozeCountdownSeconds = 300; // 5 minutes in seconds

    @FXML
    public void initialize() {
        Image image = new Image("CardiTech_Logo.jpeg");
        iLogo.setImage(image);

        iLogo.setFitWidth(100);
        iLogo.setPreserveRatio(true);
        iLogo.setSmooth(true);
        iLogo.setCache(true);

        pulseDataSeries = new XYChart.Series<>();
        cPulse.getData().add(pulseDataSeries);

        spO2DataSeries = new XYChart.Series<>();
        cSpO2.getData().add(spO2DataSeries);

        pulseUpperLimitSeries = new XYChart.Series<>();
        pulseLowerLimitSeries = new XYChart.Series<>();
        spO2LowerLimitSeries = new XYChart.Series<>();

        cPulse.getData().addAll(pulseUpperLimitSeries, pulseLowerLimitSeries);
        cSpO2.getData().add(spO2LowerLimitSeries);

        CategoryAxis xAxisPulse = (CategoryAxis) cPulse.getXAxis();
        CategoryAxis xAxisSpO2 = (CategoryAxis) cSpO2.getXAxis();

        xAxisPulse.setCategories(FXCollections.observableArrayList("Time 1", "Time 2", "Time 3"));
        xAxisSpO2.setCategories(FXCollections.observableArrayList("Time 1", "Time 2", "Time 3"));

        setButtonStyles();
        startClock();

        bPatientID.setOnAction(event -> handlePatientID());
        bPulseUpperLimit.setOnAction(event -> handlePulseUpperLimit());
        bPulseLowerLimit.setOnAction(event -> handlePulseLowerLimit());
        bPulseAlarm.setOnAction(event -> handlePulseAlarm());
        bPulse5minSnooze.setOnAction(event -> handlePulse5minSnooze());
        bPulseDeactivateAlarm.setOnAction(event -> handlePulseDeactivateAlarm());
        bSpO2LowerLimit.setOnAction(event -> handleSpO2LowerLimit());
        bSpO2Alarm.setOnAction(event -> handleSpO2Alarm());
        bSpO25minSnooze.setOnAction(event -> handleSpO25minSnooze());
        bSpO2DeactivateAlarm.setOnAction(event -> handleSpO2DeactivateAlarm());
    }

    private void setButtonStyles() {
        String greyStyle = "-fx-background-color: lightgrey;";
        bPatientID.setStyle(greyStyle);
        bPulseAlarm.setStyle(greyStyle);
        bPulse5minSnooze.setStyle(greyStyle);
        bPulseDeactivateAlarm.setStyle(greyStyle);
        bSpO2Alarm.setStyle(greyStyle);
        bSpO25minSnooze.setStyle(greyStyle);
        bSpO2DeactivateAlarm.setStyle(greyStyle);
        bPulseUpperLimit.setStyle(greyStyle);
        bPulseLowerLimit.setStyle(greyStyle);
        bSpO2LowerLimit.setStyle(greyStyle);
    }

    private void updateAlarmButtonStyles() {
        if (bPulseDeactivateAlarm.isDisabled() || bPulse5minSnooze.isDisabled()) {
            bPulseAlarm.setStyle(("-fx-background-color: chartreuse; -fx-effect: dropshadow(gaussian, rgba(95, 168, 22, 0.8), 10, 0, 0, 0);"));
        } else {
            bPulseAlarm.setStyle("-fx-background-color: lightgrey;");
        }
        if (bSpO2DeactivateAlarm.isDisabled() || bSpO25minSnooze.isDisabled()) {
            bSpO2Alarm.setStyle(("-fx-background-color: chartreuse; -fx-effect: dropshadow(gaussian, rgba(95, 168, 22, 0.8), 10, 0, 0, 0);"));
        } else {
            bSpO2Alarm.setStyle("-fx-background-color: lightgrey;");
        }
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            tTime.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void handlePatientID() {
        // Handle Patient ID input
        System.out.println("Patient ID: " + tPatientID.getText());
    }

    private void handlePulseUpperLimit() {
        try {
            pulseUpperLimit = Double.parseDouble(tPulseUpperLimit.getText());
            updatePulseLimits();
            System.out.println("Pulse Upper Limit set to: " + pulseUpperLimit);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the Pulse Upper Limit.");
        }
    }

    private void handlePulseLowerLimit() {
        try {
            double lowerLimit = Double.parseDouble(tPulseLowerLimit.getText());
            if (lowerLimit < pulseUpperLimit) {
                pulseLowerLimit = lowerLimit;
                updatePulseLimits();
                System.out.println("Pulse Lower Limit set to: " + pulseLowerLimit);
            } else {
                showAlert("Invalid Input", "Lower limit must be less than upper limit.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the Pulse Lower Limit.");
        }
    }

    private void updatePulseLimits() {
        pulseUpperLimitSeries.getData().clear();
        pulseLowerLimitSeries.getData().clear();

        CategoryAxis xAxisPulse = (CategoryAxis) cPulse.getXAxis();
        for (String category : xAxisPulse.getCategories()) {
            pulseUpperLimitSeries.getData().add(new XYChart.Data<>(category, pulseUpperLimit));
            pulseLowerLimitSeries.getData().add(new XYChart.Data<>(category, pulseLowerLimit));
        }
    }

    private void handlePulseAlarm() {
        // Handle Pulse Alarm
        lPulseAlarm.setText("Terminate Alarm");
        lPulse5minSnooze.setText("Snooze Disabled");
        if (pulseSnoozeTimeline != null) {
            pulseSnoozeTimeline.stop();
            pulseSnoozeCountdownSeconds = 300;
        }
        if (pulseAlarmFlashTimeline != null) {
            pulseAlarmFlashTimeline.stop();
            bPulseAlarm.setStyle(""); // Remove flash red style
        }
        bPulse5minSnooze.setDisable(false);
        lPulseDeactivateAlarm.setText("Alarm Active");
        bPulseDeactivateAlarm.setDisable(false);
        storeEventData("Pulse Alarm Activated");
        updateAlarmButtonStyles();
    }

    private void handlePulse5minSnooze() {
        // Handle 5 min Snooze for Pulse
        if (pulseSnoozeTimeline != null) {
            pulseSnoozeTimeline.stop();
        }
        lPulseAlarm.setText("Activate Alarm");
        lPulse5minSnooze.setText("Snooze: 05:00");
        bPulse5minSnooze.setDisable(true);
        pulseSnoozeTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updatePulseSnoozeCountdown())
        );
        pulseSnoozeTimeline.setCycleCount(Animation.INDEFINITE);
        pulseSnoozeTimeline.play();
        storeEventData("Pulse Alarm Snoozed for 5 minutes");
        updateAlarmButtonStyles();
    }

    private void updatePulseSnoozeCountdown() {
        pulseSnoozeCountdownSeconds--;
        int minutes = pulseSnoozeCountdownSeconds / 60;
        int seconds = pulseSnoozeCountdownSeconds % 60;
        lPulse5minSnooze.setText(String.format("Snooze: %02d:%02d", minutes, seconds));

        if (pulseSnoozeCountdownSeconds == 0) {
            pulseSnoozeTimeline.stop();
            lPulse5minSnooze.setText("Snooze Disabled");
            storeEventData("Pulse Snooze Disabled");
        }
    }

    private void handlePulseDeactivateAlarm() {
        // Handle Pulse Alarm Deactivation
        lPulseDeactivateAlarm.setText("Alarm Disabled");
        if (pulseSnoozeTimeline != null) {
            pulseSnoozeTimeline.stop();
        }
        bPulse5minSnooze.setDisable(true);
        lPulse5minSnooze.setText("Snooze Disabled");
        bPulseDeactivateAlarm.setDisable(true);
        lPulseAlarm.setText("Activate Alarm");
        storeEventData("Pulse Alarm Deactivated");
        updateAlarmButtonStyles();
    }

    private void handleSpO2LowerLimit() {
        try {
            spO2LowerLimit = Double.parseDouble(tSpO2LowerLimit.getText());
            updateSpO2Limits();
            System.out.println("SpO2 Lower Limit set to: " + spO2LowerLimit);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the SpO2 Lower Limit.");
        }
    }

    private void updateSpO2Limits() {
        spO2LowerLimitSeries.getData().clear();

        CategoryAxis xAxisSpO2 = (CategoryAxis) cSpO2.getXAxis();
        for (String category : xAxisSpO2.getCategories()) {
            spO2LowerLimitSeries.getData().add(new XYChart.Data<>(category, spO2LowerLimit));
        }
    }

    private void handleSpO2Alarm() {
        // Handle SpO2 Alarm
        lSpO2Alarm.setText("Terminate Alarm");
        lSpO25minSnooze.setText("Snooze Disabled");
        if (spO2SnoozeTimeline != null) {
            spO2SnoozeTimeline.stop();
            spO2SnoozeCountdownSeconds = 300;
        }
        if (spO2AlarmFlashTimeline != null) {
            spO2AlarmFlashTimeline.stop();
            bSpO2Alarm.setStyle(""); // Remove flash red style
        }
        bSpO25minSnooze.setDisable(false);
        lSpO2DeactivateAlarm.setText("Alarm Active");
        bSpO2DeactivateAlarm.setDisable(false);
        storeEventData("SpO2 Alarm Activated");
        updateAlarmButtonStyles();
    }

    private void handleSpO25minSnooze() {
        // Handle 5 min Snooze for SpO2
        if (spO2SnoozeTimeline != null) {
            spO2SnoozeTimeline.stop();
        }
        lSpO2Alarm.setText("Activate Alarm");
        lSpO25minSnooze.setText("Snooze: 05:00");
        bSpO25minSnooze.setDisable(true);
        spO2SnoozeTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateSpO2SnoozeCountdown())
        );
        spO2SnoozeTimeline.setCycleCount(Animation.INDEFINITE);
        spO2SnoozeTimeline.play();
        storeEventData("SpO2 Alarm Snoozed for 5 minutes");
        updateAlarmButtonStyles();
    }

    private void updateSpO2SnoozeCountdown() {
        spO2SnoozeCountdownSeconds--;
        int minutes = spO2SnoozeCountdownSeconds / 60;
        int seconds = spO2SnoozeCountdownSeconds % 60;
        lSpO25minSnooze.setText(String.format("Snooze: %02d:%02d", minutes, seconds));

        if (spO2SnoozeCountdownSeconds == 0) {
            spO2SnoozeTimeline.stop();
            lSpO25minSnooze.setText("Snooze Disabled");
        }
    }

    private void handleSpO2DeactivateAlarm() {
        // Handle SpO2 Alarm Deactivation
        lSpO2DeactivateAlarm.setText("Alarm Disabled");
        if (spO2SnoozeTimeline != null) {
            spO2SnoozeTimeline.stop();
        }
        bSpO25minSnooze.setDisable(true);
        lSpO25minSnooze.setText("Snooze Disabled");
        bSpO2DeactivateAlarm.setDisable(true);
        lSpO2Alarm.setText("Activate Alarm");
        storeEventData("SpO2 Alarm Deactivated");
        updateAlarmButtonStyles();
    }

    private void storeEventData(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("event_data.txt", true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(timestamp + ": " + data);
            writer.newLine();
        } catch (IOException e) {
            showAlert("Data Storage Error", "An error occurred while storing the data.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}