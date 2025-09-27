package com.soccerbots.control.gui;

import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.controller.GameController;
import com.soccerbots.control.gui.monitoring.ControllerVisualization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;

public class ControllerPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ControllerPanel.class);
    
    private final ControllerManager controllerManager;
    
    private JTable controllerTable;
    private ControllerTableModel tableModel;
    private JButton pairButton;
    private JButton unpairButton;
    private JButton showMappingButton;
    private JButton showVisualizationButton;
    private JButton refreshButton;
    private JLabel controllerCountLabel;
    private ControllerVisualization controllerVisualization;
    
    public ControllerPanel(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        startStatusUpdater();
    }
    
    private void initializeComponents() {
        setBorder(new TitledBorder("Controller Management"));
        setPreferredSize(new Dimension(380, 450));
        
        tableModel = new ControllerTableModel();
        controllerTable = new JTable(tableModel);
        controllerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        controllerTable.setRowHeight(25);
        
        setupTableRenderers();
        
        pairButton = new JButton("Pair with Robot");
        unpairButton = new JButton("Unpair");
        showMappingButton = new JButton("Show Mapping");
        showVisualizationButton = new JButton("Show Input");
        refreshButton = new JButton("🔄 Search Controllers");
        controllerCountLabel = new JLabel("Controllers: 0");
        controllerVisualization = new ControllerVisualization();

        pairButton.setEnabled(false);
        unpairButton.setEnabled(false);
        showMappingButton.setEnabled(false);
        showVisualizationButton.setEnabled(false);
    }
    
    private void setupTableRenderers() {
        // Status column renderer
        controllerTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = value.toString();
                if ("Connected".equals(status)) {
                    setForeground(new Color(0, 150, 0));
                } else {
                    setForeground(Color.RED);
                }
                return this;
            }
        });
        
        // Paired column renderer
        controllerTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null && !"None".equals(value)) {
                    setForeground(new Color(0, 150, 0));
                } else {
                    setForeground(Color.RED);
                }
                return this;
            }
        });
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Top panel with table
        JPanel topPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(controllerTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        topPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(pairButton);
        buttonPanel.add(unpairButton);
        buttonPanel.add(showMappingButton);
        buttonPanel.add(showVisualizationButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(controllerCountLabel);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Controller visualization in center
        controllerVisualization.setPreferredSize(new Dimension(0, 250));
        add(controllerVisualization, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        pairButton.addActionListener(this::handlePairing);
        unpairButton.addActionListener(this::handleUnpairing);
        showMappingButton.addActionListener(this::handleShowMapping);
        showVisualizationButton.addActionListener(this::handleShowVisualization);
        refreshButton.addActionListener(this::handleRefreshControllers);
        
        controllerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
                updateControllerVisualization();
            }
        });
    }
    
    private void handlePairing(ActionEvent e) {
        int selectedRow = controllerTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        GameController controller = tableModel.getControllerAt(selectedRow);
        if (controller == null) {
            return;
        }
        
        // Open robot selection dialog
        RobotSelectionDialog dialog = new RobotSelectionDialog(
            SwingUtilities.getWindowAncestor(this),
            controllerManager,
            controller
        );
        dialog.setVisible(true);
        
        // Refresh table after pairing
        SwingUtilities.invokeLater(() -> tableModel.fireTableDataChanged());
    }
    
    private void handleUnpairing(ActionEvent e) {
        int selectedRow = controllerTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        GameController controller = tableModel.getControllerAt(selectedRow);
        if (controller == null) {
            return;
        }
        
        String pairedRobotId = controllerManager.getPairedRobotId(controller.getId());
        if (pairedRobotId == null) {
            JOptionPane.showMessageDialog(this, 
                "Controller is not paired with any robot.", 
                "Not Paired", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to unpair this controller?",
            "Confirm Unpairing",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            controllerManager.unpairController(controller.getId());
            tableModel.fireTableDataChanged();
            logger.info("Unpaired controller: {}", controller.getName());
        }
    }

    private void handleShowMapping(ActionEvent e) {
        int selectedRow = controllerTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        GameController controller = tableModel.getControllerAt(selectedRow);
        if (controller == null) {
            return;
        }

        ControllerMappingDialog dialog = new ControllerMappingDialog(
            SwingUtilities.getWindowAncestor(this),
            controller
        );
        dialog.setVisible(true);
    }

    private void handleShowVisualization(ActionEvent e) {
        int selectedRow = controllerTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        GameController controller = tableModel.getControllerAt(selectedRow);
        if (controller == null) {
            return;
        }

        // Create a dialog to show the controller visualization in fullscreen
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Controller Input Visualization", true);
        ControllerVisualization fullViz = new ControllerVisualization();
        fullViz.setController(controller);
        fullViz.setPreferredSize(new Dimension(600, 400));

        dialog.add(fullViz);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleRefreshControllers(ActionEvent e) {
        refreshButton.setEnabled(false);
        refreshButton.setText("Searching...");

        // Run controller refresh in background
        SwingUtilities.invokeLater(() -> {
            try {
                controllerManager.refreshControllers();

                // Update the table
                tableModel.fireTableDataChanged();
                updateControllerCount();

                logger.info("Controller refresh completed");

            } catch (Exception ex) {
                logger.error("Error during controller refresh", ex);
                JOptionPane.showMessageDialog(this,
                    "Error searching for controllers: " + ex.getMessage(),
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshButton.setEnabled(true);
                refreshButton.setText("🔄 Search Controllers");
            }
        });
    }

    private void updateButtonStates() {
        boolean hasSelection = controllerTable.getSelectedRow() != -1;
        pairButton.setEnabled(hasSelection);
        showMappingButton.setEnabled(hasSelection);
        showVisualizationButton.setEnabled(hasSelection);

        if (hasSelection) {
            GameController controller = tableModel.getControllerAt(controllerTable.getSelectedRow());
            String pairedRobotId = controller != null ?
                controllerManager.getPairedRobotId(controller.getId()) : null;
            unpairButton.setEnabled(pairedRobotId != null);
        } else {
            unpairButton.setEnabled(false);
        }
    }

    private void updateControllerVisualization() {
        int selectedRow = controllerTable.getSelectedRow();
        if (selectedRow != -1) {
            GameController controller = tableModel.getControllerAt(selectedRow);
            controllerVisualization.setController(controller);
        } else {
            controllerVisualization.setController(null);
        }
    }
    
    private void updateControllerCount() {
        int count = controllerManager.getConnectedControllerCount();
        controllerCountLabel.setText("Controllers: " + count);
    }
    
    private void startStatusUpdater() {
        Timer timer = new Timer(1000, e -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.fireTableDataChanged();
                updateControllerCount();
            });
        });
        timer.start();
    }
    
    private class ControllerTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Controller Name", "Status", "Paired Robot"};
        private List<GameController> controllers = new ArrayList<>();
        
        @Override
        public int getRowCount() {
            controllers = controllerManager.getConnectedControllers();
            return controllers.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= controllers.size()) {
                return null;
            }
            
            GameController controller = controllers.get(rowIndex);
            switch (columnIndex) {
                case 0: return controller.getName();
                case 1: return controller.isConnected() ? "Connected" : "Disconnected";
                case 2: 
                    String pairedRobotId = controllerManager.getPairedRobotId(controller.getId());
                    return pairedRobotId != null ? pairedRobotId : "None";
                default: return null;
            }
        }
        
        public GameController getControllerAt(int rowIndex) {
            return rowIndex >= 0 && rowIndex < controllers.size() ? controllers.get(rowIndex) : null;
        }
    }
}