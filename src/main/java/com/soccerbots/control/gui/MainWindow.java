package com.soccerbots.control.gui;

import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.robot.RobotManager;
import com.soccerbots.control.controller.ControllerManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private NetworkManager networkManager;
    private RobotManager robotManager;
    private ControllerManager controllerManager;

    private BorderPane root;
    private VBox navigationBar;
    private StackPane contentArea;
    private HBox statusBar;
    private Button emergencyStopButton;

    // Navigation buttons
    private Button robotsTabBtn;
    private Button controllersTabBtn;
    private Button networkTabBtn;
    private Button monitoringTabBtn;
    private Button settingsTabBtn;

    // Content panels
    private RobotPanel robotPanel;
    private ControllerPanel controllerPanel;
    private NetworkPanel networkPanel;
    private MonitoringPanel monitoringPanel;
    private SettingsPanel settingsPanel;
    
    public MainWindow() {
        initializeManagers();
        createGUI();
        setupEventHandlers();
        logger.info("JavaFX MainWindow initialized successfully");
    }

    private void initializeManagers() {
        networkManager = new NetworkManager();
        robotManager = new RobotManager(networkManager);
        controllerManager = new ControllerManager(robotManager);
    }
    private void createGUI() {
        root = new BorderPane();
        root.getStyleClass().add("main-window");

        createNavigationBar();
        createContentArea();
        createStatusBar();

        root.setTop(navigationBar);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        // Initialize panels
        initializePanels();

        // Show robots panel by default
        showRobotsPanel();
    }

    private void createNavigationBar() {
        navigationBar = new VBox();
        navigationBar.getStyleClass().add("header-bar");
        navigationBar.setPrefHeight(80);

        // Main header container
        HBox headerContainer = new HBox();
        headerContainer.getStyleClass().add("header-bar");
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        headerContainer.setPadding(new Insets(16, 24, 16, 24));
        headerContainer.setSpacing(24);

        // Application title with Grok styling
        Text titleText = new Text("SoccerBots");
        titleText.getStyleClass().add("logo-text");

        // Navigation buttons container
        HBox navButtons = new HBox(8);
        navButtons.setAlignment(Pos.CENTER_LEFT);

        robotsTabBtn = createNavButton("Robots");
        controllersTabBtn = createNavButton("Controllers");
        networkTabBtn = createNavButton("Network");
        monitoringTabBtn = createNavButton("Monitoring");
        settingsTabBtn = createNavButton("Settings");

        navButtons.getChildren().addAll(
            robotsTabBtn, controllersTabBtn, networkTabBtn, monitoringTabBtn, settingsTabBtn
        );

        // Emergency stop button with Grok styling
        emergencyStopButton = new Button("EMERGENCY STOP");
        emergencyStopButton.getStyleClass().addAll("grok-button", "danger");
        emergencyStopButton.setPrefWidth(160);
        emergencyStopButton.setPrefHeight(36);
        emergencyStopButton.setOnAction(this::handleEmergencyStop);

        // Spacer to push emergency button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerContainer.getChildren().addAll(titleText, navButtons, spacer, emergencyStopButton);
        navigationBar.getChildren().add(headerContainer);
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setPrefHeight(36);
        button.setMinWidth(80);

        // Add Grok-style hover animation
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return button;
    }

    private void createContentArea() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
    }

    private void createStatusBar() {
        statusBar = new HBox(24);
        statusBar.getStyleClass().add("header-bar");
        statusBar.setPadding(new Insets(12, 24, 12, 24));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPrefHeight(48);
        statusBar.setStyle("-fx-border-color: -border-primary; -fx-border-width: 1 0 0 0;");

        Label networkStatusLabel = new Label("Network: Disconnected");
        networkStatusLabel.getStyleClass().add("grok-caption");

        Label robotCountLabel = new Label("Robots: 0");
        robotCountLabel.getStyleClass().add("grok-caption");

        Label controllerCountLabel = new Label("Controllers: 0");
        controllerCountLabel.getStyleClass().add("grok-caption");

        statusBar.getChildren().addAll(networkStatusLabel, robotCountLabel, controllerCountLabel);
    }

    private void initializePanels() {
        robotPanel = new RobotPanel(robotManager);
        controllerPanel = new ControllerPanel(controllerManager);
        networkPanel = new NetworkPanel(networkManager);
        monitoringPanel = new MonitoringPanel();
        settingsPanel = new SettingsPanel();

        // Set up navigation button actions
        robotsTabBtn.setOnAction(e -> showRobotsPanel());
        controllersTabBtn.setOnAction(e -> showControllersPanel());
        networkTabBtn.setOnAction(e -> showNetworkPanel());
        monitoringTabBtn.setOnAction(e -> showMonitoringPanel());
        settingsTabBtn.setOnAction(e -> showSettingsPanel());
    }

    private void showPanel(Parent panel, Button activeButton) {
        // Clear active state from all buttons
        robotsTabBtn.getStyleClass().remove("active");
        controllersTabBtn.getStyleClass().remove("active");
        networkTabBtn.getStyleClass().remove("active");
        monitoringTabBtn.getStyleClass().remove("active");
        settingsTabBtn.getStyleClass().remove("active");

        // Set active state for clicked button
        activeButton.getStyleClass().add("active");

        // Animate panel transition with Grok-style timing
        contentArea.getChildren().clear();
        contentArea.getChildren().add(panel);

        // Grok-style fade in animation (200ms)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), panel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Subtle scale animation for modern feel
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), panel);
        scaleIn.setFromX(0.98);
        scaleIn.setFromY(0.98);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        fadeIn.play();
        scaleIn.play();
    }

    private void showRobotsPanel() {
        showPanel(robotPanel.getRoot(), robotsTabBtn);
    }

    private void showControllersPanel() {
        showPanel(controllerPanel.getRoot(), controllersTabBtn);
    }

    private void showNetworkPanel() {
        showPanel(networkPanel.getRoot(), networkTabBtn);
    }

    private void showMonitoringPanel() {
        showPanel(monitoringPanel.getRoot(), monitoringTabBtn);
    }

    private void showSettingsPanel() {
        showPanel(settingsPanel.getRoot(), settingsTabBtn);
    }

    private void setupEventHandlers() {
        // Start status update timer
        Platform.runLater(() -> {
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(1), e -> updateStatusConnections())
            );
            timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
            timeline.play();
        });
    }

    private void handleEmergencyStop(javafx.event.ActionEvent e) {
        boolean isCurrentlyActive = controllerManager.isEmergencyStopActive();

        if (isCurrentlyActive) {
            controllerManager.deactivateEmergencyStop();
            emergencyStopButton.setText("EMERGENCY STOP");
            emergencyStopButton.getStyleClass().remove("success");
            emergencyStopButton.getStyleClass().add("danger");
            logger.info("Emergency stop deactivated by user");
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Emergency Stop");
            alert.setHeaderText("Emergency Stop Confirmation");
            alert.setContentText("This will immediately stop all robot movement.\nAre you sure?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controllerManager.activateEmergencyStop();
                    emergencyStopButton.setText("RESUME CONTROL");
                    emergencyStopButton.getStyleClass().remove("danger");
                    emergencyStopButton.getStyleClass().add("success");
                    logger.warn("Emergency stop activated by user");
                }
            });
        }
    }

    private void updateStatusConnections() {
        Platform.runLater(() -> {
            if (statusBar != null && statusBar.getChildren().size() >= 3) {
                Label networkLabel = (Label) statusBar.getChildren().get(0);
                Label robotLabel = (Label) statusBar.getChildren().get(1);
                Label controllerLabel = (Label) statusBar.getChildren().get(2);

                networkLabel.setText("Network: " + (networkManager.isNetworkActive() ? "Connected" : "Disconnected"));
                robotLabel.setText("Robots: " + robotManager.getConnectedRobotCount());
                controllerLabel.setText("Controllers: " + controllerManager.getConnectedControllerCount());
            }
        });
    }

    public Parent getRoot() {
        return root;
    }

    public void shutdown() {
        logger.info("Shutting down application");
        if (controllerManager != null) {
            controllerManager.shutdown();
        }
        if (robotManager != null) {
            robotManager.shutdown();
        }
        if (networkManager != null) {
            networkManager.shutdown();
        }
    }
}