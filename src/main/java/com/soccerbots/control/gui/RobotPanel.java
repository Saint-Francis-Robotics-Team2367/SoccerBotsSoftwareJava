package com.soccerbots.control.gui;

import com.soccerbots.control.robot.Robot;
import com.soccerbots.control.robot.RobotManager;
import com.soccerbots.control.gui.theme.Theme;
import com.soccerbots.control.gui.theme.ThemedComponent;
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

public class RobotPanel extends ThemedComponent {
    private static final Logger logger = LoggerFactory.getLogger(RobotPanel.class);
    
    private final RobotManager robotManager;
    
    private JTable robotTable;
    private RobotTableModel tableModel;
    private JButton discoverButton;
    private JButton configureButton;
    private JButton removeButton;
    private JLabel robotCountLabel;
    
    public RobotPanel(RobotManager robotManager) {
        super();
        this.robotManager = robotManager;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        startStatusUpdater();
    }
    
    private void initializeComponents() {
        setBorder(new TitledBorder("Robot Management"));
        
        tableModel = new RobotTableModel();
        robotTable = new JTable(tableModel);
        robotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        robotTable.setRowHeight(25);
        
        setupTableRenderers();
        
        discoverButton = new JButton("Discover Robots");
        configureButton = new JButton("Configure WiFi");
        removeButton = new JButton("Remove Robot");
        robotCountLabel = new JLabel("Robots: 0");
        
        configureButton.setEnabled(false);
        removeButton.setEnabled(false);
    }
    
    private void setupTableRenderers() {
        // Status column renderer
        robotTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = value.toString();
                switch (status.toLowerCase()) {
                    case "connected":
                        setForeground(new Color(0, 150, 0));
                        break;
                    case "disconnected":
                        setForeground(Color.RED);
                        break;
                    case "configuring":
                        setForeground(Color.ORANGE);
                        break;
                    default:
                        setForeground(Color.BLACK);
                        break;
                }
                return this;
            }
        });
        
        // Paired column renderer
        robotTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if ("Yes".equals(value)) {
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
        JScrollPane scrollPane = new JScrollPane(robotTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(discoverButton);
        buttonPanel.add(configureButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(robotCountLabel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        discoverButton.addActionListener(this::handleDiscovery);
        configureButton.addActionListener(this::handleConfiguration);
        removeButton.addActionListener(this::handleRemoveRobot);
        
        robotTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }
    
    private void handleDiscovery(ActionEvent e) {
        discoverButton.setEnabled(false);
        discoverButton.setText("Discovering...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                robotManager.startDiscovery();
                
                // Wait for discovery to complete
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                discoverButton.setEnabled(true);
                discoverButton.setText("Discover Robots");
                refreshTable();
                logger.info("Robot discovery completed");
            }
        };
        
        worker.execute();
    }
    
    private void handleConfiguration(ActionEvent e) {
        int selectedRow = robotTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        Robot robot = tableModel.getRobotAt(selectedRow);
        if (robot == null) {
            return;
        }
        
        WiFiConfigDialog dialog = new WiFiConfigDialog(
            SwingUtilities.getWindowAncestor(this), 
            robotManager, 
            robot
        );
        dialog.setVisible(true);
    }
    
    private void handleRemoveRobot(ActionEvent e) {
        int selectedRow = robotTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        Robot robot = tableModel.getRobotAt(selectedRow);
        if (robot == null) {
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove robot '" + robot.getName() + "'?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            robotManager.removeRobot(robot.getId());
            refreshTable();
            logger.info("Removed robot: {}", robot.getName());
        }
    }
    
    private void updateButtonStates() {
        boolean hasSelection = robotTable.getSelectedRow() != -1;
        configureButton.setEnabled(hasSelection);
        removeButton.setEnabled(hasSelection);
    }
    
    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.fireTableDataChanged();
            updateRobotCount();
        });
    }
    
    private void updateRobotCount() {
        int count = robotManager.getConnectedRobotCount();
        robotCountLabel.setText("Robots: " + count);
    }
    
    private void startStatusUpdater() {
        Timer timer = new Timer(2000, e -> {
            robotManager.clearOfflineRobots();
            refreshTable();
        });
        timer.start();
    }
    
    private class RobotTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Name", "IP Address", "Status", "Last Seen", "Paired"};
        private List<Robot> robots = new ArrayList<>();
        
        @Override
        public int getRowCount() {
            robots = robotManager.getConnectedRobots();
            return robots.size();
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
            if (rowIndex >= robots.size()) {
                return null;
            }
            
            Robot robot = robots.get(rowIndex);
            switch (columnIndex) {
                case 0: return robot.getName();
                case 1: return robot.getIpAddress();
                case 2: return robot.isConnected() ? "Connected" : "Disconnected";
                case 3: 
                    long timeSinceLastSeen = robot.getTimeSinceLastSeen() / 1000;
                    return timeSinceLastSeen + "s ago";
                case 4: return robot.isPaired() ? "Yes" : "No";
                default: return null;
            }
        }
        
        public Robot getRobotAt(int rowIndex) {
            return rowIndex >= 0 && rowIndex < robots.size() ? robots.get(rowIndex) : null;
        }
    }

    @Override
    protected void applyTheme(Theme theme) {
        setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(theme.getColor(Theme.BORDER)),
            "Robot Management",
            0, 0,
            themeManager.getFont(-1),
            theme.getColor(Theme.FOREGROUND)
        ));

        // Apply theme to table
        if (robotTable != null) {
            robotTable.setBackground(theme.getColor(Theme.BACKGROUND));
            robotTable.setForeground(theme.getColor(Theme.FOREGROUND));
            robotTable.setFont(themeManager.getFont(0));
            robotTable.setSelectionBackground(theme.getColor(Theme.SELECTION_BACKGROUND));
            robotTable.setSelectionForeground(theme.getColor(Theme.SELECTION_FOREGROUND));
            robotTable.getTableHeader().setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
            robotTable.getTableHeader().setForeground(theme.getColor(Theme.PANEL_FOREGROUND));
        }

        // Apply theme to all components
        applyThemeToComponent(this, theme);
    }
}