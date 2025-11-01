package com.soccerbots.control.gui.monitoring;

import com.soccerbots.control.gui.theme.Theme;
import com.soccerbots.control.gui.theme.ThemedComponent;
import com.soccerbots.control.robot.Robot;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class RobotStatusPanel extends ThemedComponent {
    private static final DecimalFormat PING_FORMAT = new DecimalFormat("#0.0");
    private static final int PING_TIMEOUT_MS = 5000;
    private static final int SIGNAL_BARS = 4;

    private final Map<String, RobotStatus> robotStatuses;
    private final JScrollPane scrollPane;
    private final JPanel robotListPanel;

    public RobotStatusPanel() {
        super(); // This will call applyTheme, but we need to handle null components
        robotStatuses = new HashMap<>();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 200));

        robotListPanel = new JPanel();
        robotListPanel.setLayout(new BoxLayout(robotListPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(robotListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Apply theme again after components are initialized
        applyTheme(themeManager.getCurrentTheme());

        // Update timer
        Timer updateTimer = new Timer(1000, e -> updateDisplay());
        updateTimer.start();
    }

    public void updateRobotStatus(String robotId, String ipAddress, double pingMs, int signalStrength,
                                 boolean isConnected, long lastSeen) {
        RobotStatus status = robotStatuses.computeIfAbsent(robotId, k -> new RobotStatus(robotId));
        status.update(ipAddress, pingMs, signalStrength, isConnected, lastSeen);

        SwingUtilities.invokeLater(this::rebuildDisplay);
    }

    public void removeRobot(String robotId) {
        robotStatuses.remove(robotId);
        SwingUtilities.invokeLater(this::rebuildDisplay);
    }

    private void updateDisplay() {
        long currentTime = System.currentTimeMillis();

        // Update connection status based on last seen time
        for (RobotStatus status : robotStatuses.values()) {
            if (currentTime - status.lastSeen > PING_TIMEOUT_MS) {
                status.isConnected = false;
            }
        }

        SwingUtilities.invokeLater(() -> {
            for (Component comp : robotListPanel.getComponents()) {
                if (comp instanceof RobotStatusComponent) {
                    comp.repaint();
                }
            }
        });
    }

    private void rebuildDisplay() {
        robotListPanel.removeAll();

        for (RobotStatus status : robotStatuses.values()) {
            RobotStatusComponent component = new RobotStatusComponent(status);
            robotListPanel.add(component);
            robotListPanel.add(Box.createVerticalStrut(5));
        }

        if (robotStatuses.isEmpty()) {
            JLabel noRobotsLabel = new JLabel("No robots connected", SwingConstants.CENTER);
            Theme theme = themeManager.getCurrentTheme();
            noRobotsLabel.setForeground(theme.getColor(Theme.FOREGROUND));
            noRobotsLabel.setFont(themeManager.getFont(0));
            robotListPanel.add(noRobotsLabel);
        }

        robotListPanel.revalidate();
        robotListPanel.repaint();
    }

    @Override
    protected void applyTheme(Theme theme) {
        setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(theme.getColor(Theme.BORDER)),
            "Robot Status",
            0, 0,
            themeManager.getFont(-1),
            theme.getColor(Theme.FOREGROUND)
        ));

        // Apply theme to components only if they exist
        if (robotListPanel != null) {
            robotListPanel.setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        }
        if (scrollPane != null) {
            scrollPane.setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
            scrollPane.getViewport().setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        }
    }

    private static class RobotStatus {
        final String robotId;
        String ipAddress = "";
        double pingMs = -1;
        int signalStrength = 0; // 0-100
        boolean isConnected = false;
        long lastSeen = 0;
        long firstSeen = System.currentTimeMillis();

        RobotStatus(String robotId) {
            this.robotId = robotId;
        }

        void update(String ipAddress, double pingMs, int signalStrength, boolean isConnected, long lastSeen) {
            this.ipAddress = ipAddress;
            this.pingMs = pingMs;
            this.signalStrength = Math.max(0, Math.min(100, signalStrength));
            this.isConnected = isConnected;
            this.lastSeen = lastSeen;
        }

        String getConnectionTime() {
            long connectedSeconds = (System.currentTimeMillis() - firstSeen) / 1000;
            long minutes = connectedSeconds / 60;
            long seconds = connectedSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }

        int getSignalBars() {
            return Math.min(SIGNAL_BARS, (signalStrength * SIGNAL_BARS) / 100);
        }
    }

    private class RobotStatusComponent extends JPanel {
        private final RobotStatus status;

        RobotStatusComponent(RobotStatus status) {
            this.status = status;
            setPreferredSize(new Dimension(280, 60));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Theme theme = themeManager.getCurrentTheme();
            int width = getWidth();
            int height = getHeight();

            // Background
            Color bgColor = status.isConnected ?
                theme.getColor(Theme.PANEL_BACKGROUND) :
                new Color(theme.getColor(Theme.ERROR).getRed(),
                         theme.getColor(Theme.ERROR).getGreen(),
                         theme.getColor(Theme.ERROR).getBlue(), 50);
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, width, height);

            // Border
            g2d.setColor(theme.getColor(Theme.BORDER));
            g2d.drawRect(0, 0, width - 1, height - 1);

            // Connection indicator
            int indicatorSize = 12;
            Color indicatorColor = status.isConnected ?
                theme.getColor(Theme.STATUS_CONNECTED) :
                theme.getColor(Theme.STATUS_DISCONNECTED);
            g2d.setColor(indicatorColor);
            g2d.fill(new Ellipse2D.Float(10, 10, indicatorSize, indicatorSize));

            // Robot ID
            g2d.setColor(theme.getColor(Theme.FOREGROUND));
            g2d.setFont(themeManager.getFont(Font.BOLD, 0));
            g2d.drawString("Robot " + status.robotId, 30, 20);

            // IP Address
            g2d.setFont(themeManager.getFont(-1));
            g2d.drawString(status.ipAddress, 30, 35);

            // Connection time
            g2d.drawString("Connected: " + status.getConnectionTime(), 30, 50);

            // Ping
            if (status.isConnected && status.pingMs >= 0) {
                String pingText = PING_FORMAT.format(status.pingMs) + "ms";
                Color pingColor = status.pingMs < 50 ? theme.getColor(Theme.SUCCESS) :
                                 status.pingMs < 100 ? theme.getColor(Theme.WARNING) :
                                 theme.getColor(Theme.ERROR);
                g2d.setColor(pingColor);
                g2d.drawString(pingText, width - 80, 20);
            }

            // Signal strength bars
            if (status.isConnected) {
                drawSignalBars(g2d, theme, width - 80, 25, status.getSignalBars());
            }

            g2d.dispose();
        }

        private void drawSignalBars(Graphics2D g2d, Theme theme, int x, int y, int bars) {
            int barWidth = 4;
            int barSpacing = 2;
            int maxHeight = 20;

            for (int i = 0; i < SIGNAL_BARS; i++) {
                int barHeight = maxHeight * (i + 1) / SIGNAL_BARS;
                Color barColor = i < bars ?
                    (bars >= 3 ? theme.getColor(Theme.SUCCESS) :
                     bars >= 2 ? theme.getColor(Theme.WARNING) :
                     theme.getColor(Theme.ERROR)) :
                    theme.getColor(Theme.BORDER);

                g2d.setColor(barColor);
                g2d.fillRect(x + i * (barWidth + barSpacing), y + maxHeight - barHeight,
                           barWidth, barHeight);
            }
        }
    }
}