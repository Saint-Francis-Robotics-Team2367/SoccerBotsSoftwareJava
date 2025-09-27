package com.soccerbots.control.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

public class SettingsPanel extends JPanel {
    private JCheckBox enableSoundsBox;
    private JCheckBox autoReconnectBox;
    private JCheckBox verboseLoggingBox;
    private JSlider updateRateSlider;
    private JLabel updateRateLabel;
    private JTextField maxRobotsField;
    private JTextField networkTimeoutField;
    private JButton resetButton;
    private JButton exportLogsButton;

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsPanel.class);

    public SettingsPanel() {
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadSettings();
    }

    private void initializeComponents() {
        setPreferredSize(new Dimension(400, 400));
        setBorder(new TitledBorder("Application Settings"));

        // General settings
        enableSoundsBox = new JCheckBox("Enable Sound Effects", true);
        autoReconnectBox = new JCheckBox("Auto-reconnect to Robots", true);
        verboseLoggingBox = new JCheckBox("Verbose Logging", false);

        // Performance settings
        updateRateSlider = new JSlider(10, 100, 60);
        updateRateSlider.setMajorTickSpacing(20);
        updateRateSlider.setMinorTickSpacing(10);
        updateRateSlider.setPaintTicks(true);
        updateRateSlider.setPaintLabels(true);
        updateRateLabel = new JLabel("Update Rate: 60 Hz");

        // Network settings
        maxRobotsField = new JTextField("8", 5);
        networkTimeoutField = new JTextField("5000", 8);

        // Action buttons
        resetButton = new JButton("Reset to Defaults");
        exportLogsButton = new JButton("Export Logs");
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // General Settings section
        JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder(new TitledBorder("General"));
        GridBagConstraints genGbc = new GridBagConstraints();
        genGbc.insets = new Insets(5, 5, 5, 5);
        genGbc.anchor = GridBagConstraints.WEST;

        genGbc.gridx = 0; genGbc.gridy = 0;
        generalPanel.add(enableSoundsBox, genGbc);
        genGbc.gridy = 1;
        generalPanel.add(autoReconnectBox, genGbc);
        genGbc.gridy = 2;
        generalPanel.add(verboseLoggingBox, genGbc);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(generalPanel, gbc);

        // Performance Settings section
        JPanel performancePanel = new JPanel(new GridBagLayout());
        performancePanel.setBorder(new TitledBorder("Performance"));
        GridBagConstraints perfGbc = new GridBagConstraints();
        perfGbc.insets = new Insets(5, 5, 5, 5);
        perfGbc.anchor = GridBagConstraints.WEST;

        perfGbc.gridx = 0; perfGbc.gridy = 0;
        performancePanel.add(updateRateLabel, perfGbc);
        perfGbc.gridy = 1; perfGbc.fill = GridBagConstraints.HORIZONTAL; perfGbc.weightx = 1.0;
        performancePanel.add(updateRateSlider, perfGbc);

        gbc.gridy = 1;
        add(performancePanel, gbc);

        // Network Settings section
        JPanel networkPanel = new JPanel(new GridBagLayout());
        networkPanel.setBorder(new TitledBorder("Network"));
        GridBagConstraints netGbc = new GridBagConstraints();
        netGbc.insets = new Insets(5, 5, 5, 5);
        netGbc.anchor = GridBagConstraints.WEST;

        netGbc.gridx = 0; netGbc.gridy = 0;
        networkPanel.add(new JLabel("Max Robots:"), netGbc);
        netGbc.gridx = 1;
        networkPanel.add(maxRobotsField, netGbc);

        netGbc.gridx = 0; netGbc.gridy = 1;
        networkPanel.add(new JLabel("Network Timeout (ms):"), netGbc);
        netGbc.gridx = 1;
        networkPanel.add(networkTimeoutField, netGbc);

        gbc.gridy = 2;
        add(networkPanel, gbc);

        // Buttons section
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(resetButton);
        buttonPanel.add(exportLogsButton);

        gbc.gridy = 3; gbc.weighty = 1.0; gbc.anchor = GridBagConstraints.SOUTH;
        add(buttonPanel, gbc);
    }

    private void setupEventHandlers() {
        updateRateSlider.addChangeListener(e -> {
            int rate = updateRateSlider.getValue();
            updateRateLabel.setText("Update Rate: " + rate + " Hz");
            saveSettings();
        });

        enableSoundsBox.addActionListener(e -> saveSettings());
        autoReconnectBox.addActionListener(e -> saveSettings());
        verboseLoggingBox.addActionListener(e -> saveSettings());

        maxRobotsField.addActionListener(e -> {
            try {
                int maxRobots = Integer.parseInt(maxRobotsField.getText());
                if (maxRobots < 1 || maxRobots > 32) {
                    throw new NumberFormatException();
                }
                saveSettings();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Max robots must be between 1 and 32");
                maxRobotsField.setText("8");
            }
        });

        networkTimeoutField.addActionListener(e -> {
            try {
                int timeout = Integer.parseInt(networkTimeoutField.getText());
                if (timeout < 1000 || timeout > 30000) {
                    throw new NumberFormatException();
                }
                saveSettings();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Timeout must be between 1000 and 30000 ms");
                networkTimeoutField.setText("5000");
            }
        });

        resetButton.addActionListener(e -> resetToDefaults());
        exportLogsButton.addActionListener(e -> exportLogs());
    }

    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Reset all settings to defaults?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            enableSoundsBox.setSelected(true);
            autoReconnectBox.setSelected(true);
            verboseLoggingBox.setSelected(false);
            updateRateSlider.setValue(60);
            updateRateLabel.setText("Update Rate: 60 Hz");
            maxRobotsField.setText("8");
            networkTimeoutField.setText("5000");
            saveSettings();
        }
    }

    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Logs");
        fileChooser.setSelectedFile(new java.io.File("soccerbots_logs.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.nio.file.Files.write(file.toPath(), "Application logs would be exported here\n".getBytes());
                JOptionPane.showMessageDialog(this, "Logs exported successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to export logs: " + e.getMessage());
            }
        }
    }

    private void saveSettings() {
        prefs.putBoolean("enableSounds", enableSoundsBox.isSelected());
        prefs.putBoolean("autoReconnect", autoReconnectBox.isSelected());
        prefs.putBoolean("verboseLogging", verboseLoggingBox.isSelected());
        prefs.putInt("updateRate", updateRateSlider.getValue());
        prefs.put("maxRobots", maxRobotsField.getText());
        prefs.put("networkTimeout", networkTimeoutField.getText());
    }

    private void loadSettings() {
        enableSoundsBox.setSelected(prefs.getBoolean("enableSounds", true));
        autoReconnectBox.setSelected(prefs.getBoolean("autoReconnect", true));
        verboseLoggingBox.setSelected(prefs.getBoolean("verboseLogging", false));

        int updateRate = prefs.getInt("updateRate", 60);
        updateRateSlider.setValue(updateRate);
        updateRateLabel.setText("Update Rate: " + updateRate + " Hz");

        maxRobotsField.setText(prefs.get("maxRobots", "8"));
        networkTimeoutField.setText(prefs.get("networkTimeout", "5000"));
    }

    // Getters for other components to access settings
    public boolean isSoundsEnabled() {
        return enableSoundsBox.isSelected();
    }

    public boolean isAutoReconnectEnabled() {
        return autoReconnectBox.isSelected();
    }

    public boolean isVerboseLoggingEnabled() {
        return verboseLoggingBox.isSelected();
    }

    public int getUpdateRate() {
        return updateRateSlider.getValue();
    }

    public int getMaxRobots() {
        try {
            return Integer.parseInt(maxRobotsField.getText());
        } catch (NumberFormatException e) {
            return 8;
        }
    }

    public int getNetworkTimeout() {
        try {
            return Integer.parseInt(networkTimeoutField.getText());
        } catch (NumberFormatException e) {
            return 5000;
        }
    }
}