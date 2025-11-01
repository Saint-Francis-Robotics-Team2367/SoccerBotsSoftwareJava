package com.soccerbots.control.gui.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);
    private static ThemeManager instance;

    private final Map<String, Theme> themes;
    private Theme currentTheme;
    private int globalFontSize = 12;
    private final List<ThemeChangeListener> listeners;
    private final Preferences prefs;

    private ThemeManager() {
        themes = new HashMap<>();
        listeners = new ArrayList<>();
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
        initializeThemes();
        loadSettings();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void initializeThemes() {
        // Light theme
        Map<String, Color> lightColors = new HashMap<>();
        lightColors.put(Theme.BACKGROUND, Color.WHITE);
        lightColors.put(Theme.FOREGROUND, Color.BLACK);
        lightColors.put(Theme.PANEL_BACKGROUND, new Color(245, 245, 245));
        lightColors.put(Theme.PANEL_FOREGROUND, Color.BLACK);
        lightColors.put(Theme.BORDER, new Color(200, 200, 200));
        lightColors.put(Theme.BUTTON_BACKGROUND, new Color(240, 240, 240));
        lightColors.put(Theme.BUTTON_FOREGROUND, Color.BLACK);
        lightColors.put(Theme.BUTTON_HOVER, new Color(230, 230, 230));
        lightColors.put(Theme.BUTTON_PRESSED, new Color(220, 220, 220));
        lightColors.put(Theme.ACCENT, new Color(0, 120, 215));
        lightColors.put(Theme.SUCCESS, new Color(40, 167, 69));
        lightColors.put(Theme.WARNING, new Color(255, 193, 7));
        lightColors.put(Theme.ERROR, new Color(220, 53, 69));
        lightColors.put(Theme.GRAPH_LINE, new Color(0, 120, 215));
        lightColors.put(Theme.GRAPH_GRID, new Color(220, 220, 220));
        lightColors.put(Theme.GRAPH_BACKGROUND, Color.WHITE);
        lightColors.put(Theme.STATUS_CONNECTED, new Color(40, 167, 69));
        lightColors.put(Theme.STATUS_DISCONNECTED, new Color(220, 53, 69));
        lightColors.put(Theme.SELECTION_BACKGROUND, new Color(184, 207, 229));
        lightColors.put(Theme.SELECTION_FOREGROUND, Color.BLACK);
        themes.put("Light", new Theme("Light", lightColors, new Font(Font.SANS_SERIF, Font.PLAIN, 12), 12));

        // Dark theme
        Map<String, Color> darkColors = new HashMap<>();
        darkColors.put(Theme.BACKGROUND, new Color(32, 32, 32));
        darkColors.put(Theme.FOREGROUND, new Color(220, 220, 220));
        darkColors.put(Theme.PANEL_BACKGROUND, new Color(45, 45, 45));
        darkColors.put(Theme.PANEL_FOREGROUND, new Color(220, 220, 220));
        darkColors.put(Theme.BORDER, new Color(70, 70, 70));
        darkColors.put(Theme.BUTTON_BACKGROUND, new Color(60, 60, 60));
        darkColors.put(Theme.BUTTON_FOREGROUND, new Color(220, 220, 220));
        darkColors.put(Theme.BUTTON_HOVER, new Color(80, 80, 80));
        darkColors.put(Theme.BUTTON_PRESSED, new Color(100, 100, 100));
        darkColors.put(Theme.ACCENT, new Color(100, 170, 255));
        darkColors.put(Theme.SUCCESS, new Color(60, 200, 100));
        darkColors.put(Theme.WARNING, new Color(255, 215, 50));
        darkColors.put(Theme.ERROR, new Color(255, 100, 120));
        darkColors.put(Theme.GRAPH_LINE, new Color(100, 170, 255));
        darkColors.put(Theme.GRAPH_GRID, new Color(70, 70, 70));
        darkColors.put(Theme.GRAPH_BACKGROUND, new Color(40, 40, 40));
        darkColors.put(Theme.STATUS_CONNECTED, new Color(60, 200, 100));
        darkColors.put(Theme.STATUS_DISCONNECTED, new Color(255, 100, 120));
        darkColors.put(Theme.SELECTION_BACKGROUND, new Color(80, 120, 160));
        darkColors.put(Theme.SELECTION_FOREGROUND, Color.WHITE);
        themes.put("Dark", new Theme("Dark", darkColors, new Font(Font.SANS_SERIF, Font.PLAIN, 12), 12));

        // Dracula theme
        Map<String, Color> draculaColors = new HashMap<>();
        draculaColors.put(Theme.BACKGROUND, new Color(40, 42, 54));
        draculaColors.put(Theme.FOREGROUND, new Color(248, 248, 242));
        draculaColors.put(Theme.PANEL_BACKGROUND, new Color(68, 71, 90));
        draculaColors.put(Theme.PANEL_FOREGROUND, new Color(248, 248, 242));
        draculaColors.put(Theme.BORDER, new Color(98, 114, 164));
        draculaColors.put(Theme.BUTTON_BACKGROUND, new Color(68, 71, 90));
        draculaColors.put(Theme.BUTTON_FOREGROUND, new Color(248, 248, 242));
        draculaColors.put(Theme.BUTTON_HOVER, new Color(98, 114, 164));
        draculaColors.put(Theme.BUTTON_PRESSED, new Color(139, 233, 253));
        draculaColors.put(Theme.ACCENT, new Color(139, 233, 253));
        draculaColors.put(Theme.SUCCESS, new Color(80, 250, 123));
        draculaColors.put(Theme.WARNING, new Color(241, 250, 140));
        draculaColors.put(Theme.ERROR, new Color(255, 85, 85));
        draculaColors.put(Theme.GRAPH_LINE, new Color(189, 147, 249));
        draculaColors.put(Theme.GRAPH_GRID, new Color(68, 71, 90));
        draculaColors.put(Theme.GRAPH_BACKGROUND, new Color(40, 42, 54));
        draculaColors.put(Theme.STATUS_CONNECTED, new Color(80, 250, 123));
        draculaColors.put(Theme.STATUS_DISCONNECTED, new Color(255, 85, 85));
        draculaColors.put(Theme.SELECTION_BACKGROUND, new Color(98, 114, 164));
        draculaColors.put(Theme.SELECTION_FOREGROUND, new Color(248, 248, 242));
        themes.put("Dracula", new Theme("Dracula", draculaColors, new Font(Font.SANS_SERIF, Font.PLAIN, 12), 12));

        // Midnight theme
        Map<String, Color> midnightColors = new HashMap<>();
        midnightColors.put(Theme.BACKGROUND, new Color(15, 15, 20));
        midnightColors.put(Theme.FOREGROUND, new Color(200, 200, 220));
        midnightColors.put(Theme.PANEL_BACKGROUND, new Color(25, 25, 35));
        midnightColors.put(Theme.PANEL_FOREGROUND, new Color(200, 200, 220));
        midnightColors.put(Theme.BORDER, new Color(50, 50, 70));
        midnightColors.put(Theme.BUTTON_BACKGROUND, new Color(35, 35, 50));
        midnightColors.put(Theme.BUTTON_FOREGROUND, new Color(200, 200, 220));
        midnightColors.put(Theme.BUTTON_HOVER, new Color(50, 50, 70));
        midnightColors.put(Theme.BUTTON_PRESSED, new Color(70, 70, 100));
        midnightColors.put(Theme.ACCENT, new Color(100, 150, 255));
        midnightColors.put(Theme.SUCCESS, new Color(50, 200, 150));
        midnightColors.put(Theme.WARNING, new Color(255, 200, 50));
        midnightColors.put(Theme.ERROR, new Color(255, 120, 120));
        midnightColors.put(Theme.GRAPH_LINE, new Color(120, 180, 255));
        midnightColors.put(Theme.GRAPH_GRID, new Color(40, 40, 60));
        midnightColors.put(Theme.GRAPH_BACKGROUND, new Color(20, 20, 30));
        midnightColors.put(Theme.STATUS_CONNECTED, new Color(50, 200, 150));
        midnightColors.put(Theme.STATUS_DISCONNECTED, new Color(255, 120, 120));
        midnightColors.put(Theme.SELECTION_BACKGROUND, new Color(60, 80, 140));
        midnightColors.put(Theme.SELECTION_FOREGROUND, Color.WHITE);
        themes.put("Midnight", new Theme("Midnight", midnightColors, new Font(Font.SANS_SERIF, Font.PLAIN, 12), 12));

        // Futuristic theme
        Map<String, Color> futuristicColors = new HashMap<>();
        futuristicColors.put(Theme.BACKGROUND, new Color(8, 8, 16));
        futuristicColors.put(Theme.FOREGROUND, new Color(0, 255, 255));
        futuristicColors.put(Theme.PANEL_BACKGROUND, new Color(16, 16, 32));
        futuristicColors.put(Theme.PANEL_FOREGROUND, new Color(0, 255, 255));
        futuristicColors.put(Theme.BORDER, new Color(0, 200, 200));
        futuristicColors.put(Theme.BUTTON_BACKGROUND, new Color(0, 50, 50));
        futuristicColors.put(Theme.BUTTON_FOREGROUND, new Color(0, 255, 255));
        futuristicColors.put(Theme.BUTTON_HOVER, new Color(0, 80, 80));
        futuristicColors.put(Theme.BUTTON_PRESSED, new Color(0, 120, 120));
        futuristicColors.put(Theme.ACCENT, new Color(255, 0, 255));
        futuristicColors.put(Theme.SUCCESS, new Color(0, 255, 100));
        futuristicColors.put(Theme.WARNING, new Color(255, 255, 0));
        futuristicColors.put(Theme.ERROR, new Color(255, 50, 50));
        futuristicColors.put(Theme.GRAPH_LINE, new Color(0, 255, 200));
        futuristicColors.put(Theme.GRAPH_GRID, new Color(0, 100, 100));
        futuristicColors.put(Theme.GRAPH_BACKGROUND, new Color(5, 5, 10));
        futuristicColors.put(Theme.STATUS_CONNECTED, new Color(0, 255, 100));
        futuristicColors.put(Theme.STATUS_DISCONNECTED, new Color(255, 50, 50));
        futuristicColors.put(Theme.SELECTION_BACKGROUND, new Color(80, 0, 80));
        futuristicColors.put(Theme.SELECTION_FOREGROUND, new Color(0, 255, 255));
        themes.put("Futuristic", new Theme("Futuristic", futuristicColors, new Font(Font.MONOSPACED, Font.PLAIN, 12), 12));

        // Vintage theme
        Map<String, Color> vintageColors = new HashMap<>();
        vintageColors.put(Theme.BACKGROUND, new Color(245, 235, 220));
        vintageColors.put(Theme.FOREGROUND, new Color(101, 67, 33));
        vintageColors.put(Theme.PANEL_BACKGROUND, new Color(238, 220, 190));
        vintageColors.put(Theme.PANEL_FOREGROUND, new Color(101, 67, 33));
        vintageColors.put(Theme.BORDER, new Color(160, 120, 90));
        vintageColors.put(Theme.BUTTON_BACKGROUND, new Color(210, 180, 140));
        vintageColors.put(Theme.BUTTON_FOREGROUND, new Color(101, 67, 33));
        vintageColors.put(Theme.BUTTON_HOVER, new Color(205, 175, 135));
        vintageColors.put(Theme.BUTTON_PRESSED, new Color(200, 170, 130));
        vintageColors.put(Theme.ACCENT, new Color(139, 69, 19));
        vintageColors.put(Theme.SUCCESS, new Color(85, 107, 47));
        vintageColors.put(Theme.WARNING, new Color(218, 165, 32));
        vintageColors.put(Theme.ERROR, new Color(178, 34, 34));
        vintageColors.put(Theme.GRAPH_LINE, new Color(139, 69, 19));
        vintageColors.put(Theme.GRAPH_GRID, new Color(205, 175, 135));
        vintageColors.put(Theme.GRAPH_BACKGROUND, new Color(245, 235, 220));
        vintageColors.put(Theme.STATUS_CONNECTED, new Color(85, 107, 47));
        vintageColors.put(Theme.STATUS_DISCONNECTED, new Color(178, 34, 34));
        vintageColors.put(Theme.SELECTION_BACKGROUND, new Color(160, 120, 90));
        vintageColors.put(Theme.SELECTION_FOREGROUND, Color.WHITE);
        themes.put("Vintage", new Theme("Vintage", vintageColors, new Font(Font.SERIF, Font.PLAIN, 12), 12));

        // High Contrast theme
        Map<String, Color> highContrastColors = new HashMap<>();
        highContrastColors.put(Theme.BACKGROUND, Color.BLACK);
        highContrastColors.put(Theme.FOREGROUND, Color.WHITE);
        highContrastColors.put(Theme.PANEL_BACKGROUND, Color.BLACK);
        highContrastColors.put(Theme.PANEL_FOREGROUND, Color.WHITE);
        highContrastColors.put(Theme.BORDER, Color.WHITE);
        highContrastColors.put(Theme.BUTTON_BACKGROUND, new Color(50, 50, 50));
        highContrastColors.put(Theme.BUTTON_FOREGROUND, Color.WHITE);
        highContrastColors.put(Theme.BUTTON_HOVER, new Color(100, 100, 100));
        highContrastColors.put(Theme.BUTTON_PRESSED, new Color(150, 150, 150));
        highContrastColors.put(Theme.ACCENT, Color.YELLOW);
        highContrastColors.put(Theme.SUCCESS, Color.GREEN);
        highContrastColors.put(Theme.WARNING, Color.YELLOW);
        highContrastColors.put(Theme.ERROR, Color.RED);
        highContrastColors.put(Theme.GRAPH_LINE, Color.YELLOW);
        highContrastColors.put(Theme.GRAPH_GRID, new Color(100, 100, 100));
        highContrastColors.put(Theme.GRAPH_BACKGROUND, Color.BLACK);
        highContrastColors.put(Theme.STATUS_CONNECTED, Color.GREEN);
        highContrastColors.put(Theme.STATUS_DISCONNECTED, Color.RED);
        highContrastColors.put(Theme.SELECTION_BACKGROUND, Color.WHITE);
        highContrastColors.put(Theme.SELECTION_FOREGROUND, Color.BLACK);
        themes.put("High Contrast", new Theme("High Contrast", highContrastColors, new Font(Font.SANS_SERIF, Font.BOLD, 12), 12));

        currentTheme = themes.get("Light");
    }

    public void setTheme(String themeName) {
        Theme newTheme = themes.get(themeName);
        if (newTheme != null && newTheme != currentTheme) {
            currentTheme = newTheme;
            saveSettings();
            notifyThemeChanged();
            logger.info("Theme changed to: {}", themeName);
        }
    }

    public void setGlobalFontSize(int fontSize) {
        if (fontSize != globalFontSize && fontSize >= 8 && fontSize <= 72) {
            globalFontSize = fontSize;
            saveSettings();
            notifyThemeChanged();
            logger.info("Global font size changed to: {}", fontSize);
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public int getGlobalFontSize() {
        return globalFontSize;
    }

    public Font getFont(int relativeSize) {
        return currentTheme.getFont(globalFontSize + relativeSize);
    }

    public Font getFont(int style, int relativeSize) {
        return currentTheme.getFont(style, globalFontSize + relativeSize);
    }

    public Set<String> getAvailableThemes() {
        return themes.keySet();
    }

    public void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyThemeChanged() {
        SwingUtilities.invokeLater(() -> {
            for (ThemeChangeListener listener : listeners) {
                try {
                    listener.onThemeChanged(currentTheme);
                } catch (Exception e) {
                    logger.error("Error notifying theme change listener", e);
                }
            }
        });
    }

    private void saveSettings() {
        prefs.put("theme", currentTheme.getName());
        prefs.putInt("fontSize", globalFontSize);
    }

    private void loadSettings() {
        String themeName = prefs.get("theme", "Light");
        globalFontSize = prefs.getInt("fontSize", 12);

        Theme savedTheme = themes.get(themeName);
        if (savedTheme != null) {
            currentTheme = savedTheme;
        }
    }

    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }
}