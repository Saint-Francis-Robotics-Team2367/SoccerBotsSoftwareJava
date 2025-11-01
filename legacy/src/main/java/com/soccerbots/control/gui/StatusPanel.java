package com.soccerbots.control.gui;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
    private JLabel networkStatusLabel;
    private JLabel robotCountLabel;
    private JLabel controllerCountLabel;
    private JLabel timestampLabel;
    
    public StatusPanel() {
        initializeComponents();
        layoutComponents();
        startTimestampUpdater();
    }
    
    private void initializeComponents() {
        setBorder(BorderFactory.createEtchedBorder());
        setPreferredSize(new Dimension(0, 30));
        
        networkStatusLabel = new JLabel("Network: Disconnected");
        networkStatusLabel.setForeground(Color.RED);
        
        robotCountLabel = new JLabel("Robots: 0");
        controllerCountLabel = new JLabel("Controllers: 0");
        timestampLabel = new JLabel();
        
        updateTimestamp();
    }
    
    private void layoutComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        add(networkStatusLabel);
        add(new JLabel(" | "));
        add(robotCountLabel);
        add(new JLabel(" | "));
        add(controllerCountLabel);
        add(Box.createHorizontalGlue());
        add(timestampLabel);
    }
    
    public void updateNetworkStatus(boolean isActive) {
        SwingUtilities.invokeLater(() -> {
            if (isActive) {
                networkStatusLabel.setText("Network: Active");
                networkStatusLabel.setForeground(new Color(0, 150, 0));
            } else {
                networkStatusLabel.setText("Network: Disconnected");
                networkStatusLabel.setForeground(Color.RED);
            }
        });
    }
    
    public void updateRobotCount(int count) {
        SwingUtilities.invokeLater(() -> {
            robotCountLabel.setText("Robots: " + count);
        });
    }
    
    public void updateControllerCount(int count) {
        SwingUtilities.invokeLater(() -> {
            controllerCountLabel.setText("Controllers: " + count);
        });
    }
    
    private void updateTimestamp() {
        SwingUtilities.invokeLater(() -> {
            timestampLabel.setText(new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
        });
    }
    
    private void startTimestampUpdater() {
        Timer timer = new Timer(1000, e -> updateTimestamp());
        timer.start();
    }
}