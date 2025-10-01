package com.soccerbots.control.gui;

import com.soccerbots.control.network.NetworkManager;
import javafx.application.Platform;
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

public class NetworkPanel {
    private static final Logger logger = LoggerFactory.getLogger(NetworkPanel.class);

    private final NetworkManager networkManager;
    private VBox root;
    private Label networkStatusLabel;
    private Label robotPortLabel;
    private TextField robotIpField;
    private Button connectButton;
    private Button disconnectButton;

    public NetworkPanel(NetworkManager networkManager) {
        this.networkManager = networkManager;
        createGUI();
        setupEventHandlers();
        startStatusUpdater();
    }

    private void createGUI() {
        root = new VBox(24);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("grok-card");

        createHeader();
        createNetworkSettings();
        createRobotConnection();
        updateNetworkStatus();
    }

    private void createHeader() {
        Text titleText = new Text("Network Configuration");
        titleText.getStyleClass().add("grok-title");
        root.getChildren().add(titleText);
    }

    private void createNetworkSettings() {
        VBox networkSection = new VBox(12);
        networkSection.getStyleClass().add("grok-surface");
        networkSection.setPadding(new Insets(16));

        Text sectionTitle = new Text("Network Status");
        sectionTitle.getStyleClass().add("grok-subtitle");

        networkStatusLabel = new Label("Checking network status...");
        networkStatusLabel.getStyleClass().add("network-status");

        Label expectedNetworkLabel = new Label("Expected Network: " + NetworkManager.EXPECTED_WIFI_NETWORK);
        expectedNetworkLabel.getStyleClass().add("grok-body");

        Label portLabel = new Label("ESP32 Communication Port: " + NetworkManager.ESP32_UDP_PORT);
        portLabel.getStyleClass().add("grok-caption");
        portLabel.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");

        Label infoLabel = new Label("ESP32 robots expect to connect to '" + NetworkManager.EXPECTED_WIFI_NETWORK + "' network. Make sure you're connected to the same network.");
        infoLabel.getStyleClass().addAll("grok-body", "info-surface");
        infoLabel.setWrapText(true);
        infoLabel.setPadding(new Insets(12));

        networkSection.getChildren().addAll(sectionTitle, networkStatusLabel, expectedNetworkLabel, portLabel, infoLabel);
        root.getChildren().add(networkSection);
    }

    private void createRobotConnection() {
        VBox connectionSection = new VBox(16);
        connectionSection.getStyleClass().add("grok-surface");
        connectionSection.setPadding(new Insets(16));

        Text sectionTitle = new Text("Direct Robot Connection");
        sectionTitle.getStyleClass().add("grok-subtitle");

        VBox ipRow = new VBox(8);

        Label ipLabel = new Label("Robot IP Address");
        ipLabel.getStyleClass().add("grok-body");

        robotIpField = new TextField();
        robotIpField.setPromptText("192.168.1.100");
        robotIpField.getStyleClass().add("grok-text-field");
        robotIpField.setPrefWidth(200);

        ipRow.getChildren().addAll(ipLabel, robotIpField);

        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.setPadding(new Insets(8, 0, 0, 0));

        connectButton = new Button("Connect");
        connectButton.getStyleClass().addAll("grok-button", "success");
        connectButton.setOnAction(e -> connectToRobot());

        disconnectButton = new Button("Disconnect");
        disconnectButton.getStyleClass().addAll("grok-button", "danger");
        disconnectButton.setOnAction(e -> disconnectFromRobot());
        disconnectButton.setDisable(true);

        buttonRow.getChildren().addAll(connectButton, disconnectButton);

        connectionSection.getChildren().addAll(sectionTitle, ipRow, buttonRow);
        root.getChildren().add(connectionSection);
    }

    private void setupEventHandlers() {
        // Event handlers are set up in createGUI methods
    }

    private void connectToRobot() {
        String ip = robotIpField.getText().trim();
        if (ip.isEmpty()) {
            showAlert("Error", "Please enter a robot IP address", Alert.AlertType.ERROR);
            return;
        }

        connectButton.setDisable(true);
        connectButton.setText("Connecting...");

        // Simulate connection attempt
        Platform.runLater(() -> {
            try {
                // Here you would implement actual connection logic
                logger.info("Attempting to connect to robot at IP: {}", ip);

                // For now, just simulate success
                showAlert("Success", "Connected to robot at " + ip, Alert.AlertType.INFORMATION);

                connectButton.setDisable(false);
                connectButton.setText("Connect to Robot");
                disconnectButton.setDisable(false);

            } catch (Exception e) {
                logger.error("Failed to connect to robot", e);
                showAlert("Connection Error", "Failed to connect to robot: " + e.getMessage(), Alert.AlertType.ERROR);
                connectButton.setDisable(false);
                connectButton.setText("Connect to Robot");
            }
        });
    }

    private void disconnectFromRobot() {
        // Implement disconnection logic
        logger.info("Disconnecting from robot");

        connectButton.setDisable(false);
        disconnectButton.setDisable(true);

        showAlert("Disconnected", "Disconnected from robot", Alert.AlertType.INFORMATION);
    }

    private void updateNetworkStatus() {
        Platform.runLater(() -> {
            boolean isActive = networkManager.isNetworkActive();
            networkStatusLabel.setText(isActive ? "✓ Connected" : "⚠ Disconnected");
            networkStatusLabel.getStyleClass().add("grok-body");

            if (isActive) {
                networkStatusLabel.getStyleClass().removeAll("status-disconnected");
                networkStatusLabel.getStyleClass().add("status-connected");
                networkStatusLabel.setStyle("-fx-text-fill: -success-green;");
            } else {
                networkStatusLabel.getStyleClass().removeAll("status-connected");
                networkStatusLabel.getStyleClass().add("status-disconnected");
                networkStatusLabel.setStyle("-fx-text-fill: -error-red;");
            }
        });
    }

    private void startStatusUpdater() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> updateNetworkStatus())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public Parent getRoot() {
        return root;
    }
}