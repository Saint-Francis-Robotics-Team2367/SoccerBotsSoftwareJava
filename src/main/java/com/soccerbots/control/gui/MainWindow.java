package com.soccerbots.control.gui;

import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.robot.RobotManager;
import com.soccerbots.control.controller.ControllerManager;
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
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        createMenuBar();
        createMainPanels();
        layoutComponents();
        
        statusPanel = new StatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        updateStatusConnections();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainPanels() {
        networkPanel = new NetworkPanel(networkManager);
        robotPanel = new RobotPanel(robotManager);
        controllerPanel = new ControllerPanel(controllerManager);
    }
    
    private void layoutComponents() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(networkPanel, BorderLayout.NORTH);
        leftPanel.add(controllerPanel, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        add(leftPanel, BorderLayout.WEST);
        add(robotPanel, BorderLayout.CENTER);
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
                        "• Bluetooth robot configuration\n" +
                        "• Multiple robot control\n" +
                        "• Controller pairing\n" +
                        "• Real-time status monitoring";
        
        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cleanup() {
        logger.info("Shutting down application");
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