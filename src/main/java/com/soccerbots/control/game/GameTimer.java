package com.soccerbots.control.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GameTimer {
    private static final Logger logger = LoggerFactory.getLogger(GameTimer.class);

    public enum TimerState {
        STOPPED, RUNNING, PAUSED, FINISHED
    }

    public interface GameTimerListener {
        void onTimerStateChanged(TimerState newState, Duration remainingTime);
        void onTimerFinished();
        void onTimerTick(Duration remainingTime);
    }

    private final List<GameTimerListener> listeners;
    private Timer swingTimer;
    private Duration gameDuration;
    private Duration remainingTime;
    private TimerState currentState;
    private LocalTime lastTickTime;

    public GameTimer() {
        this.listeners = new ArrayList<>();
        this.currentState = TimerState.STOPPED;
        this.gameDuration = Duration.ofMinutes(10); // Default 10 minutes
        this.remainingTime = this.gameDuration;
    }

    public void addListener(GameTimerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameTimerListener listener) {
        listeners.remove(listener);
    }

    public void setGameDuration(Duration duration) {
        if (currentState == TimerState.STOPPED) {
            this.gameDuration = duration;
            this.remainingTime = duration;
            notifyListeners();
        } else {
            logger.warn("Cannot change game duration while timer is active");
        }
    }

    public void startTimer() {
        if (currentState == TimerState.STOPPED || currentState == TimerState.PAUSED) {
            logger.info("Starting game timer with {} remaining", formatDuration(remainingTime));

            currentState = TimerState.RUNNING;
            lastTickTime = LocalTime.now();

            if (swingTimer == null) {
                swingTimer = new Timer(100, this::handleTick); // Update every 100ms
            }
            swingTimer.start();

            notifyStateChanged();
        }
    }

    public void pauseTimer() {
        if (currentState == TimerState.RUNNING) {
            logger.info("Pausing game timer");
            currentState = TimerState.PAUSED;

            if (swingTimer != null) {
                swingTimer.stop();
            }

            notifyStateChanged();
        }
    }

    public void stopTimer() {
        logger.info("Stopping game timer");
        currentState = TimerState.STOPPED;
        remainingTime = gameDuration;

        if (swingTimer != null) {
            swingTimer.stop();
        }

        notifyStateChanged();
    }

    public void resetTimer() {
        stopTimer();
        remainingTime = gameDuration;
        notifyStateChanged();
    }

    private void handleTick(java.awt.event.ActionEvent e) {
        if (currentState != TimerState.RUNNING) {
            return;
        }

        LocalTime now = LocalTime.now();
        Duration elapsed = Duration.between(lastTickTime, now);
        lastTickTime = now;

        remainingTime = remainingTime.minus(elapsed);

        if (remainingTime.isNegative() || remainingTime.isZero()) {
            // Timer finished
            remainingTime = Duration.ZERO;
            currentState = TimerState.FINISHED;

            if (swingTimer != null) {
                swingTimer.stop();
            }

            logger.info("Game timer finished");
            notifyTimerFinished();
            notifyStateChanged();
        } else {
            // Regular tick
            notifyTimerTick();
        }
    }

    private void notifyStateChanged() {
        for (GameTimerListener listener : listeners) {
            try {
                listener.onTimerStateChanged(currentState, remainingTime);
            } catch (Exception e) {
                logger.error("Error notifying timer state change", e);
            }
        }
    }

    private void notifyTimerFinished() {
        for (GameTimerListener listener : listeners) {
            try {
                listener.onTimerFinished();
            } catch (Exception e) {
                logger.error("Error notifying timer finished", e);
            }
        }
    }

    private void notifyTimerTick() {
        for (GameTimerListener listener : listeners) {
            try {
                listener.onTimerTick(remainingTime);
            } catch (Exception e) {
                logger.error("Error notifying timer tick", e);
            }
        }
    }

    private void notifyListeners() {
        notifyStateChanged();
    }

    // Getters
    public TimerState getCurrentState() {
        return currentState;
    }

    public Duration getRemainingTime() {
        return remainingTime;
    }

    public Duration getGameDuration() {
        return gameDuration;
    }

    public Duration getElapsedTime() {
        return gameDuration.minus(remainingTime);
    }

    public double getProgressPercentage() {
        if (gameDuration.isZero()) {
            return 0.0;
        }
        return (double) getElapsedTime().toMillis() / gameDuration.toMillis() * 100.0;
    }

    public String getFormattedRemainingTime() {
        return formatDuration(remainingTime);
    }

    public String getFormattedElapsedTime() {
        return formatDuration(getElapsedTime());
    }

    public static String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void shutdown() {
        if (swingTimer != null) {
            swingTimer.stop();
        }
        listeners.clear();
    }
}