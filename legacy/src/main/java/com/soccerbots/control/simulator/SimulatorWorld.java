package com.soccerbots.control.simulator;

/**
 * Container for the simulation world
 */
public class SimulatorWorld {
    private SimulatedRobot robot;

    // World bounds
    public static final double WORLD_SIZE = 1000.0;
    public static final double GRID_SIZE = 100.0;

    public SimulatorWorld() {
        robot = new SimulatedRobot();
    }

    public void update(double deltaTime) {
        robot.update(deltaTime);

        // Keep robot within bounds (optional - can disable for free movement)
        // Uncomment to enable world boundaries:
        // double x = robot.getX();
        // double y = robot.getY();
        // if (Math.abs(x) > WORLD_SIZE / 2) {
        //     robot.x = Math.signum(x) * WORLD_SIZE / 2;
        //     robot.vx = 0;
        // }
        // if (Math.abs(y) > WORLD_SIZE / 2) {
        //     robot.y = Math.signum(y) * WORLD_SIZE / 2;
        //     robot.vy = 0;
        // }
    }

    public SimulatedRobot getRobot() {
        return robot;
    }
}
