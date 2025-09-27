package com.soccerbots.control;

import com.soccerbots.control.gui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class RoboticsControlApp {
    private static final Logger logger = LoggerFactory.getLogger(RoboticsControlApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting SoccerBots Robotics Control System");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.warn("Could not set system look and feel", e);
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                logger.info("Main window initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize main window", e);
                System.exit(1);
            }
        });
    }
}