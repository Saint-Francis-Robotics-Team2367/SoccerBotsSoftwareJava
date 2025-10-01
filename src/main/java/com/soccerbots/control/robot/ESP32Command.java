package com.soccerbots.control.robot;

/**
 * ESP32-specific robot command for the minibot firmware
 * Handles the binary protocol: robotName(16) + axes(6) + buttons(2)
 */
public class ESP32Command {
    private String robotName;
    private int leftX;   // 0-255, center ~125
    private int leftY;   // 0-255, center ~130
    private int rightX;  // 0-255, center ~127
    private int rightY;  // 0-255, center ~130

    // PlayStation-style buttons
    private boolean cross;     // X button
    private boolean circle;    // O button
    private boolean square;    // □ button
    private boolean triangle;  // △ button

    public ESP32Command(String robotName) {
        this.robotName = robotName;
        // Initialize with center values
        this.leftX = 125;
        this.leftY = 130;
        this.rightX = 127;
        this.rightY = 130;
        this.cross = false;
        this.circle = false;
        this.square = false;
        this.triangle = false;
    }

    /**
     * Create command from controller input values (-1.0 to 1.0)
     */
    public static ESP32Command fromControllerInput(String robotName,
                                                   double leftStickX, double leftStickY,
                                                   double rightStickX, double rightStickY,
                                                   boolean buttonA, boolean buttonB,
                                                   boolean buttonX, boolean buttonY) {
        ESP32Command cmd = new ESP32Command(robotName);

        // Convert from -1.0/1.0 range to 0-255 range with proper center points
        cmd.leftX = (int) Math.round(leftStickX * 125.0 + 125.0);
        cmd.leftY = (int) Math.round(-leftStickY * 130.0 + 130.0);  // Invert Y axis
        cmd.rightX = (int) Math.round(rightStickX * 127.0 + 127.0);
        cmd.rightY = (int) Math.round(-rightStickY * 130.0 + 130.0); // Invert Y axis

        // Clamp values to valid range
        cmd.leftX = Math.max(0, Math.min(255, cmd.leftX));
        cmd.leftY = Math.max(0, Math.min(255, cmd.leftY));
        cmd.rightX = Math.max(0, Math.min(255, cmd.rightX));
        cmd.rightY = Math.max(0, Math.min(255, cmd.rightY));

        // Map controller buttons to PlayStation-style buttons
        cmd.cross = buttonA;     // A -> Cross
        cmd.circle = buttonB;    // B -> Circle
        cmd.square = buttonX;    // X -> Square
        cmd.triangle = buttonY;  // Y -> Triangle

        return cmd;
    }

    /**
     * Create neutral/stop command
     */
    public static ESP32Command createStopCommand(String robotName) {
        return new ESP32Command(robotName); // Default constructor creates neutral state
    }

    // Getters and setters
    public String getRobotName() { return robotName; }
    public void setRobotName(String robotName) { this.robotName = robotName; }

    public int getLeftX() { return leftX; }
    public void setLeftX(int leftX) { this.leftX = Math.max(0, Math.min(255, leftX)); }

    public int getLeftY() { return leftY; }
    public void setLeftY(int leftY) { this.leftY = Math.max(0, Math.min(255, leftY)); }

    public int getRightX() { return rightX; }
    public void setRightX(int rightX) { this.rightX = Math.max(0, Math.min(255, rightX)); }

    public int getRightY() { return rightY; }
    public void setRightY(int rightY) { this.rightY = Math.max(0, Math.min(255, rightY)); }

    public boolean isCross() { return cross; }
    public void setCross(boolean cross) { this.cross = cross; }

    public boolean isCircle() { return circle; }
    public void setCircle(boolean circle) { this.circle = circle; }

    public boolean isSquare() { return square; }
    public void setSquare(boolean square) { this.square = square; }

    public boolean isTriangle() { return triangle; }
    public void setTriangle(boolean triangle) { this.triangle = triangle; }

    /**
     * Get normalized left stick values (-1.0 to 1.0)
     */
    public double getLeftStickXNormalized() {
        return (leftX - 125.0) / 125.0;
    }

    public double getLeftStickYNormalized() {
        return -(leftY - 130.0) / 130.0; // Invert Y axis
    }

    /**
     * Get normalized right stick values (-1.0 to 1.0)
     */
    public double getRightStickXNormalized() {
        return (rightX - 127.0) / 127.0;
    }

    public double getRightStickYNormalized() {
        return -(rightY - 130.0) / 130.0; // Invert Y axis
    }

    /**
     * Check if this is a movement command (non-neutral stick positions)
     */
    public boolean hasMovement() {
        double threshold = 0.1; // 10% deadzone
        return Math.abs(getLeftStickXNormalized()) > threshold ||
               Math.abs(getLeftStickYNormalized()) > threshold ||
               Math.abs(getRightStickXNormalized()) > threshold ||
               Math.abs(getRightStickYNormalized()) > threshold;
    }

    /**
     * Check if any buttons are pressed
     */
    public boolean hasButtonInput() {
        return cross || circle || square || triangle;
    }

    @Override
    public String toString() {
        return String.format("ESP32Command{robot='%s', leftStick=(%.2f,%.2f), rightStick=(%.2f,%.2f), buttons=[%s%s%s%s]}",
                           robotName,
                           getLeftStickXNormalized(), getLeftStickYNormalized(),
                           getRightStickXNormalized(), getRightStickYNormalized(),
                           cross ? "X" : "-", circle ? "O" : "-",
                           square ? "□" : "-", triangle ? "△" : "-");
    }
}