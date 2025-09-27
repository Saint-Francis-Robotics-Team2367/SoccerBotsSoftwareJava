package com.soccerbots.control.gui;

import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.game.GameTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Duration;

public class GameTimerPanel extends JPanel implements GameTimer.GameTimerListener {
    private static final Logger logger = LoggerFactory.getLogger(GameTimerPanel.class);

    private final GameTimer gameTimer;
    private final ControllerManager controllerManager;

    // UI Components
    private JLabel timeDisplay;
    private JProgressBar progressBar;
    private JButton startButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton resetButton;
    private JSpinner durationSpinner;
    private JLabel stateLabel;
    private JCheckBox emergencyStopOnFinishCheckBox;

    public GameTimerPanel(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
        this.gameTimer = new GameTimer();
        this.gameTimer.addListener(this);

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        updateDisplay();
    }

    private void initializeComponents() {
        setBorder(new TitledBorder("Game Timer"));
        setPreferredSize(new Dimension(380, 180));

        // Time display
        timeDisplay = new JLabel(gameTimer.getFormattedRemainingTime());
        timeDisplay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        timeDisplay.setHorizontalAlignment(SwingConstants.CENTER);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        // Control buttons
        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        resetButton = new JButton("Reset");

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Duration spinner (in minutes)
        durationSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 120, 1));
        durationSpinner.setToolTipText("Game duration in minutes");

        // State label
        stateLabel = new JLabel("STOPPED");
        stateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        stateLabel.setForeground(Color.GRAY);

        // Emergency stop option
        emergencyStopOnFinishCheckBox = new JCheckBox("Emergency stop when finished", true);
        emergencyStopOnFinishCheckBox.setToolTipText("Automatically trigger emergency stop when timer expires");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Top panel with time display and progress
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(timeDisplay, BorderLayout.CENTER);
        topPanel.add(progressBar, BorderLayout.SOUTH);

        // Middle panel with duration setting
        JPanel durationPanel = new JPanel(new FlowLayout());
        durationPanel.add(new JLabel("Duration (min):"));
        durationPanel.add(durationSpinner);
        durationPanel.add(Box.createHorizontalStrut(10));
        durationPanel.add(stateLabel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);

        // Bottom panel with options
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.add(emergencyStopOnFinishCheckBox);

        // Main layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(durationPanel, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(optionsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(this::handleStart);
        pauseButton.addActionListener(this::handlePause);
        stopButton.addActionListener(this::handleStop);
        resetButton.addActionListener(this::handleReset);

        durationSpinner.addChangeListener(e -> {
            if (gameTimer.getCurrentState() == GameTimer.TimerState.STOPPED) {
                int minutes = (Integer) durationSpinner.getValue();
                gameTimer.setGameDuration(Duration.ofMinutes(minutes));
            }
        });
    }

    private void handleStart(ActionEvent e) {
        if (gameTimer.getCurrentState() == GameTimer.TimerState.STOPPED) {
            // Set duration before starting
            int minutes = (Integer) durationSpinner.getValue();
            gameTimer.setGameDuration(Duration.ofMinutes(minutes));
        }
        gameTimer.startTimer();
    }

    private void handlePause(ActionEvent e) {
        gameTimer.pauseTimer();
    }

    private void handleStop(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to stop the game timer?",
            "Confirm Stop",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            gameTimer.stopTimer();
        }
    }

    private void handleReset(ActionEvent e) {
        gameTimer.resetTimer();
    }

    @Override
    public void onTimerStateChanged(GameTimer.TimerState newState, Duration remainingTime) {
        SwingUtilities.invokeLater(() -> {
            updateDisplay();
            updateButtonStates(newState);
            updateStateLabel(newState);
        });
    }

    @Override
    public void onTimerFinished() {
        SwingUtilities.invokeLater(() -> {
            // Show notification
            JOptionPane.showMessageDialog(
                this,
                "Game time has expired!",
                "Time's Up!",
                JOptionPane.WARNING_MESSAGE
            );

            // Trigger emergency stop if enabled
            if (emergencyStopOnFinishCheckBox.isSelected()) {
                controllerManager.activateEmergencyStop();
                logger.info("Emergency stop triggered by game timer expiration");
            }
        });
    }

    @Override
    public void onTimerTick(Duration remainingTime) {
        SwingUtilities.invokeLater(this::updateDisplay);
    }

    private void updateDisplay() {
        timeDisplay.setText(gameTimer.getFormattedRemainingTime());

        double progress = gameTimer.getProgressPercentage();
        progressBar.setValue((int) progress);

        GameTimer.TimerState state = gameTimer.getCurrentState();
        String progressText = "";

        switch (state) {
            case STOPPED:
                progressText = "Ready";
                progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
                break;
            case RUNNING:
                progressText = String.format("%.1f%% - %s elapsed",
                    progress, gameTimer.getFormattedElapsedTime());
                progressBar.setForeground(Color.GREEN);
                break;
            case PAUSED:
                progressText = String.format("PAUSED - %.1f%%", progress);
                progressBar.setForeground(Color.ORANGE);
                break;
            case FINISHED:
                progressText = "FINISHED";
                progressBar.setForeground(Color.RED);
                break;
        }

        progressBar.setString(progressText);

        // Update time display color
        if (state == GameTimer.TimerState.RUNNING) {
            long totalSeconds = gameTimer.getRemainingTime().getSeconds();
            if (totalSeconds <= 30) {
                timeDisplay.setForeground(Color.RED);
            } else if (totalSeconds <= 60) {
                timeDisplay.setForeground(Color.ORANGE);
            } else {
                timeDisplay.setForeground(Color.BLACK);
            }
        } else {
            timeDisplay.setForeground(Color.BLACK);
        }
    }

    private void updateButtonStates(GameTimer.TimerState state) {
        durationSpinner.setEnabled(state == GameTimer.TimerState.STOPPED);

        switch (state) {
            case STOPPED:
                startButton.setEnabled(true);
                startButton.setText("Start");
                pauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                resetButton.setEnabled(true);
                break;
            case RUNNING:
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
                stopButton.setEnabled(true);
                resetButton.setEnabled(false);
                break;
            case PAUSED:
                startButton.setEnabled(true);
                startButton.setText("Resume");
                pauseButton.setEnabled(false);
                stopButton.setEnabled(true);
                resetButton.setEnabled(true);
                break;
            case FINISHED:
                startButton.setEnabled(false);
                pauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                resetButton.setEnabled(true);
                break;
        }
    }

    private void updateStateLabel(GameTimer.TimerState state) {
        stateLabel.setText(state.toString());

        switch (state) {
            case STOPPED:
                stateLabel.setForeground(Color.GRAY);
                break;
            case RUNNING:
                stateLabel.setForeground(Color.GREEN);
                break;
            case PAUSED:
                stateLabel.setForeground(Color.ORANGE);
                break;
            case FINISHED:
                stateLabel.setForeground(Color.RED);
                break;
        }
    }

    public GameTimer getGameTimer() {
        return gameTimer;
    }

    public void shutdown() {
        if (gameTimer != null) {
            gameTimer.shutdown();
        }
    }
}