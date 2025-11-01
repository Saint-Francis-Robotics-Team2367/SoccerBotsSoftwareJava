package com.soccerbots.control.gui;

import com.soccerbots.control.robot.Robot;
import com.soccerbots.control.robot.RobotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class WiFiConfigDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(WiFiConfigDialog.class);

    private final RobotManager robotManager;
    private final Robot robot;
    
    private JTextField ssidField;
    private JPasswordField passwordField;
    private JButton configureButton;
    private JButton cancelButton;
    
    public WiFiConfigDialog(Window parent, RobotManager robotManager, Robot robot) {
        super(parent, "Configure WiFi for " + robot.getName(), ModalityType.APPLICATION_MODAL);
        this.robotManager = robotManager;
        this.robot = robot;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        ssidField = new JTextField(20);
        passwordField = new JPasswordField(20);
        configureButton = new JButton("Configure");
        cancelButton = new JButton("Cancel");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Robot:"), gbc);
        gbc.gridx = 1;
        JLabel robotLabel = new JLabel(robot.getName() + " (" + robot.getIpAddress() + ")");
        robotLabel.setFont(robotLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(robotLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("SSID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(ssidField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(configureButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        configureButton.addActionListener(this::handleConfigure);
        cancelButton.addActionListener(e -> dispose());
        
        // Allow Enter key to trigger configure
        getRootPane().setDefaultButton(configureButton);
    }
    
    private void handleConfigure(ActionEvent e) {
        String ssid = ssidField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (ssid.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "SSID cannot be empty.", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, 
                "Password must be at least 8 characters long.", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        configureButton.setEnabled(false);
        configureButton.setText("Configuring...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // ESP32 robots are pre-configured for WATCHTOWER network
                // WiFi configuration is not supported for ESP32 robots
                logger.info("WiFi configuration not supported for ESP32 robots");
                return null;
            }
            
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(WiFiConfigDialog.this,
                    "ESP32 robots are pre-configured to connect to the WATCHTOWER network.\nWiFi configuration is not supported for ESP32 robots.",
                    "ESP32 Network Info", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        };
        
        worker.execute();
    }
}