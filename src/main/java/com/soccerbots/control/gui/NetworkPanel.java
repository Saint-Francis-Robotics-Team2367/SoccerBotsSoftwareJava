package com.soccerbots.control.gui;

import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.gui.theme.Theme;
import com.soccerbots.control.gui.theme.ThemedComponent;
import com.soccerbots.control.gui.monitoring.NetworkTrafficGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class NetworkPanel extends ThemedComponent {
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
    private NetworkTrafficGraph trafficGraph;
    private JLabel connectionDetailsLabel;
    private JProgressBar signalStrengthBar;
    private Timer networkStatsTimer;

    public NetworkPanel(NetworkManager networkManager) {
        super(); // This will call applyTheme, but we need to handle null components
        this.networkManager = networkManager;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        updateUIElements();
        // Apply theme again after components are initialized
        applyTheme(themeManager.getCurrentTheme());
    }

    private void initializeComponents() {
        setPreferredSize(new Dimension(400, 450));

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

        // Enhanced monitoring components
        trafficGraph = new NetworkTrafficGraph();
        connectionDetailsLabel = new JLabel("<html>IP: N/A<br>Signal: N/A<br>Speed: N/A</html>");

        signalStrengthBar = new JProgressBar(0, 100);
        signalStrengthBar.setStringPainted(true);
        signalStrengthBar.setString("Signal: 0%");
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

        // Status and monitoring section
        gbc.gridy = 7;
        add(statusLabel, gbc);

        // Signal strength
        gbc.gridy = 8;
        add(new JLabel("Signal Strength:"), gbc);
        gbc.gridy = 9;
        add(signalStrengthBar, gbc);

        // Connection details
        gbc.gridy = 10;
        add(connectionDetailsLabel, gbc);

        // Network traffic graph
        gbc.gridy = 11; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        add(trafficGraph, gbc);
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

        // Network statistics timer
        networkStatsTimer = new Timer(1000, e -> updateNetworkStats());
        networkStatsTimer.start();
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
        Theme theme = themeManager.getCurrentTheme();

        if (networkManager.isNetworkActive()) {
            String status = "Network: Connected";
            if (networkManager.isHostingNetwork()) {
                status += " (Hosting)";
                updateConnectionDetails("Host Mode", "100%", "N/A");
                signalStrengthBar.setValue(100);
                signalStrengthBar.setString("Signal: 100% (Host)");
            } else {
                status += " (" + networkManager.getCurrentSSID() + ")";
                // Simulate signal strength and connection info
                int signalStrength = 75 + (int)(Math.random() * 25); // 75-100%
                updateConnectionDetails(getLocalIPAddress(), signalStrength + "%", "54 Mbps");
                signalStrengthBar.setValue(signalStrength);
                signalStrengthBar.setString("Signal: " + signalStrength + "%");
            }
            statusLabel.setText(status);
            statusLabel.setForeground(theme.getColor(Theme.STATUS_CONNECTED));
        } else {
            statusLabel.setText("Network: Disconnected");
            statusLabel.setForeground(theme.getColor(Theme.STATUS_DISCONNECTED));
            updateConnectionDetails("N/A", "N/A", "N/A");
            signalStrengthBar.setValue(0);
            signalStrengthBar.setString("Signal: 0%");
        }

        updateStartStopButton();
    }

    private void updateConnectionDetails(String ip, String signal, String speed) {
        connectionDetailsLabel.setText("<html>IP: " + ip + "<br>Signal: " + signal + "<br>Speed: " + speed + "</html>");
    }

    private String getLocalIPAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void updateNetworkStats() {
        if (networkManager.isNetworkActive()) {
            // Simulate network traffic data
            double sent = Math.random() * 1024; // Random bytes
            double received = Math.random() * 2048;
            trafficGraph.addDataPoint(sent, received);
        }
    }

    @Override
    protected void applyTheme(Theme theme) {
        setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(theme.getColor(Theme.BORDER)),
            "Network Configuration",
            0, 0,
            themeManager.getFont(-1),
            theme.getColor(Theme.FOREGROUND)
        ));

        // Apply theme to all components
        applyThemeToComponent(this, theme);

        // Special handling for signal strength bar (only if initialized)
        if (signalStrengthBar != null) {
            signalStrengthBar.setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
            signalStrengthBar.setForeground(theme.getColor(Theme.ACCENT));
        }
    }

    @Override
    public void removeNotify() {
        if (networkStatsTimer != null) {
            networkStatsTimer.stop();
        }
        super.removeNotify();
    }
}