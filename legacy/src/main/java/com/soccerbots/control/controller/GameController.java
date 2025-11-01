package com.soccerbots.control.controller;

import net.java.games.input.Controller;

public class GameController {
    private final String id;
    private final Controller controller;
    private ControllerInput lastInput;
    private long lastUpdateTime;
    private boolean isConnected;
    
    public GameController(String id, Controller controller) {
        this.id = id;
        this.controller = controller;
        this.lastInput = new ControllerInput();
        this.lastUpdateTime = System.currentTimeMillis();
        this.isConnected = true;
    }
    
    public String getId() {
        return id;
    }
    
    public Controller getController() {
        return controller;
    }
    
    public String getName() {
        return controller.getName();
    }
    
    public ControllerInput getLastInput() {
        return lastInput;
    }
    
    public void updateInput(ControllerInput input) {
        this.lastInput = input;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public boolean isConnected() {
        return isConnected && (System.currentTimeMillis() - lastUpdateTime) < 5000;
    }
    
    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }
    
    public long getTimeSinceLastUpdate() {
        return System.currentTimeMillis() - lastUpdateTime;
    }

    public String getType() {
        String name = controller.getName().toLowerCase();
        if (name.contains("dualsense") || name.contains("ps5")) {
            return "ps5";
        } else if (name.contains("dualshock") || name.contains("ps4")) {
            return "ps4";
        } else if (name.contains("xbox")) {
            return "xbox";
        } else if (name.contains("playstation")) {
            return "playstation";
        } else {
            return "gamepad";
        }
    }

    @Override
    public String toString() {
        return String.format("GameController{id='%s', name='%s', connected=%s}",
                           id, getName(), isConnected());
    }
}