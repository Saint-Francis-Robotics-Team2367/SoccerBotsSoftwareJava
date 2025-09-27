package com.soccerbots.control.gui.monitoring;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SystemLogPanel extends JPanel {
    private static final int MAX_LOG_ENTRIES = 1000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private JTextPane logTextPane;
    private StyledDocument logDocument;
    private final ConcurrentLinkedQueue<LogEntry> logQueue;
    private Timer updateTimer;

    private Style infoStyle;
    private Style warnStyle;
    private Style errorStyle;
    private Style debugStyle;
    private Style timeStyle;

    public SystemLogPanel() {
        logQueue = new ConcurrentLinkedQueue<>();
        initializeComponents();
        setupStyles();
        startUpdateTimer();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("System Log"));
        setPreferredSize(new Dimension(400, 200));

        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        logTextPane.setBackground(new Color(25, 25, 25));
        logTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        logDocument = logTextPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(logTextPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearLog());

        JCheckBox autoScrollBox = new JCheckBox("Auto Scroll", true);
        autoScrollBox.addActionListener(e -> {
            if (autoScrollBox.isSelected()) {
                scrollToBottom();
            }
        });

        controlPanel.add(clearButton);
        controlPanel.add(autoScrollBox);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void setupStyles() {
        timeStyle = logDocument.addStyle("time", null);
        StyleConstants.setForeground(timeStyle, new Color(150, 150, 150));
        StyleConstants.setFontSize(timeStyle, 10);

        infoStyle = logDocument.addStyle("info", null);
        StyleConstants.setForeground(infoStyle, new Color(200, 200, 200));

        warnStyle = logDocument.addStyle("warn", null);
        StyleConstants.setForeground(warnStyle, new Color(255, 193, 7));
        StyleConstants.setBold(warnStyle, true);

        errorStyle = logDocument.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, new Color(255, 100, 120));
        StyleConstants.setBold(errorStyle, true);

        debugStyle = logDocument.addStyle("debug", null);
        StyleConstants.setForeground(debugStyle, new Color(120, 120, 120));
        StyleConstants.setItalic(debugStyle, true);
    }

    private void startUpdateTimer() {
        updateTimer = new Timer(100, e -> processLogQueue());
        updateTimer.start();
    }

    public void addLogEntry(LogLevel level, String message) {
        LogEntry entry = new LogEntry(LocalDateTime.now(), level, message);
        logQueue.offer(entry);

        // Remove old entries if queue gets too large
        while (logQueue.size() > MAX_LOG_ENTRIES) {
            logQueue.poll();
        }
    }

    private void processLogQueue() {
        if (logQueue.isEmpty()) return;

        boolean needsScroll = isScrolledToBottom();

        while (!logQueue.isEmpty()) {
            LogEntry entry = logQueue.poll();
            appendLogEntry(entry);
        }

        if (needsScroll) {
            scrollToBottom();
        }
    }

    private void appendLogEntry(LogEntry entry) {
        try {
            // Add timestamp
            logDocument.insertString(logDocument.getLength(),
                entry.timestamp.format(TIME_FORMAT) + " ", timeStyle);

            // Add level indicator
            String levelIndicator = "[" + entry.level.name() + "] ";
            Style levelStyle = getLevelStyle(entry.level);
            logDocument.insertString(logDocument.getLength(), levelIndicator, levelStyle);

            // Add message
            logDocument.insertString(logDocument.getLength(),
                entry.message + "\n", getLevelStyle(entry.level));

        } catch (BadLocationException e) {
            // Ignore, shouldn't happen
        }
    }

    private Style getLevelStyle(LogLevel level) {
        switch (level) {
            case ERROR: return errorStyle;
            case WARN: return warnStyle;
            case DEBUG: return debugStyle;
            case INFO:
            default: return infoStyle;
        }
    }

    private boolean isScrolledToBottom() {
        JScrollBar verticalScrollBar = ((JScrollPane) logTextPane.getParent().getParent()).getVerticalScrollBar();
        return verticalScrollBar.getValue() >= verticalScrollBar.getMaximum() - verticalScrollBar.getVisibleAmount() - 10;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            logTextPane.setCaretPosition(logDocument.getLength());
        });
    }

    private void clearLog() {
        try {
            logDocument.remove(0, logDocument.getLength());
            logQueue.clear();
        } catch (BadLocationException e) {
            // Ignore
        }
    }

    @Override
    public void removeNotify() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.removeNotify();
    }

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    private static class LogEntry {
        final LocalDateTime timestamp;
        final LogLevel level;
        final String message;

        LogEntry(LocalDateTime timestamp, LogLevel level, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }
    }

    // Static methods for easy logging from anywhere in the application
    private static SystemLogPanel instance;

    public static void setInstance(SystemLogPanel panel) {
        instance = panel;
    }

    public static void logInfo(String message) {
        if (instance != null) {
            instance.addLogEntry(LogLevel.INFO, message);
        }
    }

    public static void logWarn(String message) {
        if (instance != null) {
            instance.addLogEntry(LogLevel.WARN, message);
        }
    }

    public static void logError(String message) {
        if (instance != null) {
            instance.addLogEntry(LogLevel.ERROR, message);
        }
    }

    public static void logDebug(String message) {
        if (instance != null) {
            instance.addLogEntry(LogLevel.DEBUG, message);
        }
    }
}