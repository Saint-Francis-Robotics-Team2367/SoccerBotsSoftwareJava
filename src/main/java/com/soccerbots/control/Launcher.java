package com.soccerbots.control;

import com.soccerbots.control.launcher.SplashScreen;
import com.soccerbots.control.launcher.LauncherWindow;

import javax.swing.*;

/**
 * Main entry point for the SoccerBots Control System
 * Shows splash screen followed by launcher window
 */
public class Launcher {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show splash screen
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.show(() -> {
                // After splash screen completes, show launcher
                LauncherWindow launcher = new LauncherWindow();
                launcher.setVisible(true);
            });
        });
    }
}
