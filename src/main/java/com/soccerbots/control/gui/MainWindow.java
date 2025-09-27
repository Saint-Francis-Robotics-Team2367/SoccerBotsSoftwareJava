package com.soccerbots.control.gui;

import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.robot.RobotManager;
import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.gui.monitoring.RobotStatusPanel;
import com.soccerbots.control.gui.monitoring.SystemLogPanel;
import com.soccerbots.control.gui.monitoring.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    private NetworkManager networkManager;
    private RobotManager robotManager;
    private ControllerManager controllerManager;

    private NetworkPanel networkPanel;
    private RobotPanel robotPanel;
    private ControllerPanel controllerPanel;
    private StatusPanel statusPanel;
    private GameTimerPanel gameTimerPanel;
    private JButton emergencyStopButton;
    private RobotStatusPanel robotStatusPanel;
    private SettingsPanel settingsPanel;
    private SystemLogPanel systemLogPanel;
    private PerformanceMonitor performanceMonitor;
    private JTabbedPane rightTabbedPane;
    
    public MainWindow() {
        initializeManagers();
        initializeGUI();
        setupEventHandlers();
    }
    
    private void initializeManagers() {
        networkManager = new NetworkManager();
        robotManager = new RobotManager(networkManager);
        controllerManager = new ControllerManager(robotManager);
    }
    
    private void initializeGUI() {
        setTitle("SoccerBots Robotics Control System v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        createMenuBar();
        createMainPanels();
        layoutComponents();

        statusPanel = new StatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        updateStatusConnections();

        // Add initial log entries
        SystemLogPanel.logInfo("Application started successfully");
        SystemLogPanel.logInfo("Managers initialized");
        SystemLogPanel.logInfo("GUI components loaded");
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem settingsItem = new JMenuItem("Preferences");
        settingsItem.addActionListener(e -> showSettingsDialog());
        settingsMenu.add(settingsItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainPanels() {
        networkPanel = new NetworkPanel(networkManager);
        robotPanel = new RobotPanel(robotManager);
        controllerPanel = new ControllerPanel(controllerManager);
        gameTimerPanel = new GameTimerPanel(controllerManager);
        robotStatusPanel = new RobotStatusPanel();
        settingsPanel = new SettingsPanel();
        systemLogPanel = new SystemLogPanel();
        performanceMonitor = new PerformanceMonitor();

        // Set up static log panel instance for global logging
        SystemLogPanel.setInstance(systemLogPanel);

        // Create emergency stop button
        emergencyStopButton = new JButton("EMERGENCY STOP");
        emergencyStopButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        emergencyStopButton.setPreferredSize(new Dimension(200, 40));
        emergencyStopButton.addActionListener(this::handleEmergencyStop);
    }
    
    private void layoutComponents() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(networkPanel, BorderLayout.NORTH);

        // Create center-left panel for controller and timer
        JPanel centerLeftPanel = new JPanel(new BorderLayout());
        centerLeftPanel.add(controllerPanel, BorderLayout.CENTER);
        centerLeftPanel.add(gameTimerPanel, BorderLayout.SOUTH);
        leftPanel.add(centerLeftPanel, BorderLayout.CENTER);

        // Emergency stop button at bottom of left panel
        JPanel emergencyPanel = new JPanel(new FlowLayout());
        emergencyPanel.add(emergencyStopButton);
        leftPanel.add(emergencyPanel, BorderLayout.SOUTH);

        leftPanel.setPreferredSize(new Dimension(420, 0));

        // Create right tabbed pane for monitoring and settings
        rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("Robots", robotPanel);
        rightTabbedPane.addTab("Robot Status", robotStatusPanel);

        // Create monitoring panel with sub-tabs
        JTabbedPane monitoringTabs = new JTabbedPane();
        monitoringTabs.addTab("System Log", systemLogPanel);
        monitoringTabs.addTab("Performance", performanceMonitor);
        rightTabbedPane.addTab("Monitoring", monitoringTabs);

        rightTabbedPane.addTab("Settings", settingsPanel);
        rightTabbedPane.setPreferredSize(new Dimension(500, 0));

        add(leftPanel, BorderLayout.WEST);
        add(rightTabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
        
        Timer statusUpdateTimer = new Timer(1000, e -> updateStatusConnections());
        statusUpdateTimer.start();
    }

    private void handleEmergencyStop(java.awt.event.ActionEvent e) {
        boolean isCurrentlyActive = controllerManager.isEmergencyStopActive();

        if (isCurrentlyActive) {
            // Deactivate emergency stop
            controllerManager.deactivateEmergencyStop();
            emergencyStopButton.setText("EMERGENCY STOP");
            emergencyStopButton.setBackground(new Color(220, 53, 69));
            logger.info("Emergency stop deactivated by user");
            SystemLogPanel.logInfo("Emergency stop deactivated - normal operation resumed");
        } else {
            // Activate emergency stop
            int result = JOptionPane.showConfirmDialog(
                this,
                "This will immediately stop all robot movement.\nAre you sure?",
                "Emergency Stop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                controllerManager.activateEmergencyStop();
                emergencyStopButton.setText("RESUME CONTROL");
                emergencyStopButton.setBackground(new Color(255, 193, 7));
                logger.warn("Emergency stop activated by user");
                SystemLogPanel.logWarn("Emergency stop activated by user");
            }
        }
    }
    
    private void updateStatusConnections() {
        if (statusPanel != null) {
            statusPanel.updateNetworkStatus(networkManager.isNetworkActive());
            statusPanel.updateRobotCount(robotManager.getConnectedRobotCount());
            statusPanel.updateControllerCount(controllerManager.getConnectedControllerCount());
        }
    }
    
    private void showAboutDialog() {
        String message = "SoccerBots Robotics Control System v1.0\n\n" +
                        "A low-latency control system for ESP32-based soccer robots.\n\n" +
                        "Features:\n" +
                        "• WiFi network management\n" +
                        "• Multiple robot control\n" +
                        "• Controller pairing & mapping\n" +
                        "• Real-time status monitoring\n" +
                        "• Emergency stop control\n" +
                        "• Game timer with auto-stop\n" +
                        "• Customizable themes\n" +
                        "• Network traffic monitoring\n" +
                        "• Robot connectivity status";

        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettingsDialog() {
        rightTabbedPane.setSelectedComponent(settingsPanel);
    }

    
    private void cleanup() {
        logger.info("Shutting down application");
        if (gameTimerPanel != null) {
            gameTimerPanel.shutdown();
        }
        if (controllerManager != null) {
            controllerManager.shutdown();
        }
        if (robotManager != null) {
            robotManager.shutdown();
        }
        if (networkManager != null) {
            networkManager.shutdown();
        }
    }
}