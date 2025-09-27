package com.soccerbots.control.gui;

import com.soccerbots.control.controller.GameController;
import com.soccerbots.control.controller.ControllerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControllerMappingDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMappingDialog.class);

    private final GameController gameController;
    private Timer updateTimer;

    // Input display components
    private JProgressBar leftStickXBar, leftStickYBar;
    private JProgressBar rightStickXBar, rightStickYBar;
    private JProgressBar leftTriggerBar, rightTriggerBar;
    private JLabel dPadLabel;
    private JLabel[] buttonLabels;
    private JLabel[] buttonValues;

    // Movement output display
    private JLabel forwardLabel, sidewaysLabel, rotationLabel;
    private JLabel hasMovementLabel;

    public ControllerMappingDialog(Window parent, GameController gameController) {
        super(parent, "Controller Mapping - " + gameController.getName(), ModalityType.MODELESS);
        this.gameController = gameController;

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        startUpdateTimer();

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeComponents() {
        // Analog stick bars (-100 to 100)
        leftStickXBar = createProgressBar(-100, 100, "Left Stick X");
        leftStickYBar = createProgressBar(-100, 100, "Left Stick Y");
        rightStickXBar = createProgressBar(-100, 100, "Right Stick X");
        rightStickYBar = createProgressBar(-100, 100, "Right Stick Y");

        // Trigger bars (0 to 100)
        leftTriggerBar = createProgressBar(0, 100, "Left Trigger");
        rightTriggerBar = createProgressBar(0, 100, "Right Trigger");

        // D-Pad display
        dPadLabel = new JLabel("D-Pad: None");
        dPadLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Button displays (16 buttons max)
        buttonLabels = new JLabel[16];
        buttonValues = new JLabel[16];
        for (int i = 0; i < 16; i++) {
            buttonLabels[i] = new JLabel("Button " + i + ":");
            buttonValues[i] = new JLabel("Released");
            buttonValues[i].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            buttonValues[i].setForeground(Color.GRAY);
        }

        // Movement output displays
        forwardLabel = new JLabel("Forward: 0.00");
        sidewaysLabel = new JLabel("Sideways: 0.00");
        rotationLabel = new JLabel("Rotation: 0.00");
        hasMovementLabel = new JLabel("Has Movement: false");

        Font outputFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
        forwardLabel.setFont(outputFont);
        sidewaysLabel.setFont(outputFont);
        rotationLabel.setFont(outputFont);
        hasMovementLabel.setFont(outputFont);
    }

    private JProgressBar createProgressBar(int min, int max, String name) {
        JProgressBar bar = new JProgressBar(min, max);
        bar.setStringPainted(true);
        bar.setValue(0);
        bar.setString(name + ": 0");
        return bar;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Analog sticks panel
        JPanel sticksPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        sticksPanel.setBorder(new TitledBorder("Analog Sticks"));
        sticksPanel.add(leftStickXBar);
        sticksPanel.add(leftStickYBar);
        sticksPanel.add(rightStickXBar);
        sticksPanel.add(rightStickYBar);
        mainPanel.add(sticksPanel);

        // Triggers panel
        JPanel triggersPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        triggersPanel.setBorder(new TitledBorder("Triggers"));
        triggersPanel.add(leftTriggerBar);
        triggersPanel.add(rightTriggerBar);
        mainPanel.add(triggersPanel);

        // D-Pad panel
        JPanel dPadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dPadPanel.setBorder(new TitledBorder("D-Pad"));
        dPadPanel.add(dPadLabel);
        mainPanel.add(dPadPanel);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(8, 4, 5, 2));
        buttonsPanel.setBorder(new TitledBorder("Buttons"));
        for (int i = 0; i < 16; i++) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            buttonPanel.add(buttonLabels[i]);
            buttonPanel.add(buttonValues[i]);
            buttonsPanel.add(buttonPanel);
        }
        mainPanel.add(buttonsPanel);

        // Movement output panel
        JPanel movementPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        movementPanel.setBorder(new TitledBorder("Robot Movement Commands"));
        movementPanel.add(forwardLabel);
        movementPanel.add(sidewaysLabel);
        movementPanel.add(rotationLabel);
        movementPanel.add(hasMovementLabel);
        mainPanel.add(movementPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (updateTimer != null) {
                    updateTimer.stop();
                }
            }
        });
    }

    private void startUpdateTimer() {
        updateTimer = new Timer(50, e -> updateDisplay()); // 20 FPS update rate
        updateTimer.start();
    }

    private void updateDisplay() {
        if (gameController == null) {
            return;
        }

        ControllerInput input = gameController.getLastInput();
        if (input == null) {
            return;
        }

        // Update analog sticks
        updateProgressBar(leftStickXBar, input.getLeftStickX(), "Left Stick X");
        updateProgressBar(leftStickYBar, input.getLeftStickY(), "Left Stick Y");
        updateProgressBar(rightStickXBar, input.getRightStickX(), "Right Stick X");
        updateProgressBar(rightStickYBar, input.getRightStickY(), "Right Stick Y");

        // Update triggers
        updateProgressBar(leftTriggerBar, input.getLeftTrigger(), "Left Trigger");
        updateProgressBar(rightTriggerBar, input.getRightTrigger(), "Right Trigger");

        // Update D-Pad
        String dPadText = formatDPad(input.getDPad());
        dPadLabel.setText("D-Pad: " + dPadText);

        // Update buttons
        for (int i = 0; i < 16; i++) {
            boolean pressed = input.getButton(i);
            buttonValues[i].setText(pressed ? "Pressed" : "Released");
            buttonValues[i].setForeground(pressed ? Color.GREEN : Color.GRAY);
        }

        // Update movement output
        forwardLabel.setText(String.format("Forward: %.2f", input.getForward()));
        sidewaysLabel.setText(String.format("Sideways: %.2f", input.getSideways()));
        rotationLabel.setText(String.format("Rotation: %.2f", input.getRotation()));
        hasMovementLabel.setText("Has Movement: " + input.hasMovement());
        hasMovementLabel.setForeground(input.hasMovement() ? Color.GREEN : Color.GRAY);
    }

    private void updateProgressBar(JProgressBar bar, float value, String name) {
        int intValue = Math.round(value * 100);
        bar.setValue(intValue);
        bar.setString(String.format("%s: %.2f", name, value));

        // Color coding for better visual feedback
        if (Math.abs(value) > 0.1f) {
            bar.setForeground(Color.BLUE);
        } else {
            bar.setForeground(UIManager.getColor("ProgressBar.foreground"));
        }
    }

    private String formatDPad(float dPadValue) {
        if (dPadValue == 0.0f) return "Center";
        if (dPadValue == 0.25f) return "Right";
        if (dPadValue == 0.5f) return "Down";
        if (dPadValue == 0.75f) return "Left";
        if (dPadValue == 1.0f) return "Up";
        return String.format("%.2f", dPadValue);
    }

    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.dispose();
    }
}