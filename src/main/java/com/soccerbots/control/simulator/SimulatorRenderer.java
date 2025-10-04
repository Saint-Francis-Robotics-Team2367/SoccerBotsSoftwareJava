package com.soccerbots.control.simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Lightweight 3D renderer for the robot simulator
 * Uses simple 2.5D projection for performance on old hardware
 */
public class SimulatorRenderer extends JPanel {
    private SimulatorWorld world;
    private double cameraX = 0;
    private double cameraY = 0;
    private double zoom = 1.0;
    private boolean followRobot = true;

    // Performance tracking
    private long lastFrameTime = System.nanoTime();
    private int fps = 0;
    private int frameCount = 0;
    private long fpsTimer = System.currentTimeMillis();

    // Colors
    private static final Color BG_COLOR = new Color(20, 25, 35);
    private static final Color GRID_COLOR = new Color(40, 50, 70);
    private static final Color GRID_MAJOR_COLOR = new Color(60, 75, 100);
    private static final Color ROBOT_COLOR = new Color(0, 255, 200);
    private static final Color ROBOT_SHADOW = new Color(0, 0, 0, 80);
    private static final Color WHEEL_COLOR = new Color(50, 50, 50);
    private static final Color GROUND_COLOR = new Color(30, 40, 55);

    public SimulatorRenderer(SimulatorWorld world) {
        this.world = world;
        setBackground(BG_COLOR);
        setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // Update camera to follow robot
        if (followRobot) {
            SimulatedRobot robot = world.getRobot();
            cameraX = robot.getX();
            cameraY = robot.getY();
        }

        // Draw scene
        drawGrid(g2d);
        drawRobot(g2d);
        drawHUD(g2d);

        // Update FPS
        updateFPS();
    }

    private void drawGrid(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        double gridSize = SimulatorWorld.GRID_SIZE * zoom;

        // Calculate visible grid range
        double startX = cameraX - (width / (2.0 * zoom));
        double endX = cameraX + (width / (2.0 * zoom));
        double startY = cameraY - (height / (2.0 * zoom));
        double endY = cameraY + (height / (2.0 * zoom));

        // Draw grid lines
        g2d.setStroke(new BasicStroke(1));

        // Vertical lines
        int startGridX = (int) Math.floor(startX / SimulatorWorld.GRID_SIZE);
        int endGridX = (int) Math.ceil(endX / SimulatorWorld.GRID_SIZE);

        for (int i = startGridX; i <= endGridX; i++) {
            double worldX = i * SimulatorWorld.GRID_SIZE;
            int screenX = worldToScreenX(worldX);

            if (i % 5 == 0) {
                g2d.setColor(GRID_MAJOR_COLOR);
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setColor(GRID_COLOR);
                g2d.setStroke(new BasicStroke(1));
            }

            g2d.drawLine(screenX, 0, screenX, height);
        }

        // Horizontal lines
        int startGridY = (int) Math.floor(startY / SimulatorWorld.GRID_SIZE);
        int endGridY = (int) Math.ceil(endY / SimulatorWorld.GRID_SIZE);

        for (int i = startGridY; i <= endGridY; i++) {
            double worldY = i * SimulatorWorld.GRID_SIZE;
            int screenY = worldToScreenY(worldY);

            if (i % 5 == 0) {
                g2d.setColor(GRID_MAJOR_COLOR);
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setColor(GRID_COLOR);
                g2d.setStroke(new BasicStroke(1));
            }

            g2d.drawLine(0, screenY, width, screenY);
        }

        // Draw origin marker
        g2d.setColor(new Color(255, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        int originX = worldToScreenX(0);
        int originY = worldToScreenY(0);
        g2d.drawLine(originX - 20, originY, originX + 20, originY);
        g2d.drawLine(originX, originY - 20, originX, originY + 20);
    }

    private void drawRobot(Graphics2D g2d) {
        SimulatedRobot robot = world.getRobot();

        int screenX = worldToScreenX(robot.getX());
        int screenY = worldToScreenY(robot.getY());

        g2d.translate(screenX, screenY);
        g2d.rotate(-robot.getAngle()); // Negative because screen Y is inverted

        double scale = zoom;

        // Draw shadow (simple ellipse on ground)
        g2d.setColor(ROBOT_SHADOW);
        int shadowWidth = (int) (SimulatedRobot.WIDTH * scale * 1.2);
        int shadowHeight = (int) (SimulatedRobot.LENGTH * scale * 0.3);
        g2d.fillOval(-shadowWidth / 2, (int)(SimulatedRobot.LENGTH * scale * 0.3), shadowWidth, shadowHeight);

        // Draw auto-rickshaw body (simple 3D projection)
        drawAutoRickshaw(g2d, scale);

        g2d.rotate(robot.getAngle());
        g2d.translate(-screenX, -screenY);
    }

    private void drawAutoRickshaw(Graphics2D g2d, double scale) {
        int width = (int) (SimulatedRobot.WIDTH * scale);
        int length = (int) (SimulatedRobot.LENGTH * scale);
        int height = (int) (SimulatedRobot.HEIGHT * scale);

        // 3D offset for pseudo-3D effect
        int offset3D = (int) (15 * scale);

        // Draw back panel (darker)
        g2d.setColor(ROBOT_COLOR.darker().darker());
        int[] backX = {-width/2 + offset3D, width/2 + offset3D, width/2 + offset3D, -width/2 + offset3D};
        int[] backY = {-length/2 - offset3D, -length/2 - offset3D, length/2 - offset3D, length/2 - offset3D};
        g2d.fillPolygon(backX, backY, 4);

        // Draw roof (top panel)
        g2d.setColor(ROBOT_COLOR.darker());
        int[] roofX = {-width/2, width/2, width/2 + offset3D, -width/2 + offset3D};
        int[] roofY = {-length/2 - height, -length/2 - height, -length/2 - height - offset3D, -length/2 - height - offset3D};
        g2d.fillPolygon(roofX, roofY, 4);

        // Draw main body
        g2d.setColor(ROBOT_COLOR);
        g2d.fillRoundRect(-width/2, -length/2, width, length, 10, 10);

        // Draw cabin (front part)
        g2d.setColor(ROBOT_COLOR.brighter());
        int cabinWidth = (int) (width * 0.8);
        int cabinLength = (int) (length * 0.4);
        g2d.fillRoundRect(-cabinWidth/2, -length/2, cabinWidth, cabinLength, 8, 8);

        // Draw windshield
        g2d.setColor(new Color(100, 150, 200, 150));
        int windshieldWidth = (int) (cabinWidth * 0.7);
        int windshieldHeight = (int) (cabinLength * 0.6);
        g2d.fillRoundRect(-windshieldWidth/2, -length/2 + 5, windshieldWidth, windshieldHeight, 5, 5);

        // Draw wheels (3 wheels for auto-rickshaw)
        g2d.setColor(WHEEL_COLOR);
        int wheelRadius = (int) (SimulatedRobot.WHEEL_RADIUS * scale);

        // Front wheel (center)
        g2d.fillOval(-wheelRadius/2, -length/2 - wheelRadius/2, wheelRadius, wheelRadius);

        // Back wheels (left and right)
        int backWheelY = length/2 - wheelRadius/2;
        g2d.fillOval(-width/2 - wheelRadius/2, backWheelY, wheelRadius, wheelRadius);
        g2d.fillOval(width/2 - wheelRadius/2, backWheelY, wheelRadius, wheelRadius);

        // Draw direction indicator (front)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, -length/2, 0, -length/2 - 15);

        // Draw velocity vectors (debug)
        if (robot.isMoving()) {
            g2d.setColor(new Color(255, 255, 0, 150));
            g2d.setStroke(new BasicStroke(2));
            double vx = robot.getInputSideways() * 30;
            double vy = -robot.getInputForward() * 30;
            g2d.drawLine(0, 0, (int) vx, (int) vy);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        SimulatedRobot robot = world.getRobot();

        // Draw controls help (top-left)
        g2d.setColor(new Color(200, 200, 200, 180));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        String[] controls = {
            "Controls:",
            "  Controller: Left Stick = Move, Right Stick X = Rotate",
            "  Keyboard: WASD = Move, QE = Rotate, R = Reset"
        };

        int y = 20;
        for (String line : controls) {
            g2d.drawString(line, 10, y);
            y += 15;
        }

        // Draw input indicators (bottom-left)
        drawInputIndicators(g2d);
    }

    private void drawInputIndicators(Graphics2D g2d) {
        SimulatedRobot robot = world.getRobot();
        int x = 10;
        int y = getHeight() - 120;

        g2d.setColor(new Color(50, 60, 80, 200));
        g2d.fillRoundRect(x, y, 200, 110, 10, 10);

        g2d.setColor(ROBOT_COLOR);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2d.drawString("INPUT", x + 10, y + 20);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2d.setColor(Color.LIGHT_GRAY);

        drawBar(g2d, "Sideways", robot.getInputSideways(), x + 10, y + 35);
        drawBar(g2d, "Forward ", robot.getInputForward(), x + 10, y + 60);
        drawBar(g2d, "Rotation", robot.getInputRotation(), x + 10, y + 85);
    }

    private void drawBar(Graphics2D g2d, String label, double value, int x, int y) {
        g2d.drawString(label + ":", x, y);

        int barX = x + 70;
        int barWidth = 100;
        int barHeight = 10;

        // Background
        g2d.setColor(new Color(30, 40, 50));
        g2d.fillRect(barX, y - 8, barWidth, barHeight);

        // Value bar
        int fillWidth = (int) (Math.abs(value) * barWidth / 2);
        if (value > 0) {
            g2d.setColor(ROBOT_COLOR);
            g2d.fillRect(barX + barWidth/2, y - 8, fillWidth, barHeight);
        } else if (value < 0) {
            g2d.setColor(new Color(255, 100, 100));
            g2d.fillRect(barX + barWidth/2 - fillWidth, y - 8, fillWidth, barHeight);
        }

        // Center line
        g2d.setColor(Color.GRAY);
        g2d.drawLine(barX + barWidth/2, y - 8, barX + barWidth/2, y + 2);

        // Border
        g2d.setColor(GRID_COLOR);
        g2d.drawRect(barX, y - 8, barWidth, barHeight);
    }

    private int worldToScreenX(double worldX) {
        return getWidth() / 2 + (int) ((worldX - cameraX) * zoom);
    }

    private int worldToScreenY(double worldY) {
        return getHeight() / 2 + (int) ((worldY - cameraY) * zoom);
    }

    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - fpsTimer >= 1000) {
            fps = frameCount;
            frameCount = 0;
            fpsTimer = currentTime;
        }
        lastFrameTime = System.nanoTime();
    }

    public int getFPS() {
        return fps;
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(0.1, Math.min(5.0, zoom));
    }

    public void setFollowRobot(boolean follow) {
        this.followRobot = follow;
    }

    private SimulatedRobot robot; // Local reference for direct access
}
