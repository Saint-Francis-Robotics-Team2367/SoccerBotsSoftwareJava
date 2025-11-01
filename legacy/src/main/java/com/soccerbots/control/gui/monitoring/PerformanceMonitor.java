package com.soccerbots.control.gui.monitoring;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PerformanceMonitor extends JPanel {
    private static final int MAX_DATA_POINTS = 120; // 2 minutes at 1 second intervals
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    private final List<Double> cpuData;
    private final List<Double> memoryData;
    private final List<Long> timestamps;

    private final MemoryMXBean memoryBean;
    private com.sun.management.OperatingSystemMXBean osBean;

    private double maxMemoryMB = 100.0;
    private double maxCpuPercent = 100.0;

    public PerformanceMonitor() {
        cpuData = new ArrayList<>();
        memoryData = new ArrayList<>();
        timestamps = new ArrayList<>();

        memoryBean = ManagementFactory.getMemoryMXBean();
        try {
            osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (Exception e) {
            osBean = null;
        }

        setPreferredSize(new Dimension(300, 120));
        setBorder(BorderFactory.createTitledBorder("Performance Monitor"));
        setBackground(new Color(45, 45, 45));

        // Initialize with zeros
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            cpuData.add(0.0);
            memoryData.add(0.0);
            timestamps.add(System.currentTimeMillis() - (MAX_DATA_POINTS - i) * 1000);
        }

        // Update timer
        Timer updateTimer = new Timer(1000, e -> updateData());
        updateTimer.start();
    }

    private void updateData() {
        synchronized (cpuData) {
            // Remove oldest data point
            if (cpuData.size() >= MAX_DATA_POINTS) {
                cpuData.remove(0);
                memoryData.remove(0);
                timestamps.remove(0);
            }

            // Get current metrics
            double cpuUsage = getCpuUsage();
            double memoryUsage = getMemoryUsage();

            cpuData.add(cpuUsage);
            memoryData.add(memoryUsage);
            timestamps.add(System.currentTimeMillis());

            // Auto-scale
            maxMemoryMB = Math.max(Math.max(memoryUsage * 1.1, 50.0), maxMemoryMB * 0.95);
        }

        SwingUtilities.invokeLater(this::repaint);
    }

    private double getCpuUsage() {
        if (osBean != null) {
            double cpu = osBean.getProcessCpuLoad();
            return cpu >= 0 ? cpu * 100 : 0;
        }
        return 0;
    }

    private double getMemoryUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return heapUsage.getUsed() / (1024.0 * 1024.0); // Convert to MB
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
        int marginTop = 30;
        int marginBottom = 40;

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
            g2d.drawLine(marginLeft, y, marginLeft + graphWidth, y);
        }

        // Vertical grid lines
        for (int i = 0; i <= 6; i++) {
            int x = marginLeft + (graphWidth * i) / 6;
            g2d.drawLine(x, marginTop, x, marginTop + graphHeight);
        }

        synchronized (cpuData) {
            if (cpuData.size() < 2) {
                g2d.dispose();
                return;
            }

            // Draw memory data (blue)
            g2d.setColor(new Color(100, 170, 255));
            g2d.setStroke(new BasicStroke(2.0f));

            for (int i = 1; i < memoryData.size(); i++) {
                float x1 = marginLeft + (float) (graphWidth * (i - 1)) / (MAX_DATA_POINTS - 1);
                float y1 = marginTop + graphHeight - (float) (graphHeight * memoryData.get(i - 1) / maxMemoryMB);
                float x2 = marginLeft + (float) (graphWidth * i) / (MAX_DATA_POINTS - 1);
                float y2 = marginTop + graphHeight - (float) (graphHeight * memoryData.get(i) / maxMemoryMB);

                g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            }

            // Draw CPU data (orange)
            g2d.setColor(new Color(255, 165, 0));

            for (int i = 1; i < cpuData.size(); i++) {
                float x1 = marginLeft + (float) (graphWidth * (i - 1)) / (MAX_DATA_POINTS - 1);
                float y1 = marginTop + graphHeight - (float) (graphHeight * cpuData.get(i - 1) / maxCpuPercent);
                float x2 = marginLeft + (float) (graphWidth * i) / (MAX_DATA_POINTS - 1);
                float y2 = marginTop + graphHeight - (float) (graphHeight * cpuData.get(i) / maxCpuPercent);

                g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            }
        }

        // Labels
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g2d.setColor(new Color(220, 220, 220));

        // Y-axis labels for memory
        FontMetrics fm = g2d.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            double value = maxMemoryMB * (4 - i) / 4;
            String label = DECIMAL_FORMAT.format(value) + "MB";
            int y = marginTop + (graphHeight * i) / 4;

            if (i == 0 || (y - (marginTop + (graphHeight * (i-1)) / 4)) >= fm.getHeight()) {
                g2d.drawString(label, 5, y + 3);
            }
        }

        // Current values and legend
        synchronized (cpuData) {
            double currentMemory = memoryData.isEmpty() ? 0 : memoryData.get(memoryData.size() - 1);
            double currentCpu = cpuData.isEmpty() ? 0 : cpuData.get(cpuData.size() - 1);

            // Legend
            int legendY = height - 25;
            g2d.setColor(new Color(100, 170, 255));
            g2d.fillRect(marginLeft, legendY, 15, 3);
            g2d.setColor(new Color(220, 220, 220));
            g2d.drawString(String.format("Memory: %.1fMB", currentMemory), marginLeft + 20, legendY + 8);

            g2d.setColor(new Color(255, 165, 0));
            g2d.fillRect(marginLeft + 150, legendY, 15, 3);
            g2d.setColor(new Color(220, 220, 220));
            g2d.drawString(String.format("CPU: %.1f%%", currentCpu), marginLeft + 170, legendY + 8);
        }

        // Current system info
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2d.setColor(new Color(180, 180, 180));

        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long maxMemory = heap.getMax();
        long usedMemory = heap.getUsed();
        double memoryPercent = (double) usedMemory / maxMemory * 100;

        String memInfo = String.format("Heap: %d/%dMB (%.1f%%)",
            usedMemory / (1024 * 1024), maxMemory / (1024 * 1024), memoryPercent);
        g2d.drawString(memInfo, 5, 15);

        g2d.dispose();
    }

    public double getCurrentMemoryUsage() {
        synchronized (cpuData) {
            return memoryData.isEmpty() ? 0 : memoryData.get(memoryData.size() - 1);
        }
    }

    public double getCurrentCpuUsage() {
        synchronized (cpuData) {
            return cpuData.isEmpty() ? 0 : cpuData.get(cpuData.size() - 1);
        }
    }
}