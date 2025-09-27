package com.soccerbots.control.gui.theme;

import javax.swing.*;
import java.awt.*;

public abstract class ThemedComponent extends JPanel implements ThemeManager.ThemeChangeListener {
    protected final ThemeManager themeManager;

    public ThemedComponent() {
        this.themeManager = ThemeManager.getInstance();
        this.themeManager.addThemeChangeListener(this);
        applyTheme(this.themeManager.getCurrentTheme());
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        applyTheme(newTheme);
        SwingUtilities.invokeLater(this::repaint);
    }

    protected abstract void applyTheme(Theme theme);

    protected void applyThemeToComponent(JComponent component, Theme theme) {
        if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setBackground(theme.getColor(Theme.BUTTON_BACKGROUND));
            button.setForeground(theme.getColor(Theme.BUTTON_FOREGROUND));
            button.setFont(themeManager.getFont(0));
            button.setBorder(BorderFactory.createLineBorder(theme.getColor(Theme.BORDER)));
        } else if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            label.setForeground(theme.getColor(Theme.FOREGROUND));
            label.setFont(themeManager.getFont(0));
        } else if (component instanceof JTextField || component instanceof JPasswordField) {
            component.setBackground(theme.getColor(Theme.BACKGROUND));
            component.setForeground(theme.getColor(Theme.FOREGROUND));
            component.setFont(themeManager.getFont(0));
            component.setBorder(BorderFactory.createLineBorder(theme.getColor(Theme.BORDER)));
        } else if (component instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) component;
            combo.setBackground(theme.getColor(Theme.BUTTON_BACKGROUND));
            combo.setForeground(theme.getColor(Theme.BUTTON_FOREGROUND));
            combo.setFont(themeManager.getFont(0));
        } else if (component instanceof JRadioButton || component instanceof JCheckBox) {
            component.setBackground(theme.getColor(Theme.PANEL_BACKGROUND));
            component.setForeground(theme.getColor(Theme.PANEL_FOREGROUND));
            component.setFont(themeManager.getFont(0));
        }

        // Apply to all child components recursively
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                if (child instanceof JComponent) {
                    applyThemeToComponent((JComponent) child, theme);
                }
            }
        }
    }

    @Override
    public void removeNotify() {
        themeManager.removeThemeChangeListener(this);
        super.removeNotify();
    }
}