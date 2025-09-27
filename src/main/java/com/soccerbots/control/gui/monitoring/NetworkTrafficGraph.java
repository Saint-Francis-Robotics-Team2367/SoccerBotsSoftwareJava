package com.soccerbots.control.gui.monitoring;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NetworkTrafficGraph extends JPanel {
    private static final int MAX_DATA_POINTS = 60; // 1 minute at 1 second intervals
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    private final List<Double> sentData;
    private final List<Double> receivedData;
    private final List<Long> timestamps;

    private double maxValue = 100.0; // Auto-scaling maximum
    private long lastUpdateTime = 0;
    private double totalSent = 0;
    private double totalReceived = 0;

    public NetworkTrafficGraph() {
        setBorder(BorderFactory.createTitledBorder("Network Traffic"));
        sentData = new ArrayList<>();
        receivedData = new ArrayList<>();
        timestamps = new ArrayList<>();

        setPreferredSize(new Dimension(300, 120));
        setMinimumSize(new Dimension(200, 80));

        // Initialize with zeros
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            sentData.add(0.0);
            receivedData.add(0.0);
            timestamps.add(System.currentTimeMillis() - (MAX_DATA_POINTS - i) * 1000);
        }

        // Update timer
        Timer updateTimer = new Timer(1000, e -> updateGraph());
        updateTimer.start();
    }

    public void addDataPoint(double bytesSent, double bytesReceived) {
        synchronized (sentData) {
            // Remove oldest data point
            if (sentData.size() >= MAX_DATA_POINTS) {
                sentData.remove(0);
                receivedData.remove(0);
                timestamps.remove(0);
            }

            // Add new data point (convert to KB/s)
            long currentTime = System.currentTimeMillis();
            double timeDiff = (currentTime - lastUpdateTime) / 1000.0;

            if (timeDiff > 0 && lastUpdateTime > 0) {
                double sentRate = (bytesSent / timeDiff) / 1024.0; // KB/s
                double receivedRate = (bytesReceived / timeDiff) / 1024.0; // KB/s

                sentData.add(sentRate);
                receivedData.add(receivedRate);
            } else {
                sentData.add(0.0);
                receivedData.add(0.0);
            }

            timestamps.add(currentTime);
            lastUpdateTime = currentTime;

            totalSent += bytesSent;
            totalReceived += bytesReceived;

            // Auto-scale
            double currentMax = Math.max(
                sentData.stream().mapToDouble(Double::doubleValue).max().orElse(0),
                receivedData.stream().mapToDouble(Double::doubleValue).max().orElse(0)
            );
            maxValue = Math.max(Math.max(currentMax * 1.1, 10.0), maxValue * 0.95);
        }

        SwingUtilities.invokeLater(this::repaint);
    }

    private void updateGraph() {
        // Add zero data point to keep graph moving
        addDataPoint(0, 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Margins
        int marginLeft = 60;
        int marginRight = 10;
        int marginTop = 20;
        int marginBottom = 30;

        int graphWidth = width - marginLeft - marginRight;
        int graphHeight = height - marginTop - marginBottom;

        // Background
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, 0, width, height);

        if (graphWidth <= 0 || graphHeight <= 0) {
            g2d.dispose();
            return;
        }

        // Grid lines
        g2d.setColor(new Color(70, 70, 70));
        g2d.setStroke(new BasicStroke(0.5f));

        // Horizontal grid lines
        for (int i = 0; i <= 4; i++) {
            int y = marginTop + (graphHeight * i) / 4;
            g2d.draw(new Line2D.Float(marginLeft, y, marginLeft + graphWidth, y));
        }

        // Vertical grid lines
        for (int i = 0; i <= 6; i++) {
            int x = marginLeft + (graphWidth * i) / 6;
            g2d.draw(new Line2D.Float(x, marginTop, x, marginTop + graphHeight));
        }

        synchronized (sentData) {
            if (sentData.size() < 2) {
                g2d.dispose();
                return;
            }

            // Draw data lines
            g2d.setStroke(new BasicStroke(2.0f));

            // Sent data (blue/accent color)
            g2d.setColor(new Color(100, 170, 255));
            Path2D sentPath = new Path2D.Float();
            boolean firstPoint = true;

            for (int i = 0; i < sentData.size(); i++) {
                float x = marginLeft + (float) (graphWidth * i) / (MAX_DATA_POINTS - 1);
                float y = marginTop + graphHeight - (float) (graphHeight * sentData.get(i) / maxValue);

                if (firstPoint) {
                    sentPath.moveTo(x, y);
                    firstPoint = false;
                } else {
                    sentPath.lineTo(x, y);
                }
            }
            g2d.draw(sentPath);

            // Received data (success color)
            g2d.setColor(new Color(60, 200, 100));
            Path2D receivedPath = new Path2D.Float();
            firstPoint = true;

            for (int i = 0; i < receivedData.size(); i++) {
                float x = marginLeft + (float) (graphWidth * i) / (MAX_DATA_POINTS - 1);
                float y = marginTop + graphHeight - (float) (graphHeight * receivedData.get(i) / maxValue);

                if (firstPoint) {
                    receivedPath.moveTo(x, y);
                    firstPoint = false;
                } else {
                    receivedPath.lineTo(x, y);
                }
            }
            g2d.draw(receivedPath);
        }

        // Labels
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2d.setColor(new Color(220, 220, 220));

        // Y-axis labels (KB/s)
        FontMetrics fm = g2d.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            double value = maxValue * (4 - i) / 4;
            String label = DECIMAL_FORMAT.format(value);
            int y = marginTop + (graphHeight * i) / 4;

            // Center the label vertically and ensure proper spacing
            int labelHeight = fm.getHeight();
            int labelY = y + (labelHeight / 4);

            // Ensure labels don't overlap by checking spacing
            if (i == 0 || (y - (marginTop + (graphHeight * (i-1)) / 4)) >= labelHeight) {
                g2d.drawString(label, 5, labelY);
            }
        }

        // Legend
        int legendY = height - 15;
        g2d.setColor(new Color(100, 170, 255));
        g2d.fillRect(marginLeft, legendY, 15, 3);
        g2d.setColor(new Color(220, 220, 220));
        g2d.drawString("Sent", marginLeft + 20, legendY + 8);

        g2d.setColor(new Color(60, 200, 100));
        g2d.fillRect(marginLeft + 80, legendY, 15, 3);
        g2d.setColor(new Color(220, 220, 220));
        g2d.drawString("Received", marginLeft + 100, legendY + 8);

        // Current values
        g2d.drawString("KB/s", 5, 15);

        g2d.dispose();
    }


    public double getTotalSent() {
        return totalSent;
    }

    public double getTotalReceived() {
        return totalReceived;
    }

    public void reset() {
        synchronized (sentData) {
            sentData.clear();
            receivedData.clear();
            timestamps.clear();
            totalSent = 0;
            totalReceived = 0;
            maxValue = 100.0;

            // Reinitialize with zeros
            for (int i = 0; i < MAX_DATA_POINTS; i++) {
                sentData.add(0.0);
                receivedData.add(0.0);
                timestamps.add(System.currentTimeMillis() - (MAX_DATA_POINTS - i) * 1000);
            }
        }
        repaint();
    }
}