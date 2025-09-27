package com.soccerbots.control.gui.theme;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Theme {
    private final String name;
    private final Map<String, Color> colors;
    private final Font baseFont;
    private final int baseFontSize;

    public Theme(String name, Map<String, Color> colors, Font baseFont, int baseFontSize) {
        this.name = name;
        this.colors = new HashMap<>(colors);
        this.baseFont = baseFont;
        this.baseFontSize = baseFontSize;
    }

    public String getName() {
        return name;
    }

    public Color getColor(String key) {
        return colors.getOrDefault(key, Color.BLACK);
    }

    public Font getFont(int size) {
        return baseFont.deriveFont((float) size);
    }

    public Font getFont(int style, int size) {
        return baseFont.deriveFont(style, (float) size);
    }

    public Font getBaseFont() {
        return getFont(baseFontSize);
    }

    public int getBaseFontSize() {
        return baseFontSize;
    }

    // Common color keys
    public static final String BACKGROUND = "background";
    public static final String FOREGROUND = "foreground";
    public static final String PANEL_BACKGROUND = "panelBackground";
    public static final String PANEL_FOREGROUND = "panelForeground";
    public static final String BORDER = "border";
    public static final String BUTTON_BACKGROUND = "buttonBackground";
    public static final String BUTTON_FOREGROUND = "buttonForeground";
    public static final String BUTTON_HOVER = "buttonHover";
    public static final String BUTTON_PRESSED = "buttonPressed";
    public static final String ACCENT = "accent";
    public static final String SUCCESS = "success";
    public static final String WARNING = "warning";
    public static final String ERROR = "error";
    public static final String GRAPH_LINE = "graphLine";
    public static final String GRAPH_GRID = "graphGrid";
    public static final String GRAPH_BACKGROUND = "graphBackground";
    public static final String STATUS_CONNECTED = "statusConnected";
    public static final String STATUS_DISCONNECTED = "statusDisconnected";
    public static final String SELECTION_BACKGROUND = "selectionBackground";
    public static final String SELECTION_FOREGROUND = "selectionForeground";
}