package com.soccerbots.control.gui;

import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.controller.GameController;
import com.soccerbots.control.robot.Robot;
import com.soccerbots.control.robot.RobotManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RobotSelectionDialog extends JDialog {
    private final ControllerManager controllerManager;
    private final GameController controller;
    
    private JList<Robot> robotList;
    private DefaultListModel<Robot> listModel;
    private JButton pairButton;
    private JButton cancelButton;
    
    public RobotSelectionDialog(Window parent, ControllerManager controllerManager, GameController controller) {
        super(parent, "Pair Controller with Robot", ModalityType.APPLICATION_MODAL);
        this.controllerManager = controllerManager;
        this.controller = controller;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadAvailableRobots();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        robotList = new JList<>(listModel);
        robotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        robotList.setCellRenderer(new RobotListCellRenderer());
        
        pairButton = new JButton("Pair");
        cancelButton = new JButton("Cancel");
        
        pairButton.setEnabled(false);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel("Controller: " + controller.getName()));
        add(headerPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(robotList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Robots"));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(pairButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        pairButton.addActionListener(this::handlePair);
        cancelButton.addActionListener(e -> dispose());
        
        robotList.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pairButton.setEnabled(robotList.getSelectedValue() != null);
            }
        });
        
        robotList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && robotList.getSelectedValue() != null) {
                    handlePair(null);
                }
            }
        });
    }
    
    private void loadAvailableRobots() {
        // This is a simplified approach - in a real implementation, 
        // you'd get the RobotManager instance from the ControllerManager
        // For now, we'll show a message that robots need to be discovered first
        listModel.clear();
        
        // Add dummy robots for demonstration
        // In the real implementation, this would come from RobotManager
        listModel.addElement(new Robot("demo1", "Demo Robot 1", "192.168.1.100", "Connected"));
        listModel.addElement(new Robot("demo2", "Demo Robot 2", "192.168.1.101", "Connected"));
        
        if (listModel.isEmpty()) {
            JLabel emptyLabel = new JLabel("No robots available. Discover robots first.");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            
            // Replace the list with the empty message
            remove(getContentPane().getComponent(1)); // Remove scroll pane
            add(emptyLabel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }
    
    private void handlePair(ActionEvent e) {
        Robot selectedRobot = robotList.getSelectedValue();
        if (selectedRobot == null) {
            return;
        }
        
        // Check if robot is already paired
        String currentPairedControllerId = selectedRobot.getPairedControllerId();
        if (currentPairedControllerId != null && !currentPairedControllerId.equals(controller.getId())) {
            int result = JOptionPane.showConfirmDialog(this,
                "Robot '" + selectedRobot.getName() + "' is already paired with another controller.\n" +
                "Do you want to reassign it to this controller?",
                "Robot Already Paired",
                JOptionPane.YES_NO_OPTION);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        controllerManager.pairControllerWithRobot(controller.getId(), selectedRobot.getId());
        
        JOptionPane.showMessageDialog(this,
            "Controller '" + controller.getName() + "' has been paired with robot '" + selectedRobot.getName() + "'.",
            "Pairing Successful",
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    private static class RobotListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Robot) {
                Robot robot = (Robot) value;
                setText(robot.getName() + " (" + robot.getIpAddress() + ")");
                
                if (robot.isConnected()) {
                    setIcon(createStatusIcon(Color.GREEN));
                } else {
                    setIcon(createStatusIcon(Color.RED));
                }
                
                if (robot.isPaired()) {
                    setForeground(isSelected ? Color.WHITE : Color.GRAY);
                }
            }
            
            return this;
        }
        
        private Icon createStatusIcon(Color color) {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(color);
                    g.fillOval(x, y + 2, 8, 8);
                    g.setColor(Color.BLACK);
                    g.drawOval(x, y + 2, 8, 8);
                }
                
                @Override
                public int getIconWidth() { return 12; }
                
                @Override
                public int getIconHeight() { return 12; }
            };
        }
    }
}