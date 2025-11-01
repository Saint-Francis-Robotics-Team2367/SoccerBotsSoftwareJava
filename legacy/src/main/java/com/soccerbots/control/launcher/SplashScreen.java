package com.soccerbots.control.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Sci-fi themed splash screen with animated elements
 */
public class SplashScreen extends JWindow {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final Color BG_COLOR = new Color(10, 15, 25);
    private static final Color ACCENT_COLOR = new Color(0, 255, 200);
    private static final Color SECONDARY_COLOR = new Color(100, 150, 255);

    private int progress = 0;
    private Timer animationTimer;

    public SplashScreen() {
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Background
                g2d.setColor(BG_COLOR);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);

                // Grid lines
                drawGrid(g2d);

                // Hexagonal border
                drawHexBorder(g2d);

                // Title
                drawTitle(g2d);

                // Progress bar
                drawProgressBar(g2d);

                // Loading text
                drawLoadingText(g2d);
            }
        };

        contentPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setContentPane(contentPanel);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(30, 40, 60, 50));
        int gridSize = 20;

        for (int x = 0; x < WIDTH; x += gridSize) {
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += gridSize) {
            g2d.drawLine(0, y, WIDTH, y);
        }
    }

    private void drawHexBorder(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(ACCENT_COLOR);

        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2 - 30;
        int radius = 80;

        Polygon hexagon = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            hexagon.addPoint(x, y);
        }

        g2d.draw(hexagon);

        // Inner glow
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 100));
        for (int i = 1; i <= 5; i++) {
            Polygon innerHex = new Polygon();
            int innerRadius = radius - i * 3;
            for (int j = 0; j < 6; j++) {
                double angle = Math.PI / 3 * j;
                int x = centerX + (int) (innerRadius * Math.cos(angle));
                int y = centerY + (int) (innerRadius * Math.sin(angle));
                innerHex.addPoint(x, y);
            }
            g2d.draw(innerHex);
        }
    }

    private void drawTitle(Graphics2D g2d) {
        // Main title
        Font titleFont = new Font("Monospaced", Font.BOLD, 36);
        g2d.setFont(titleFont);
        g2d.setColor(ACCENT_COLOR);
        String title = "SOCCERBOTS";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, HEIGHT / 2 + 60);

        // Subtitle
        Font subtitleFont = new Font("Monospaced", Font.PLAIN, 14);
        g2d.setFont(subtitleFont);
        g2d.setColor(SECONDARY_COLOR);
        String subtitle = "ROBOTICS CONTROL SYSTEM";
        fm = g2d.getFontMetrics();
        int subtitleX = (WIDTH - fm.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subtitleX, HEIGHT / 2 + 85);
    }

    private void drawProgressBar(Graphics2D g2d) {
        int barWidth = 400;
        int barHeight = 6;
        int barX = (WIDTH - barWidth) / 2;
        int barY = HEIGHT - 80;

        // Background
        g2d.setColor(new Color(30, 40, 60));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 3, 3);

        // Progress fill with gradient
        int fillWidth = (int) (barWidth * (progress / 100.0));
        if (fillWidth > 0) {
            GradientPaint gradient = new GradientPaint(
                barX, barY, ACCENT_COLOR,
                barX + fillWidth, barY, SECONDARY_COLOR
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(barX, barY, fillWidth, barHeight, 3, 3);
        }

        // Border
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 3, 3);
    }

    private void drawLoadingText(Graphics2D g2d) {
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        g2d.setFont(font);
        g2d.setColor(SECONDARY_COLOR);

        String text = "INITIALIZING SYSTEMS... " + progress + "%";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (WIDTH - fm.stringWidth(text)) / 2;
        g2d.drawString(text, textX, HEIGHT - 50);
    }

    public void show(Runnable onComplete) {
        setVisible(true);

        animationTimer = new Timer(20, e -> {
            progress += 2;
            if (progress >= 100) {
                animationTimer.stop();
                Timer closeTimer = new Timer(500, evt -> {
                    setVisible(false);
                    dispose();
                    onComplete.run();
                    ((Timer) evt.getSource()).stop();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
            repaint();
        });
        animationTimer.start();
    }
}
