package com.soccerbots.control.gui.monitoring;

import com.soccerbots.control.controller.GameController;
import com.soccerbots.control.controller.ControllerInput;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class ControllerVisualization extends JPanel {
    private GameController controller;
    private static final int STICK_AREA_SIZE = 80;
    private static final int STICK_RADIUS = 8;
    private static final int BUTTON_SIZE = 20;
    private static final int TRIGGER_WIDTH = 60;
    private static final int TRIGGER_HEIGHT = 15;

    public ControllerVisualization() {
        setPreferredSize(new Dimension(400, 300));
        setBorder(BorderFactory.createTitledBorder("Controller Input Visualization"));
        setBackground(new Color(45, 45, 45));

        // Update timer
        Timer updateTimer = new Timer(50, e -> repaint());
        updateTimer.start();
    }

    public void setController(GameController controller) {
        this.controller = controller;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (controller == null || !controller.isConnected()) {
            drawNoController(g2d);
            g2d.dispose();
            return;
        }

        ControllerInput input = controller.getLastInput();

        // Calculate layout positions
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw controller outline/background
        drawControllerBackground(g2d, centerX, centerY);

        // Draw analog sticks
        drawAnalogStick(g2d, centerX - 120, centerY, input.getLeftStickX(), input.getLeftStickY(), "L");
        drawAnalogStick(g2d, centerX + 120, centerY, input.getRightStickX(), input.getRightStickY(), "R");

        // Draw triggers
        drawTrigger(g2d, centerX - 100, centerY - 120, input.getLeftTrigger(), "LT");
        drawTrigger(g2d, centerX + 40, centerY - 120, input.getRightTrigger(), "RT");

        // Draw D-Pad
        drawDPad(g2d, centerX - 60, centerY + 60, input.getDPad());

        // Draw buttons
        drawButtons(g2d, centerX + 60, centerY + 30, input);

        // Draw controller info
        drawControllerInfo(g2d, 10, height - 40);

        g2d.dispose();
    }

    private void drawNoController(Graphics2D g2d) {
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        String message = "No Controller Selected";
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(message, x, y);
    }

    private void drawControllerBackground(Graphics2D g2d, int centerX, int centerY) {
        // Draw a simple controller outline
        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(2));

        // Main body
        Rectangle2D body = new Rectangle2D.Float(centerX - 150, centerY - 50, 300, 100);
        g2d.draw(body);

        // Handle grips
        Rectangle2D leftGrip = new Rectangle2D.Float(centerX - 180, centerY - 20, 30, 60);
        Rectangle2D rightGrip = new Rectangle2D.Float(centerX + 150, centerY - 20, 30, 60);
        g2d.draw(leftGrip);
        g2d.draw(rightGrip);
    }

    private void drawAnalogStick(Graphics2D g2d, int centerX, int centerY, float x, float y, String label) {
        // Draw stick area
        g2d.setColor(new Color(80, 80, 80));
        Ellipse2D stickArea = new Ellipse2D.Float(
            centerX - STICK_AREA_SIZE/2,
            centerY - STICK_AREA_SIZE/2,
            STICK_AREA_SIZE,
            STICK_AREA_SIZE
        );
        g2d.fill(stickArea);

        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(stickArea);

        // Draw stick position
        float stickX = centerX + (x * (STICK_AREA_SIZE/2 - STICK_RADIUS));
        float stickY = centerY + (y * (STICK_AREA_SIZE/2 - STICK_RADIUS));

        g2d.setColor(new Color(100, 170, 255));
        Ellipse2D stick = new Ellipse2D.Float(
            stickX - STICK_RADIUS,
            stickY - STICK_RADIUS,
            STICK_RADIUS * 2,
            STICK_RADIUS * 2
        );
        g2d.fill(stick);

        // Draw label
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(label, centerX - fm.stringWidth(label)/2, centerY + STICK_AREA_SIZE/2 + 20);

        // Draw coordinates
        String coords = String.format("(%.2f, %.2f)", x, y);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        fm = g2d.getFontMetrics();
        g2d.drawString(coords, centerX - fm.stringWidth(coords)/2, centerY + STICK_AREA_SIZE/2 + 35);
    }

    private void drawTrigger(Graphics2D g2d, int x, int y, float value, String label) {
        // Draw trigger background
        g2d.setColor(new Color(80, 80, 80));
        Rectangle2D triggerBg = new Rectangle2D.Float(x, y, TRIGGER_WIDTH, TRIGGER_HEIGHT);
        g2d.fill(triggerBg);

        g2d.setColor(new Color(120, 120, 120));
        g2d.draw(triggerBg);

        // Draw trigger value
        if (value > 0) {
            g2d.setColor(new Color(255, 193, 7));
            Rectangle2D triggerValue = new Rectangle2D.Float(x + 2, y + 2, (TRIGGER_WIDTH - 4) * value, TRIGGER_HEIGHT - 4);
            g2d.fill(triggerValue);
        }

        // Draw label
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g2d.drawString(label, x, y - 5);

        // Draw value
        String valueStr = String.format("%.2f", value);
        g2d.drawString(valueStr, x, y + TRIGGER_HEIGHT + 15);
    }

    private void drawDPad(Graphics2D g2d, int centerX, int centerY, float dPadValue) {
        int dPadSize = 40;

        // Draw D-Pad background
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(centerX - dPadSize/2, centerY - dPadSize/2, dPadSize, dPadSize);

        g2d.setColor(new Color(120, 120, 120));
        g2d.drawRect(centerX - dPadSize/2, centerY - dPadSize/2, dPadSize, dPadSize);

        // Highlight pressed direction based on dPadValue
        g2d.setColor(new Color(60, 200, 100));

        if (dPadValue == 0.25f) { // Up
            g2d.fillRect(centerX - 5, centerY - dPadSize/2, 10, 15);
        } else if (dPadValue == 0.75f) { // Down
            g2d.fillRect(centerX - 5, centerY + dPadSize/2 - 15, 10, 15);
        } else if (dPadValue == 0.5f) { // Right
            g2d.fillRect(centerX + dPadSize/2 - 15, centerY - 5, 15, 10);
        } else if (dPadValue == 1.0f) { // Left
            g2d.fillRect(centerX - dPadSize/2, centerY - 5, 15, 10);
        }

        // Draw label
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g2d.drawString("D-PAD", centerX - 15, centerY + dPadSize/2 + 15);
    }

    private void drawButtons(Graphics2D g2d, int startX, int startY, ControllerInput input) {
        String[] buttonNames = {"A", "B", "X", "Y", "LB", "RB", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};

        for (int i = 0; i < Math.min(16, buttonNames.length); i++) {
            int x = startX + (i % 4) * 25;
            int y = startY + (i / 4) * 25;

            // Draw button
            if (input.getButton(i)) {
                g2d.setColor(new Color(255, 100, 120));
            } else {
                g2d.setColor(new Color(80, 80, 80));
            }

            Ellipse2D button = new Ellipse2D.Float(x - BUTTON_SIZE/2, y - BUTTON_SIZE/2, BUTTON_SIZE, BUTTON_SIZE);
            g2d.fill(button);

            g2d.setColor(new Color(120, 120, 120));
            g2d.draw(button);

            // Draw button label
            g2d.setColor(new Color(220, 220, 220));
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
            FontMetrics fm = g2d.getFontMetrics();
            String buttonName = i < 4 ? buttonNames[i] : String.valueOf(i);
            g2d.drawString(buttonName, x - fm.stringWidth(buttonName)/2, y + 3);
        }

        // Draw buttons label
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g2d.drawString("BUTTONS", startX - 5, startY - 15);
    }

    private void drawControllerInfo(Graphics2D g2d, int x, int y) {
        if (controller == null) return;

        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        String info = String.format("Controller: %s | Last Update: %dms ago",
                                  controller.getName(),
                                  controller.getTimeSinceLastUpdate());
        g2d.drawString(info, x, y);

        // Connection status
        if (controller.isConnected()) {
            g2d.setColor(new Color(60, 200, 100));
            g2d.drawString("● CONNECTED", x, y + 15);
        } else {
            g2d.setColor(new Color(255, 100, 120));
            g2d.drawString("● DISCONNECTED", x, y + 15);
        }
    }
}