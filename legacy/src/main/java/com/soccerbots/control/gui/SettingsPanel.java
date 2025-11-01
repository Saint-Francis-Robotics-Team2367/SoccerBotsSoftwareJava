package com.soccerbots.control.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsPanel {
    private static final Logger logger = LoggerFactory.getLogger(SettingsPanel.class);

    private VBox root;
    private TextField robotPortField;
    private Slider updateRateSlider;
    private CheckBox enableLoggingCheck;
    private ComboBox<String> logLevelCombo;
    private CheckBox enableAnimationsCheck;
    private Slider animationSpeedSlider;

    public SettingsPanel() {
        createGUI();
        setupEventHandlers();
        loadSettings();
    }

    private void createGUI() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("card");

        createHeader();
        createNetworkSettings();
        createApplicationSettings();
        createUISettings();
        createActionButtons();
    }

    private void createHeader() {
        Text titleText = new Text("Application Settings");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleText.getStyleClass().add("card-title");
        root.getChildren().add(titleText);
    }

    private void createNetworkSettings() {
        VBox networkSection = new VBox(15);
        networkSection.getStyleClass().add("card");
        networkSection.setPadding(new Insets(15));

        Text sectionTitle = new Text("Network Settings");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox portRow = new HBox(10);
        portRow.setAlignment(Pos.CENTER_LEFT);

        Label portLabel = new Label("Robot Communication Port:");
        robotPortField = new TextField("12346");
        robotPortField.getStyleClass().add("modern-text-field");
        robotPortField.setPrefWidth(100);

        portRow.getChildren().addAll(portLabel, robotPortField);

        HBox updateRow = new HBox(10);
        updateRow.setAlignment(Pos.CENTER_LEFT);

        Label updateLabel = new Label("Update Rate (Hz):");
        updateRateSlider = new Slider(10, 60, 30);
        updateRateSlider.setShowTickLabels(true);
        updateRateSlider.setShowTickMarks(true);
        updateRateSlider.setMajorTickUnit(10);
        updateRateSlider.setPrefWidth(200);

        Label updateValueLabel = new Label("30 Hz");
        updateRateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateValueLabel.setText(String.format("%.0f Hz", newVal.doubleValue()));
        });

        updateRow.getChildren().addAll(updateLabel, updateRateSlider, updateValueLabel);

        networkSection.getChildren().addAll(sectionTitle, portRow, updateRow);
        root.getChildren().add(networkSection);
    }

    private void createApplicationSettings() {
        VBox appSection = new VBox(15);
        appSection.getStyleClass().add("card");
        appSection.setPadding(new Insets(15));

        Text sectionTitle = new Text("Application Settings");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        enableLoggingCheck = new CheckBox("Enable Debug Logging");
        enableLoggingCheck.setSelected(true);

        HBox logLevelRow = new HBox(10);
        logLevelRow.setAlignment(Pos.CENTER_LEFT);

        Label logLevelLabel = new Label("Log Level:");
        logLevelCombo = new ComboBox<>();
        logLevelCombo.getItems().addAll("DEBUG", "INFO", "WARN", "ERROR");
        logLevelCombo.setValue("INFO");
        logLevelCombo.getStyleClass().add("modern-combo");

        logLevelRow.getChildren().addAll(logLevelLabel, logLevelCombo);

        appSection.getChildren().addAll(sectionTitle, enableLoggingCheck, logLevelRow);
        root.getChildren().add(appSection);
    }

    private void createUISettings() {
        VBox uiSection = new VBox(15);
        uiSection.getStyleClass().add("card");
        uiSection.setPadding(new Insets(15));

        Text sectionTitle = new Text("User Interface Settings");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        enableAnimationsCheck = new CheckBox("Enable Animations");
        enableAnimationsCheck.setSelected(true);

        HBox animSpeedRow = new HBox(10);
        animSpeedRow.setAlignment(Pos.CENTER_LEFT);

        Label animSpeedLabel = new Label("Animation Speed:");
        animationSpeedSlider = new Slider(0.5, 2.0, 1.0);
        animationSpeedSlider.setShowTickLabels(true);
        animationSpeedSlider.setShowTickMarks(true);
        animationSpeedSlider.setMajorTickUnit(0.5);
        animationSpeedSlider.setPrefWidth(200);

        Label animSpeedValueLabel = new Label("1.0x");
        animationSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animSpeedValueLabel.setText(String.format("%.1fx", newVal.doubleValue()));
        });

        animSpeedRow.getChildren().addAll(animSpeedLabel, animationSpeedSlider, animSpeedValueLabel);

        uiSection.getChildren().addAll(sectionTitle, enableAnimationsCheck, animSpeedRow);
        root.getChildren().add(uiSection);
    }

    private void createActionButtons() {
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().addAll("modern-button", "success-button");
        saveButton.setOnAction(e -> saveSettings());

        Button resetButton = new Button("Reset to Defaults");
        resetButton.getStyleClass().addAll("modern-button", "danger-button");
        resetButton.setOnAction(e -> resetToDefaults());

        Button aboutButton = new Button("About");
        aboutButton.getStyleClass().add("modern-button");
        aboutButton.setOnAction(e -> showAbout());

        buttonRow.getChildren().addAll(saveButton, resetButton, aboutButton);
        root.getChildren().add(buttonRow);
    }

    private void setupEventHandlers() {
        // Enable/disable animation speed slider based on checkbox
        enableAnimationsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            animationSpeedSlider.setDisable(!newVal);
        });

        // Enable/disable log level combo based on logging checkbox
        enableLoggingCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            logLevelCombo.setDisable(!newVal);
        });
    }

    private void loadSettings() {
        // Load settings from preferences or config file
        // For now, use default values
        logger.info("Settings loaded");
    }

    private void saveSettings() {
        try {
            // Save all settings
            int port = Integer.parseInt(robotPortField.getText());
            double updateRate = updateRateSlider.getValue();
            boolean loggingEnabled = enableLoggingCheck.isSelected();
            String logLevel = logLevelCombo.getValue();
            boolean animationsEnabled = enableAnimationsCheck.isSelected();
            double animationSpeed = animationSpeedSlider.getValue();

            // Validate settings
            if (port < 1024 || port > 65535) {
                showAlert("Invalid Port", "Port must be between 1024 and 65535", Alert.AlertType.ERROR);
                return;
            }

            // Here you would save to preferences or config file
            logger.info("Settings saved - Port: {}, Update Rate: {}, Logging: {}, Log Level: {}, Animations: {}, Anim Speed: {}",
                port, updateRate, loggingEnabled, logLevel, animationsEnabled, animationSpeed);

            showAlert("Settings Saved", "All settings have been saved successfully", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid port number", Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Failed to save settings", e);
            showAlert("Save Error", "Failed to save settings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void resetToDefaults() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reset Settings");
        confirmation.setHeaderText("Reset to Default Settings");
        confirmation.setContentText("Are you sure you want to reset all settings to their default values?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                robotPortField.setText("12346");
                updateRateSlider.setValue(30);
                enableLoggingCheck.setSelected(true);
                logLevelCombo.setValue("INFO");
                enableAnimationsCheck.setSelected(true);
                animationSpeedSlider.setValue(1.0);

                logger.info("Settings reset to defaults");
                showAlert("Settings Reset", "All settings have been reset to default values", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About SoccerBots Control Station");
        about.setHeaderText("SoccerBots Control Station v2.0");
        about.setContentText(
            "A modern JavaFX-based control application for managing soccer robots.\n\n" +
            "Features:\n" +
            "• Modern and responsive UI\n" +
            "• Real-time robot control\n" +
            "• Controller management\n" +
            "• Network communication\n" +
            "• System monitoring\n\n" +
            "Built with JavaFX and love for robotics."
        );
        about.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Parent getRoot() {
        return root;
    }
}