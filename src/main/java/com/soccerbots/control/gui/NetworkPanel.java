package com.soccerbots.control.gui;

import com.soccerbots.control.network.NetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class NetworkPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(NetworkPanel.class);
    
    private final NetworkManager networkManager;
    
    private JRadioButton hostNetworkRadio;
    private JRadioButton connectNetworkRadio;
    private JTextField hostSSIDField;
    private JPasswordField hostPasswordField;
    private JComboBox<String> availableNetworksCombo;
    private JPasswordField connectPasswordField;
    private JButton startStopButton;
    private JLabel statusLabel;
    private JButton refreshNetworksButton;
    
    public NetworkPanel(NetworkManager networkManager) {
        this.networkManager = networkManager;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        updateUIElements();
    }
    
    private void initializeComponents() {
        setBorder(new TitledBorder("Network Configuration"));
        setPreferredSize(new Dimension(380, 300));
        
        hostNetworkRadio = new JRadioButton("Host Own Network", true);
        connectNetworkRadio = new JRadioButton("Connect to Existing Network");
        
        ButtonGroup networkModeGroup = new ButtonGroup();
        networkModeGroup.add(hostNetworkRadio);
        networkModeGroup.add(connectNetworkRadio);
        
        hostSSIDField = new JTextField("SoccerBots_Network", 15);
        hostPasswordField = new JPasswordField("soccerbots123", 15);
        
        availableNetworksCombo = new JComboBox<>();
        connectPasswordField = new JPasswordField(15);
        
        startStopButton = new JButton("Start Network");
        refreshNetworksButton = new JButton("Refresh");
        
        statusLabel = new JLabel("Network: Disconnected");
        statusLabel.setForeground(Color.RED);
    }
    
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Network mode selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(hostNetworkRadio, gbc);
        
        gbc.gridy = 1;
        add(connectNetworkRadio, gbc);
        
        // Host network configuration
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("SSID:"), gbc);
        gbc.gridx = 1;
        add(hostSSIDField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(hostPasswordField, gbc);
        
        // Connect to network configuration
        gbc.gridy = 4; gbc.gridx = 0;
        add(new JLabel("Network:"), gbc);
        gbc.gridx = 1;
        JPanel networkSelectPanel = new JPanel(new BorderLayout());
        networkSelectPanel.add(availableNetworksCombo, BorderLayout.CENTER);
        networkSelectPanel.add(refreshNetworksButton, BorderLayout.EAST);
        add(networkSelectPanel, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(connectPasswordField, gbc);
        
        // Control buttons
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(startStopButton, gbc);
        
        // Status
        gbc.gridy = 7;
        add(statusLabel, gbc);
    }
    
    private void setupEventHandlers() {
        hostNetworkRadio.addActionListener(e -> updateUIElements());
        connectNetworkRadio.addActionListener(e -> updateUIElements());
        
        startStopButton.addActionListener(this::handleStartStopAction);
        refreshNetworksButton.addActionListener(e -> refreshAvailableNetworks());
        
        // Initial network scan
        SwingUtilities.invokeLater(this::refreshAvailableNetworks);
        
        // Status update timer
        Timer statusTimer = new Timer(2000, e -> updateNetworkStatus());
        statusTimer.start();
    }
    
    private void updateUIElements() {
        boolean hostMode = hostNetworkRadio.isSelected();
        
        hostSSIDField.setEnabled(hostMode);
        hostPasswordField.setEnabled(hostMode);
        
        availableNetworksCombo.setEnabled(!hostMode);
        connectPasswordField.setEnabled(!hostMode);
        refreshNetworksButton.setEnabled(!hostMode);
        
        updateStartStopButton();
    }
    
    private void updateStartStopButton() {
        if (networkManager.isNetworkActive()) {
            startStopButton.setText("Stop Network");
            startStopButton.setBackground(new Color(255, 200, 200));
        } else {
            startStopButton.setText("Start Network");
            startStopButton.setBackground(new Color(200, 255, 200));
        }
    }
    
    private void handleStartStopAction(ActionEvent e) {
        if (networkManager.isNetworkActive()) {
            stopNetwork();
        } else {
            startNetwork();
        }
    }
    
    private void startNetwork() {
        startStopButton.setEnabled(false);
        startStopButton.setText("Starting...");
        
        SwingUtilities.invokeLater(() -> {
            boolean success = false;
            
            try {
                if (hostNetworkRadio.isSelected()) {
                    String ssid = hostSSIDField.getText().trim();
                    String password = new String(hostPasswordField.getPassword());
                    
                    if (ssid.isEmpty() || password.length() < 8) {
                        JOptionPane.showMessageDialog(this, 
                            "SSID cannot be empty and password must be at least 8 characters long.", 
                            "Invalid Configuration", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    success = networkManager.startHostedNetwork(ssid, password);
                } else {
                    String selectedNetwork = (String) availableNetworksCombo.getSelectedItem();
                    String password = new String(connectPasswordField.getPassword());
                    
                    if (selectedNetwork == null || selectedNetwork.isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "Please select a network to connect to.", 
                            "No Network Selected", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    success = networkManager.connectToNetwork(selectedNetwork, password);
                }
                
                if (success) {
                    logger.info("Network operation successful");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to start/connect to network. Check your configuration and try again.", 
                        "Network Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                logger.error("Network operation failed", ex);
                JOptionPane.showMessageDialog(this, 
                    "Network operation failed: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                startStopButton.setEnabled(true);
                updateStartStopButton();
                updateNetworkStatus();
            }
        });
    }
    
    private void stopNetwork() {
        startStopButton.setEnabled(false);
        startStopButton.setText("Stopping...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                if (networkManager.isHostingNetwork()) {
                    networkManager.stopHostedNetwork();
                }
                logger.info("Network stopped");
            } catch (Exception ex) {
                logger.error("Failed to stop network", ex);
            } finally {
                startStopButton.setEnabled(true);
                updateStartStopButton();
                updateNetworkStatus();
            }
        });
    }
    
    private void refreshAvailableNetworks() {
        refreshNetworksButton.setEnabled(false);
        refreshNetworksButton.setText("Scanning...");
        
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return networkManager.scanAvailableNetworks();
            }
            
            @Override
            protected void done() {
                try {
                    List<String> networks = get();
                    availableNetworksCombo.removeAllItems();
                    for (String network : networks) {
                        availableNetworksCombo.addItem(network);
                    }
                    if (!networks.isEmpty()) {
                        availableNetworksCombo.setSelectedIndex(0);
                    }
                    logger.info("Refreshed network list: {} networks found", networks.size());
                } catch (Exception e) {
                    logger.error("Failed to refresh networks", e);
                } finally {
                    refreshNetworksButton.setEnabled(true);
                    refreshNetworksButton.setText("Refresh");
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateNetworkStatus() {
        if (networkManager.isNetworkActive()) {
            String status = "Network: Connected";
            if (networkManager.isHostingNetwork()) {
                status += " (Hosting)";
            } else {
                status += " (" + networkManager.getCurrentSSID() + ")";
            }
            statusLabel.setText(status);
            statusLabel.setForeground(new Color(0, 150, 0));
        } else {
            statusLabel.setText("Network: Disconnected");
            statusLabel.setForeground(Color.RED);
        }
        
        updateStartStopButton();
    }
}