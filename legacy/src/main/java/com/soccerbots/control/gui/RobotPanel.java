package com.soccerbots.control.gui;

import com.soccerbots.control.robot.Robot;
import com.soccerbots.control.robot.RobotManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RobotPanel {
    private static final Logger logger = LoggerFactory.getLogger(RobotPanel.class);

    private final RobotManager robotManager;
    private VBox root;
    private FlowPane robotCardsContainer;
    private Button addRobotButton;
    private Button refreshButton;
    private Label robotCountLabel;
    private ObservableList<Robot> robots;
    
    public RobotPanel(RobotManager robotManager) {
        this.robotManager = robotManager;
        this.robots = FXCollections.observableArrayList();
        createGUI();
        setupEventHandlers();
        startStatusUpdater();
    }
    private void createGUI() {
        root = new VBox(24);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("grok-card");

        createHeader();
        createRobotCards();
        updateRobotDisplay();
    }

    private void createHeader() {
        // Network status section with Grok styling
        VBox networkStatus = new VBox(12);
        networkStatus.getStyleClass().add("grok-surface");
        networkStatus.setPadding(new Insets(16));

        Text networkLabel = new Text("Network Status");
        networkLabel.getStyleClass().add("grok-subtitle");

        Label currentNetwork = new Label("Current: " +
            (robotManager.isNetworkReady() ? robotManager.getExpectedNetworkName() : "Not Connected"));
        currentNetwork.getStyleClass().add("grok-body");

        Label expectedNetwork = new Label("Expected: " + robotManager.getExpectedNetworkName());
        expectedNetwork.getStyleClass().add("grok-body");

        Label statusText = new Label(robotManager.isConnectedToRobotNetwork() ?
            "✓ Ready for ESP32 communication" : "⚠ Connect to WATCHTOWER network");
        statusText.getStyleClass().add(robotManager.isConnectedToRobotNetwork() ? "grok-body" : "grok-body");
        statusText.setStyle(robotManager.isConnectedToRobotNetwork() ?
            "-fx-text-fill: -success-green;" : "-fx-text-fill: -warning-yellow;");

        networkStatus.getChildren().addAll(networkLabel, currentNetwork, expectedNetwork, statusText);

        // Robot management header with Grok styling
        HBox header = new HBox(24);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 0, 0, 0));

        Text titleText = new Text("ESP32 Robot Management");
        titleText.getStyleClass().add("grok-title");

        robotCountLabel = new Label("Robots: 0");
        robotCountLabel.getStyleClass().add("grok-caption");

        addRobotButton = new Button("Add Robot");
        addRobotButton.getStyleClass().addAll("grok-button", "success");
        addRobotButton.setOnAction(e -> showAddRobotDialog());

        Button gameControlButton = new Button(robotManager.isInTeleopMode() ? "Stop Teleop" : "Start Teleop");
        gameControlButton.getStyleClass().addAll("grok-button",
            robotManager.isInTeleopMode() ? "danger" : "success");
        gameControlButton.setOnAction(e -> toggleTeleopMode(gameControlButton));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleText, spacer, robotCountLabel, gameControlButton, addRobotButton);

        root.getChildren().addAll(networkStatus, header);
    }

    private void createRobotCards() {
        robotCardsContainer = new FlowPane(16, 16);
        robotCardsContainer.getStyleClass().add("grok-flow-pane");
        robotCardsContainer.setPadding(new Insets(16, 0, 0, 0));
        robotCardsContainer.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(robotCardsContainer);
        scrollPane.getStyleClass().add("grok-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox createRobotCard(Robot robot) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("robot-card");

        if (robot.isConnected()) {
            card.getStyleClass().add("connected");
        } else {
            card.getStyleClass().add("disconnected");
        }

        // Robot name and status indicator with Grok styling
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Circle statusIndicator = new Circle(4);
        statusIndicator.getStyleClass().add("status-indicator");
        if (robot.isConnected()) {
            statusIndicator.getStyleClass().add("status-connected");
        } else {
            statusIndicator.getStyleClass().add("status-disconnected");
        }

        Text nameText = new Text(robot.getName());
        nameText.getStyleClass().add("robot-name");

        nameRow.getChildren().addAll(statusIndicator, nameText);

        // IP Address
        Label ipLabel = new Label(robot.getIpAddress());
        ipLabel.getStyleClass().add("robot-ip");

        // Status
        Label statusLabel = new Label(robot.isConnected() ? "Connected" : "Disconnected");
        statusLabel.getStyleClass().add("robot-status");
        statusLabel.setStyle(robot.isConnected() ?
            "-fx-text-fill: -success-green;" : "-fx-text-fill: -error-red;");

        // Last seen
        long timeSinceLastSeen = robot.getTimeSinceLastSeen() / 1000;
        Label lastSeenLabel = new Label("Last seen " + timeSinceLastSeen + "s ago");
        lastSeenLabel.getStyleClass().add("robot-last-seen");

        // Action buttons with Grok styling
        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER);

        Button testButton = new Button("Test");
        testButton.getStyleClass().addAll("grok-button", "secondary");
        testButton.setOnAction(e -> testRobot(robot));

        Button stopButton = new Button("Stop");
        stopButton.getStyleClass().addAll("grok-button", "warning");
        stopButton.setOnAction(e -> stopRobot(robot));

        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().addAll("grok-button", "ghost");
        removeButton.setOnAction(e -> handleRemoveRobot(robot));

        buttonRow.getChildren().addAll(testButton, stopButton, removeButton);

        card.getChildren().addAll(nameRow, ipLabel, statusLabel, lastSeenLabel, buttonRow);

        // Add Grok hover animation
        addCardAnimations(card);

        return card;
    }

    private void addCardAnimations(VBox card) {
        // Grok-style subtle animations (150ms)
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), card);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), card);

        card.setOnMouseEntered(e -> {
            scaleIn.setToX(1.02);
            scaleIn.setToY(1.02);
            scaleIn.play();
        });

        card.setOnMouseExited(e -> {
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
        });
    }

    private void setupEventHandlers() {
        // No additional setup needed for now
    }

    private void showAddRobotDialog() {
        Dialog<Robot> dialog = new Dialog<>();
        dialog.setTitle("Add ESP32 Robot");
        dialog.setHeaderText("Add New ESP32 Robot");

        // Set button types
        ButtonType addButtonType = new ButtonType("Add Robot", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Robot Name (e.g., YOUR NAME HERE)");
        TextField ipField = new TextField();
        ipField.setPromptText("IP Address (e.g., 192.168.1.100)");

        grid.add(new Label("Robot Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("IP Address:"), 0, 1);
        grid.add(ipField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable add button depending on whether fields are filled
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty() || ipField.getText().trim().isEmpty());
        });
        ipField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty());
        });

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to a robot when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return robotManager.addRobot(nameField.getText().trim(), ipField.getText().trim());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(robot -> {
            logger.info("Added ESP32 robot: {} at {}", robot.getName(), robot.getIpAddress());
            updateRobotDisplay();
        });
    }

    private void toggleTeleopMode(Button gameControlButton) {
        if (robotManager.isInTeleopMode()) {
            robotManager.stopTeleop();
            gameControlButton.setText("Start Teleop");
            gameControlButton.getStyleClass().removeAll("danger");
            gameControlButton.getStyleClass().add("success");
        } else {
            robotManager.startTeleop();
            gameControlButton.setText("Stop Teleop");
            gameControlButton.getStyleClass().removeAll("success");
            gameControlButton.getStyleClass().add("danger");
        }
    }

    private void testRobot(Robot robot) {
        robotManager.testRobotConnection(robot.getName());
        showAlert("Robot Test", "Test signal sent to " + robot.getName(), Alert.AlertType.INFORMATION);
    }

    private void stopRobot(Robot robot) {
        robotManager.sendStopCommand(robot.getName());
        showAlert("Robot Stop", "Stop command sent to " + robot.getName(), Alert.AlertType.INFORMATION);
    }

    private void handleRemoveRobot(Robot robot) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Robot");
        alert.setHeaderText("Remove Robot Confirmation");
        alert.setContentText("Are you sure you want to remove robot '" + robot.getName() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                robotManager.removeRobot(robot.getId());
                updateRobotDisplay();
                logger.info("Removed robot: {}", robot.getName());
            }
        });
    }

    private void updateRobotDisplay() {
        Platform.runLater(() -> {
            List<Robot> connectedRobots = robotManager.getConnectedRobots();
            robots.setAll(connectedRobots);

            robotCardsContainer.getChildren().clear();

            for (Robot robot : connectedRobots) {
                VBox robotCard = createRobotCard(robot);

                // Add fade-in animation for new cards
                robotCard.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), robotCard);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                robotCardsContainer.getChildren().add(robotCard);
            }

            robotCountLabel.setText("Robots: " + connectedRobots.size());
        });
    }

    private void startStatusUpdater() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(2), e -> {
                robotManager.clearOfflineRobots();
                updateRobotDisplay();
            })
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
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