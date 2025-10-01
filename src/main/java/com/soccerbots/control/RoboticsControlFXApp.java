package com.soccerbots.control;

import com.soccerbots.control.gui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoboticsControlFXApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(RoboticsControlFXApp.class);

    public static void main(String[] args) {
        logger.info("Starting SoccerBots Robotics Control System (JavaFX)");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("SoccerBots Control Station");
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);

            MainWindow mainWindow = new MainWindow();
            Scene scene = new Scene(mainWindow.getRoot(), 1400, 900);

            // Load Grok-inspired CSS theme
            scene.getStylesheets().add(getClass().getResource("/styles/grok.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                logger.info("Application closing...");
                mainWindow.shutdown();
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
            logger.info("JavaFX application initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize JavaFX application", e);
            System.exit(1);
        }
    }
}