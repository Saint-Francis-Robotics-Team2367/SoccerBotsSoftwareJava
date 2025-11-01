package com.soccerbots.control.simulator;

import com.soccerbots.control.controller.ControllerInput;
import com.soccerbots.control.controller.GameController;
import net.java.games.input.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight 3D robot simulator with game controller support
 * Optimized for low-end hardware (old ThinkPads)
 */
public class SimulatorApp extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(SimulatorApp.class);
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    private static final int TARGET_FPS = 60;

    private SimulatorRenderer renderer;
    private SimulatorWorld world;
    private Map<String, GameController> connectedControllers;
    private Timer updateTimer;
    private Timer controllerDetectionTimer;
    private boolean running = false;

    public SimulatorApp() {
        setTitle("SoccerBots Simulator");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize world and robot
        world = new SimulatorWorld();

        // Initialize renderer
        renderer = new SimulatorRenderer(world);
        renderer.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        renderer.setFocusable(true);

        // Initialize controller management
        connectedControllers = new ConcurrentHashMap<>();
        startControllerDetection();

        // Setup UI
        setupUI();

        // Keyboard controls as backup
        setupKeyboardControls();

        // Start update loop
        startUpdateLoop();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add renderer
        mainPanel.add(renderer, BorderLayout.CENTER);

        // Add control panel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 35, 45));
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 60));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        // Controller status
        JLabel controllerLabel = new JLabel("Controller: Disconnected");
        controllerLabel.setForeground(Color.LIGHT_GRAY);
        controllerLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // FPS label
        JLabel fpsLabel = new JLabel("FPS: 0");
        fpsLabel.setForeground(Color.LIGHT_GRAY);
        fpsLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Position label
        JLabel posLabel = new JLabel("Position: (0.0, 0.0)");
        posLabel.setForeground(Color.LIGHT_GRAY);
        posLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Reset button
        JButton resetButton = new JButton("Reset Position");
        resetButton.setFocusable(false);
        resetButton.addActionListener(e -> world.getRobot().reset());

        // Update labels periodically
        Timer labelUpdateTimer = new Timer(100, e -> {
            List<GameController> controllers = new ArrayList<>(connectedControllers.values());
            if (!controllers.isEmpty()) {
                GameController controller = controllers.get(0);
                controllerLabel.setText("Controller: " + controller.getName());
                controllerLabel.setForeground(new Color(0, 255, 200));
            } else {
                controllerLabel.setText("Controller: Disconnected");
                controllerLabel.setForeground(Color.LIGHT_GRAY);
            }

            fpsLabel.setText("FPS: " + renderer.getFPS());

            SimulatedRobot robot = world.getRobot();
            posLabel.setText(String.format("Position: (%.1f, %.1f) | Angle: %.1f°",
                    robot.getX(), robot.getY(), Math.toDegrees(robot.getAngle())));
        });
        labelUpdateTimer.start();

        panel.add(controllerLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(fpsLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(posLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(resetButton);

        return panel;
    }

    private void setupKeyboardControls() {
        renderer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SimulatedRobot robot = world.getRobot();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        robot.setManualControl(0, 0.5, 0);
                        break;
                    case KeyEvent.VK_S:
                        robot.setManualControl(0, -0.5, 0);
                        break;
                    case KeyEvent.VK_A:
                        robot.setManualControl(-0.5, 0, 0);
                        break;
                    case KeyEvent.VK_D:
                        robot.setManualControl(0.5, 0, 0);
                        break;
                    case KeyEvent.VK_Q:
                        robot.setManualControl(0, 0, -0.5);
                        break;
                    case KeyEvent.VK_E:
                        robot.setManualControl(0, 0, 0.5);
                        break;
                    case KeyEvent.VK_R:
                        robot.reset();
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                SimulatedRobot robot = world.getRobot();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_Q:
                    case KeyEvent.VK_E:
                        robot.setManualControl(0, 0, 0);
                        break;
                }
            }
        });
    }

    private void startUpdateLoop() {
        running = true;
        int frameDelay = 1000 / TARGET_FPS;

        updateTimer = new Timer(frameDelay, e -> {
            if (!running) return;

            // Update controller input
            updateControllerInput();

            // Update world physics
            world.update(1.0 / TARGET_FPS);

            // Repaint renderer
            renderer.repaint();
        });

        updateTimer.start();
    }

    private void startControllerDetection() {
        controllerDetectionTimer = new Timer(5000, e -> detectControllers());
        controllerDetectionTimer.start();
        detectControllers(); // Initial detection
    }

    private void detectControllers() {
        try {
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            if (env == null) {
                logger.warn("Controller environment is not available");
                return;
            }

            Controller[] controllers;
            try {
                controllers = env.getControllers();
            } catch (UnsatisfiedLinkError e) {
                logger.warn("JInput native libraries not available. Controller support disabled.");
                return;
            } catch (Exception e) {
                logger.error("Failed to get controllers from environment", e);
                return;
            }

            if (controllers == null) return;

            for (Controller controller : controllers) {
                if (controller == null) continue;

                // Check for game controllers
                if (controller.getType() == Controller.Type.GAMEPAD ||
                    controller.getType() == Controller.Type.STICK ||
                    (controller.getName() != null && (
                        controller.getName().toLowerCase().contains("gamepad") ||
                        controller.getName().toLowerCase().contains("controller") ||
                        controller.getName().toLowerCase().contains("xbox") ||
                        controller.getName().toLowerCase().contains("playstation")))) {

                    try {
                        if (!controller.poll()) continue;
                    } catch (Exception pollException) {
                        continue;
                    }

                    String controllerId = controller.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                                        Math.abs(controller.hashCode());

                    if (!connectedControllers.containsKey(controllerId)) {
                        GameController gameController = new GameController(controllerId, controller);
                        connectedControllers.put(controllerId, gameController);
                        logger.info("Detected new controller: {} (Type: {})", controller.getName(), controller.getType());
                    }
                }
            }

            removeDisconnectedControllers();

        } catch (Exception e) {
            logger.error("Error detecting controllers", e);
        }
    }

    private void removeDisconnectedControllers() {
        connectedControllers.entrySet().removeIf(entry -> {
            try {
                Controller controller = entry.getValue().getController();
                if (!controller.poll()) {
                    logger.info("Controller disconnected: {}", controller.getName());
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Error polling controller, removing: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    private void updateControllerInput() {
        List<GameController> controllers = new ArrayList<>(connectedControllers.values());
        if (controllers.isEmpty()) return;

        GameController gameController = controllers.get(0);
        Controller controller = gameController.getController();

        try {
            if (!controller.poll()) return;
        } catch (Exception e) {
            logger.error("Failed to poll controller", e);
            return;
        }

        ControllerInput input = readControllerInput(controller);
        gameController.updateInput(input);

        // Use the same control scheme as the real robot
        // Left stick: forward/sideways movement
        // Right stick X: rotation
        double forward = -input.getLeftStickY();  // Invert for forward
        double sideways = input.getLeftStickX();
        double rotation = input.getRightStickX();

        world.getRobot().setControllerInput(sideways, forward, rotation);
    }

    private ControllerInput readControllerInput(Controller controller) {
        ControllerInput input = new ControllerInput();

        net.java.games.input.Component[] components = controller.getComponents();
        for (net.java.games.input.Component component : components) {
            float value = component.getPollData();
            String name = component.getName();

            switch (name.toLowerCase()) {
                case "x":
                case "x axis":
                    input.setLeftStickX(value);
                    break;
                case "y":
                case "y axis":
                    input.setLeftStickY(-value); // Invert Y axis
                    break;
                case "rx":
                case "z rotation":
                    input.setRightStickX(value);
                    break;
                case "ry":
                case "z axis":
                    input.setRightStickY(-value); // Invert Y axis
                    break;
                case "z":
                case "left trigger":
                    input.setLeftTrigger(value);
                    break;
                case "rz":
                case "right trigger":
                    input.setRightTrigger(value);
                    break;
                case "pov":
                case "hat switch":
                    input.setDPad(value);
                    break;
                default:
                    if (component.getIdentifier() instanceof net.java.games.input.Component.Identifier.Button) {
                        net.java.games.input.Component.Identifier.Button button =
                            (net.java.games.input.Component.Identifier.Button) component.getIdentifier();
                        int buttonIndex = button.toString().hashCode() % 16;
                        input.setButton(buttonIndex, value > 0.5f);
                    }
                    break;
            }
        }

        return input;
    }

    public void stop() {
        running = false;
        if (updateTimer != null) {
            updateTimer.stop();
        }
        if (controllerDetectionTimer != null) {
            controllerDetectionTimer.stop();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("Failed to set look and feel", e);
        }

        SwingUtilities.invokeLater(() -> {
            SimulatorApp app = new SimulatorApp();
            app.setVisible(true);

            // Cleanup on close
            app.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    app.stop();
                }
            });
        });
    }
}
