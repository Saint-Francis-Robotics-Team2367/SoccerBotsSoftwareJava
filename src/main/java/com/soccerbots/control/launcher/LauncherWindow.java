package com.soccerbots.control.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Main launcher window with options to start Host/DriverStation or Simulator
 */
public class LauncherWindow extends JFrame {
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;
    private static final Color BG_COLOR = new Color(15, 20, 30);
    private static final Color ACCENT_COLOR = new Color(0, 255, 200);
    private static final Color SECONDARY_COLOR = new Color(100, 150, 255);
    private static final Color BUTTON_BG = new Color(25, 35, 50);
    private static final Color BUTTON_HOVER = new Color(35, 50, 70);

    public LauncherWindow() {
        setTitle("SoccerBots Control System - Launcher");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(BG_COLOR);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);

                // Grid pattern
                drawGrid(g2d);

                // Decorative corner elements
                drawCornerElements(g2d);
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center buttons
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(30, 40, 60, 30));
        int gridSize = 25;

        for (int x = 0; x < WIDTH; x += gridSize) {
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += gridSize) {
            g2d.drawLine(0, y, WIDTH, y);
        }
    }

    private void drawCornerElements(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(ACCENT_COLOR);
        int cornerSize = 40;

        // Top-left
        g2d.drawLine(20, 20, 20 + cornerSize, 20);
        g2d.drawLine(20, 20, 20, 20 + cornerSize);

        // Top-right
        g2d.drawLine(WIDTH - 20, 20, WIDTH - 20 - cornerSize, 20);
        g2d.drawLine(WIDTH - 20, 20, WIDTH - 20, 20 + cornerSize);

        // Bottom-left
        g2d.drawLine(20, HEIGHT - 20, 20 + cornerSize, HEIGHT - 20);
        g2d.drawLine(20, HEIGHT - 20, 20, HEIGHT - 20 - cornerSize);

        // Bottom-right
        g2d.drawLine(WIDTH - 20, HEIGHT - 20, WIDTH - 20 - cornerSize, HEIGHT - 20);
        g2d.drawLine(WIDTH - 20, HEIGHT - 20, WIDTH - 20, HEIGHT - 20 - cornerSize);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("SOCCERBOTS CONTROL SYSTEM");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Select Launch Mode");
        subtitleLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        subtitleLabel.setForeground(SECONDARY_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // Host/DriverStation Button
        JButton hostButton = createStyledButton(
            "HOST / DRIVER STATION",
            "Launch the main control system for robot operation",
            ACCENT_COLOR
        );
        hostButton.addActionListener(e -> launchHost());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(hostButton, gbc);

        // Simulator Button
        JButton simulatorButton = createStyledButton(
            "SIMULATOR",
            "Launch the robot simulator (Coming Soon)",
            SECONDARY_COLOR
        );
        simulatorButton.addActionListener(e -> launchSimulator());
        simulatorButton.setEnabled(false); // Disabled for now

        gbc.gridx = 1;
        panel.add(simulatorButton, gbc);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel versionLabel = new JLabel("v1.0.0 | © 2024 SoccerBots");
        versionLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(100, 120, 150));

        panel.add(versionLabel);

        return panel;
    }

    private JButton createStyledButton(String title, String description, Color accentColor) {
        JButton button = new JButton() {
            private boolean hovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Background
                g2d.setColor(hovered && isEnabled() ? BUTTON_HOVER : BUTTON_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border with accent color
                g2d.setColor(isEnabled() ? accentColor : new Color(60, 70, 90));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);

                // Title
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                g2d.setColor(isEnabled() ? accentColor : new Color(80, 90, 110));
                FontMetrics fm = g2d.getFontMetrics();
                int titleX = (getWidth() - fm.stringWidth(title)) / 2;
                int titleY = getHeight() / 2 - 10;
                g2d.drawString(title, titleX, titleY);

                // Description
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
                g2d.setColor(isEnabled() ? SECONDARY_COLOR : new Color(60, 70, 90));
                fm = g2d.getFontMetrics();
                int descX = (getWidth() - fm.stringWidth(description)) / 2;
                int descY = getHeight() / 2 + 15;
                g2d.drawString(description, descX, descY);

                // Decorative corner brackets
                if (isEnabled()) {
                    g2d.setColor(accentColor);
                    int bracketSize = 8;
                    g2d.drawLine(10, 10, 10 + bracketSize, 10);
                    g2d.drawLine(10, 10, 10, 10 + bracketSize);
                    g2d.drawLine(getWidth() - 10, 10, getWidth() - 10 - bracketSize, 10);
                    g2d.drawLine(getWidth() - 10, 10, getWidth() - 10, 10 + bracketSize);
                }
            }
        };

        button.setPreferredSize(new Dimension(300, 150));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("hovered", true);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("hovered", false);
                button.repaint();
            }
        });

        return button;
    }

    private void launchHost() {
        // Close launcher and start the host application
        dispose();
        SwingUtilities.invokeLater(() -> {
            try {
                com.soccerbots.control.RoboticsControlApp.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to launch Host/DriverStation: " + e.getMessage(),
                    "Launch Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private void launchSimulator() {
        // Placeholder for future simulator implementation
        JOptionPane.showMessageDialog(this,
            "Simulator coming soon!",
            "Not Available",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
