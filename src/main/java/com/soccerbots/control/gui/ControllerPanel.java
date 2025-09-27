package com.soccerbots.control.gui;

import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.controller.GameController;
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
    private JLabel controllerCountLabel;
    
    public ControllerPanel(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        startStatusUpdater();
    }
    
    private void initializeComponents() {
        setBorder(new TitledBorder("Controller Management"));
        setPreferredSize(new Dimension(380, 300));
        
        tableModel = new ControllerTableModel();
        controllerTable = new JTable(tableModel);
        controllerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        controllerTable.setRowHeight(25);
        
        setupTableRenderers();
        
        pairButton = new JButton("Pair with Robot");
        unpairButton = new JButton("Unpair");
        controllerCountLabel = new JLabel("Controllers: 0");
        
        pairButton.setEnabled(false);
        unpairButton.setEnabled(false);
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
        
        // Table with scroll pane
        JScrollPane scrollPane = new JScrollPane(controllerTable);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(pairButton);
        buttonPanel.add(unpairButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(controllerCountLabel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        pairButton.addActionListener(this::handlePairing);
        unpairButton.addActionListener(this::handleUnpairing);
        
        controllerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
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
    
    private void updateButtonStates() {
        boolean hasSelection = controllerTable.getSelectedRow() != -1;
        pairButton.setEnabled(hasSelection);
        
        if (hasSelection) {
            GameController controller = tableModel.getControllerAt(controllerTable.getSelectedRow());
            String pairedRobotId = controller != null ? 
                controllerManager.getPairedRobotId(controller.getId()) : null;
            unpairButton.setEnabled(pairedRobotId != null);
        } else {
            unpairButton.setEnabled(false);
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