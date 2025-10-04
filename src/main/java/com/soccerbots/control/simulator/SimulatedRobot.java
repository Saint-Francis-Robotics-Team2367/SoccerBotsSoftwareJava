package com.soccerbots.control.simulator;

/**
 * Simulated robot with holonomic drive (omni-directional movement)
 * Matches the real robot's control scheme
 */
public class SimulatedRobot {
    // Position and orientation
    private double x = 0.0;
    private double y = 0.0;
    private double angle = 0.0; // radians

    // Velocity
    private double vx = 0.0;
    private double vy = 0.0;
    private double vAngle = 0.0;

    // Control inputs (-1.0 to 1.0)
    private double inputSideways = 0.0;
    private double inputForward = 0.0;
    private double inputRotation = 0.0;

    // Robot physical properties
    private static final double MAX_SPEED = 200.0; // units per second
    private static final double MAX_ROTATION_SPEED = Math.PI; // radians per second
    private static final double ACCELERATION = 800.0;
    private static final double ROTATION_ACCELERATION = Math.PI * 4;
    private static final double FRICTION = 0.85;
    private static final double ROTATION_FRICTION = 0.8;

    // Robot dimensions (for auto-rickshaw model)
    public static final double WIDTH = 60.0;
    public static final double LENGTH = 80.0;
    public static final double HEIGHT = 50.0;
    public static final double WHEEL_RADIUS = 10.0;

    public SimulatedRobot() {
        reset();
    }

    public void reset() {
        x = 0.0;
        y = 0.0;
        angle = 0.0;
        vx = 0.0;
        vy = 0.0;
        vAngle = 0.0;
        inputSideways = 0.0;
        inputForward = 0.0;
        inputRotation = 0.0;
    }

    /**
     * Set controller input (matches ESP32Command scheme)
     * @param sideways Left stick X (-1.0 to 1.0)
     * @param forward Left stick Y (-1.0 to 1.0)
     * @param rotation Right stick X (-1.0 to 1.0)
     */
    public void setControllerInput(double sideways, double forward, double rotation) {
        this.inputSideways = clamp(sideways, -1.0, 1.0);
        this.inputForward = clamp(forward, -1.0, 1.0);
        this.inputRotation = clamp(rotation, -1.0, 1.0);
    }

    /**
     * Manual control for keyboard input
     */
    public void setManualControl(double sideways, double forward, double rotation) {
        setControllerInput(sideways, forward, rotation);
    }

    /**
     * Update robot physics
     */
    public void update(double deltaTime) {
        // Calculate target velocities from input
        double targetVx = inputSideways * MAX_SPEED;
        double targetVy = inputForward * MAX_SPEED;
        double targetVAngle = inputRotation * MAX_ROTATION_SPEED;

        // Apply acceleration towards target velocity
        vx = accelerateTowards(vx, targetVx, ACCELERATION * deltaTime);
        vy = accelerateTowards(vy, targetVy, ACCELERATION * deltaTime);
        vAngle = accelerateTowards(vAngle, targetVAngle, ROTATION_ACCELERATION * deltaTime);

        // Apply friction
        vx *= FRICTION;
        vy *= FRICTION;
        vAngle *= ROTATION_FRICTION;

        // Rotate velocity by robot angle for field-relative movement
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double worldVx = vx * cosAngle - vy * sinAngle;
        double worldVy = vx * sinAngle + vy * cosAngle;

        // Update position
        x += worldVx * deltaTime;
        y += worldVy * deltaTime;
        angle += vAngle * deltaTime;

        // Normalize angle to -PI to PI
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
    }

    private double accelerateTowards(double current, double target, double maxDelta) {
        double diff = target - current;
        if (Math.abs(diff) <= maxDelta) {
            return target;
        }
        return current + Math.signum(diff) * maxDelta;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getVAngle() { return vAngle; }
    public double getInputSideways() { return inputSideways; }
    public double getInputForward() { return inputForward; }
    public double getInputRotation() { return inputRotation; }

    public boolean isMoving() {
        return Math.abs(vx) > 1.0 || Math.abs(vy) > 1.0 || Math.abs(vAngle) > 0.01;
    }
}
