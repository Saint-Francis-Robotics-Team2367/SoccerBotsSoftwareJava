package com.soccerbots.control.gui;

import com.soccerbots.control.gui.theme.Theme;
import com.soccerbots.control.gui.theme.ThemedComponent;
import com.soccerbots.control.gui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SettingsPanel extends ThemedComponent {
    private final ThemeManager themeManager;

    private JComboBox<String> themeComboBox;
    private JSlider fontSizeSlider;
    private JLabel fontSizeLabel;
    private JLabel previewLabel;
    private JButton resetButton;

    public SettingsPanel() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        updatePreview();
    }

    private void initializeComponents() {
        setPreferredSize(new Dimension(400, 300));

        // Theme selection
        themeComboBox = new JComboBox<>();
        for (String themeName : themeManager.getAvailableThemes()) {
            themeComboBox.addItem(themeName);
        }
        themeComboBox.setSelectedItem(themeManager.getCurrentTheme().getName());

        // Font size slider
        fontSizeSlider = new JSlider(8, 72, themeManager.getGlobalFontSize());
        fontSizeSlider.setMajorTickSpacing(16);
        fontSizeSlider.setMinorTickSpacing(4);
        fontSizeSlider.setPaintTicks(true);
        fontSizeSlider.setPaintLabels(true);

        fontSizeLabel = new JLabel("Font Size: " + themeManager.getGlobalFontSize() + "pt");

        // Preview
        previewLabel = new JLabel("<html><b>Sample Text</b><br>This is how text will appear</html>");
        previewLabel.setOpaque(true);
        previewLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Reset button
        resetButton = new JButton("Reset to Defaults");
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Theme section
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(themeComboBox, gbc);

        // Font size section
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        add(fontSizeLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        add(fontSizeSlider, gbc);

        // Preview section
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(new TitledBorder("Preview"));
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        add(previewPanel, gbc);

        // Reset button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(resetButton, gbc);
    }

    private void setupEventHandlers() {
        themeComboBox.addActionListener(this::handleThemeChange);

        fontSizeSlider.addChangeListener(e -> {
            int fontSize = fontSizeSlider.getValue();
            fontSizeLabel.setText("Font Size: " + fontSize + "pt");
            themeManager.setGlobalFontSize(fontSize);
            updatePreview();
        });

        resetButton.addActionListener(e -> resetToDefaults());
    }

    private void handleThemeChange(ActionEvent e) {
        String selectedTheme = (String) themeComboBox.getSelectedItem();
        if (selectedTheme != null) {
            themeManager.setTheme(selectedTheme);
            updatePreview();
        }
    }

    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Reset all theme settings to defaults?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            themeManager.setTheme("Light");
            themeManager.setGlobalFontSize(12);

            themeComboBox.setSelectedItem("Light");
            fontSizeSlider.setValue(12);
            fontSizeLabel.setText("Font Size: 12pt");
            updatePreview();
        }
    }

    private void updatePreview() {
        Theme theme = themeManager.getCurrentTheme();
        previewLabel.setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        previewLabel.setForeground(theme.getColor(Theme.PANEL_FOREGROUND));
        previewLabel.setFont(themeManager.getFont(0));
        previewLabel.repaint();
    }

    @Override
    protected void applyTheme(Theme theme) {
        setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(theme.getColor(Theme.BORDER)),
            "Appearance Settings",
            0, 0,
            themeManager.getFont(-1),
            theme.getColor(Theme.FOREGROUND)
        ));

        // Apply theme to all components
        applyThemeToComponent(this, theme);

        // Special handling for slider
        fontSizeSlider.setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
        fontSizeSlider.setForeground(theme.getColor(Theme.PANEL_FOREGROUND));

        updatePreview();
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        super.onThemeChanged(newTheme);
        updatePreview();
    }
}