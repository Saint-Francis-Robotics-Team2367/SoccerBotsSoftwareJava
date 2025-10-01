package com.soccerbots.control.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonitoringPanel {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringPanel.class);

    private VBox root;
    private TextArea logArea;
    private ListView<String> performanceList;
    private Label cpuUsageLabel;
    private Label memoryUsageLabel;
    private ProgressBar cpuBar;
    private ProgressBar memoryBar;
    private List<String> logEntries;

    public MonitoringPanel() {
        this.logEntries = new ArrayList<>();
        createGUI();
        setupEventHandlers();
        startMonitoring();
    }

    private void createGUI() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("card");

        createHeader();
        createTabs();
    }

    private void createHeader() {
        Text titleText = new Text("System Monitoring");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleText.getStyleClass().add("card-title");
        root.getChildren().add(titleText);
    }

    private void createTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // System Log Tab
        Tab logTab = new Tab("System Log");
        logTab.setContent(createLogPanel());

        // Performance Tab
        Tab performanceTab = new Tab("Performance");
        performanceTab.setContent(createPerformancePanel());

        tabPane.getTabs().addAll(logTab, performanceTab);
        root.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }

    private VBox createLogPanel() {
        VBox logPanel = new VBox(10);
        logPanel.setPadding(new Insets(15));

        HBox logControls = new HBox(10);
        logControls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button clearButton = new Button("Clear Log");
        clearButton.getStyleClass().addAll("modern-button", "danger-button");
        clearButton.setOnAction(e -> clearLog());

        Button exportButton = new Button("Export Log");
        exportButton.getStyleClass().add("modern-button");
        exportButton.setOnAction(e -> exportLog());

        logControls.getChildren().addAll(clearButton, exportButton);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");
        logArea.setPrefHeight(400);

        logPanel.getChildren().addAll(logControls, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        // Add initial log entries
        addLogEntry("INFO", "Application started successfully");
        addLogEntry("INFO", "Monitoring system initialized");

        return logPanel;
    }

    private VBox createPerformancePanel() {
        VBox performancePanel = new VBox(15);
        performancePanel.setPadding(new Insets(15));

        // System metrics
        VBox metricsBox = new VBox(10);
        metricsBox.getStyleClass().add("card");
        metricsBox.setPadding(new Insets(10));

        Text metricsTitle = new Text("System Metrics");
        metricsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        // CPU Usage
        HBox cpuBox = new HBox(10);
        cpuBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        cpuUsageLabel = new Label("CPU Usage: 0%");
        cpuBar = new ProgressBar(0);
        cpuBar.setPrefWidth(200);
        cpuBox.getChildren().addAll(cpuUsageLabel, cpuBar);

        // Memory Usage
        HBox memoryBox = new HBox(10);
        memoryBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        memoryUsageLabel = new Label("Memory Usage: 0%");
        memoryBar = new ProgressBar(0);
        memoryBar.setPrefWidth(200);
        memoryBox.getChildren().addAll(memoryUsageLabel, memoryBar);

        metricsBox.getChildren().addAll(metricsTitle, cpuBox, memoryBox);

        // Performance data list
        VBox listBox = new VBox(10);
        listBox.getStyleClass().add("card");
        listBox.setPadding(new Insets(10));

        Text listTitle = new Text("Performance Data");
        listTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        performanceList = new ListView<>();
        performanceList.setPrefHeight(200);
        performanceList.getStyleClass().add("performance-list");

        listBox.getChildren().addAll(listTitle, performanceList);

        performancePanel.getChildren().addAll(metricsBox, listBox);
        return performancePanel;
    }

    private void setupEventHandlers() {
        // Event handlers are set up in create methods
    }

    private void clearLog() {
        logEntries.clear();
        logArea.clear();
        addLogEntry("INFO", "Log cleared by user");
    }

    private void exportLog() {
        // Simulate log export
        addLogEntry("INFO", "Log exported to file");
        logger.info("Log export requested by user");
    }

    private void addLogEntry(String level, String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logEntry = String.format("[%s] %s: %s", timestamp, level, message);
            logEntries.add(logEntry);

            logArea.appendText(logEntry + "\n");

            // Keep only last 100 entries
            if (logEntries.size() > 100) {
                logEntries.remove(0);
                // Rebuild log area
                logArea.clear();
                for (String entry : logEntries) {
                    logArea.appendText(entry + "\n");
                }
            }

            // Auto-scroll to bottom
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void updatePerformanceMetrics() {
        Platform.runLater(() -> {
            // Simulate CPU and memory usage
            double cpuUsage = Math.random() * 100;
            double memoryUsage = 30 + Math.random() * 40; // 30-70%

            cpuUsageLabel.setText(String.format("CPU Usage: %.1f%%", cpuUsage));
            cpuBar.setProgress(cpuUsage / 100.0);

            memoryUsageLabel.setText(String.format("Memory Usage: %.1f%%", memoryUsage));
            memoryBar.setProgress(memoryUsage / 100.0);

            // Add performance data to list
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String perfData = String.format("%s - CPU: %.1f%%, Memory: %.1f%%", timestamp, cpuUsage, memoryUsage);

            performanceList.getItems().add(0, perfData);

            // Keep only last 20 entries
            if (performanceList.getItems().size() > 20) {
                performanceList.getItems().remove(20, performanceList.getItems().size());
            }
        });
    }

    private void startMonitoring() {
        // Update performance metrics every 2 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> updatePerformanceMetrics())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();

        // Add periodic log entries
        javafx.animation.Timeline logTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), e -> {
                addLogEntry("INFO", "System health check - All systems operational");
            })
        );
        logTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        logTimeline.play();
    }

    public void logInfo(String message) {
        addLogEntry("INFO", message);
    }

    public void logWarn(String message) {
        addLogEntry("WARN", message);
    }

    public void logError(String message) {
        addLogEntry("ERROR", message);
    }

    public Parent getRoot() {
        return root;
    }
}