package com.soccerbots.control.gui;

import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.controller.GameController;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ControllerPanel {
    private static final Logger logger = LoggerFactory.getLogger(ControllerPanel.class);

    private final ControllerManager controllerManager;
    private VBox root;
    private FlowPane controllerCardsContainer;
    private Button refreshButton;
    private Label controllerCountLabel;

    public ControllerPanel(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
        createGUI();
        setupEventHandlers();
        startStatusUpdater();
    }

    private void createGUI() {
        root = new VBox(24);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("grok-card");

        createHeader();
        createControllerCards();
        updateControllerDisplay();
    }

    private void createHeader() {
        HBox header = new HBox(24);
        header.setAlignment(Pos.CENTER_LEFT);

        Text titleText = new Text("Controller Management");
        titleText.getStyleClass().add("grok-title");

        controllerCountLabel = new Label("Controllers: 0");
        controllerCountLabel.getStyleClass().add("grok-caption");

        refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().addAll("grok-button", "secondary");
        refreshButton.setOnAction(e -> refreshControllers());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleText, spacer, controllerCountLabel, refreshButton);
        root.getChildren().add(header);
    }

    private void createControllerCards() {
        controllerCardsContainer = new FlowPane(16, 16);
        controllerCardsContainer.getStyleClass().add("grok-flow-pane");
        controllerCardsContainer.setPadding(new Insets(16, 0, 0, 0));
        controllerCardsContainer.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(controllerCardsContainer);
        scrollPane.getStyleClass().add("grok-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("controller-scroll");

        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox createControllerCard(GameController controller) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("robot-card");
        card.setPrefWidth(300);

        // Controller name and status
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Circle statusIndicator = new Circle(6);
        statusIndicator.getStyleClass().add("status-indicator");
        statusIndicator.getStyleClass().add("status-connected");

        Text nameText = new Text(controller.getName());
        nameText.getStyleClass().add("robot-name");
        nameText.setFont(Font.font("System", FontWeight.BOLD, 16));

        nameRow.getChildren().addAll(statusIndicator, nameText);

        // Controller info
        Label typeLabel = new Label("Type: USB Game Controller");
        typeLabel.getStyleClass().add("robot-ip");

        // Visual representation of controller
        VBox controllerVisual = createControllerVisualization(controller);

        // Status info
        Label statusLabel = new Label("Status: " + (controller.isConnected() ? "Connected" : "Disconnected"));
        statusLabel.getStyleClass().add("robot-status");

        // Action buttons
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);

        Button testButton = new Button("Test Input");
        testButton.getStyleClass().add("modern-button");
        testButton.setOnAction(e -> testController(controller));

        Button calibrateButton = new Button("Calibrate");
        calibrateButton.getStyleClass().addAll("modern-button");
        calibrateButton.setOnAction(e -> calibrateController(controller));

        buttonRow.getChildren().addAll(testButton, calibrateButton);

        card.getChildren().addAll(nameRow, typeLabel, controllerVisual, statusLabel, buttonRow);

        return card;
    }

    private VBox createControllerVisualization(GameController controller) {
        VBox visual = new VBox(10);
        visual.getStyleClass().add("controller-visual");
        visual.setAlignment(Pos.CENTER);
        visual.setPadding(new Insets(15));

        // Joystick visualization
        HBox joysticks = new HBox(30);
        joysticks.setAlignment(Pos.CENTER);

        // Left joystick
        VBox leftJoystick = createJoystickVisual("Left Stick",
            controller.getLastInput().getLeftStickX(), controller.getLastInput().getLeftStickY());

        // Right joystick
        VBox rightJoystick = createJoystickVisual("Right Stick",
            controller.getLastInput().getRightStickX(), controller.getLastInput().getRightStickY());

        joysticks.getChildren().addAll(leftJoystick, rightJoystick);

        // Button status
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        for (int i = 0; i < 4; i++) {
            Circle buttonIndicator = new Circle(8);
            buttonIndicator.getStyleClass().add("button-indicator");
            if (controller.getLastInput().getButton(i)) {
                buttonIndicator.setFill(Color.LIGHTBLUE);
            } else {
                buttonIndicator.setFill(Color.GRAY);
            }
            buttons.getChildren().add(buttonIndicator);
        }

        visual.getChildren().addAll(joysticks, buttons);
        return visual;
    }

    private VBox createJoystickVisual(String label, double x, double y) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 10px; -fx-text-fill: #cccccc;");

        // Joystick circle
        Circle circle = new Circle(25);
        circle.getStyleClass().add("joystick-circle");
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.LIGHTGRAY);

        // Joystick position dot
        Circle dot = new Circle(4);
        dot.getStyleClass().add("joystick-dot");

        // Calculate dot position based on joystick values
        double dotX = x * 20; // Scale to circle size
        double dotY = y * 20;
        dot.setTranslateX(dotX);
        dot.setTranslateY(dotY);

        StackPane joystickArea = new StackPane();
        joystickArea.getChildren().addAll(circle, dot);

        container.getChildren().addAll(labelText, joystickArea);
        return container;
    }

    private void setupEventHandlers() {
        // Additional event handling can be added here
    }

    private void testController(GameController controller) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Controller");
        alert.setHeaderText("Controller Test");
        alert.setContentText("Move joysticks and press buttons to test controller '" + controller.getName() + "'");
        alert.showAndWait();
        logger.info("Testing controller {}", controller.getName());
    }

    private void calibrateController(GameController controller) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Calibrate Controller");
        alert.setHeaderText("Controller Calibration");
        alert.setContentText("Calibration feature for controller '" + controller.getName() + "' would be implemented here.");
        alert.showAndWait();
        logger.info("Calibrating controller {}", controller.getName());
    }

    private void refreshControllers() {
        refreshButton.setDisable(true);

        // Add rotation animation to refresh button
        RotateTransition rotate = new RotateTransition(Duration.millis(500), refreshButton);
        rotate.setByAngle(360);
        rotate.play();

        Platform.runLater(() -> {
            controllerManager.refreshControllers();
            updateControllerDisplay();
            refreshButton.setDisable(false);
            logger.info("Controller list refreshed");
        });
    }

    private void updateControllerDisplay() {
        Platform.runLater(() -> {
            List<GameController> controllers = controllerManager.getConnectedControllers();

            controllerCardsContainer.getChildren().clear();

            for (GameController controller : controllers) {
                VBox controllerCard = createControllerCard(controller);

                // Add fade-in animation
                controllerCard.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), controllerCard);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                controllerCardsContainer.getChildren().add(controllerCard);
            }

            controllerCountLabel.setText("Controllers: " + controllers.size());
        });
    }

    private void startStatusUpdater() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(100), e -> updateControllerDisplay())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    public Parent getRoot() {
        return root;
    }
}