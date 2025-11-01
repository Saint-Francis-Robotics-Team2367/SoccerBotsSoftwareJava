package com.soccerbots.control.controller;

public class ControllerInput {
    private float leftStickX = 0.0f;
    private float leftStickY = 0.0f;
    private float rightStickX = 0.0f;
    private float rightStickY = 0.0f;
    private float leftTrigger = 0.0f;
    private float rightTrigger = 0.0f;
    private float dPad = 0.0f;
    private boolean[] buttons = new boolean[16];
    
    private static final float DEADZONE = 0.1f;
    private static final float MOVEMENT_THRESHOLD = 0.05f;
    
    public ControllerInput() {
        // Initialize all buttons to false
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = false;
        }
    }
    
    public float getLeftStickX() {
        return applyDeadzone(leftStickX);
    }
    
    public void setLeftStickX(float leftStickX) {
        this.leftStickX = leftStickX;
    }
    
    public float getLeftStickY() {
        return applyDeadzone(leftStickY);
    }
    
    public void setLeftStickY(float leftStickY) {
        this.leftStickY = leftStickY;
    }
    
    public float getRightStickX() {
        return applyDeadzone(rightStickX);
    }
    
    public void setRightStickX(float rightStickX) {
        this.rightStickX = rightStickX;
    }
    
    public float getRightStickY() {
        return applyDeadzone(rightStickY);
    }
    
    public void setRightStickY(float rightStickY) {
        this.rightStickY = rightStickY;
    }
    
    public float getLeftTrigger() {
        return leftTrigger;
    }
    
    public void setLeftTrigger(float leftTrigger) {
        this.leftTrigger = leftTrigger;
    }
    
    public float getRightTrigger() {
        return rightTrigger;
    }
    
    public void setRightTrigger(float rightTrigger) {
        this.rightTrigger = rightTrigger;
    }
    
    public float getDPad() {
        return dPad;
    }
    
    public void setDPad(float dPad) {
        this.dPad = dPad;
    }
    
    public boolean getButton(int index) {
        return index >= 0 && index < buttons.length ? buttons[index] : false;
    }
    
    public void setButton(int index, boolean pressed) {
        if (index >= 0 && index < buttons.length) {
            buttons[index] = pressed;
        }
    }
    
    private float applyDeadzone(float value) {
        return Math.abs(value) < DEADZONE ? 0.0f : value;
    }
    
    public double getForward() {
        return -getLeftStickY(); // Negative because Y is inverted
    }
    
    public double getSideways() {
        return getLeftStickX();
    }
    
    public double getRotation() {
        return getRightStickX();
    }
    
    public boolean hasMovement() {
        return Math.abs(getForward()) > MOVEMENT_THRESHOLD ||
               Math.abs(getSideways()) > MOVEMENT_THRESHOLD ||
               Math.abs(getRotation()) > MOVEMENT_THRESHOLD;
    }
    
    public boolean isStopCommand() {
        // Stop if no significant stick movement
        return !hasMovement();
    }
    
    @Override
    public String toString() {
        return String.format("ControllerInput{leftStick=(%.2f,%.2f), rightStick=(%.2f,%.2f), triggers=(%.2f,%.2f)}", 
                           getLeftStickX(), getLeftStickY(), getRightStickX(), getRightStickY(), 
                           leftTrigger, rightTrigger);
    }
}