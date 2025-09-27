package com.soccerbots.control.robot;

import java.util.Objects;

public class Robot {
    private final String id;
    private String name;
    private String ipAddress;
    private String status;
    private long lastSeenTime;
    private long lastCommandTime;
    private boolean isConnected;
    private String pairedControllerId;
    
    public Robot(String id, String name, String ipAddress, String status) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.status = status;
        this.lastSeenTime = System.currentTimeMillis();
        this.lastCommandTime = 0;
        this.isConnected = true;
        this.pairedControllerId = null;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        updateLastSeenTime();
    }
    
    public long getLastSeenTime() {
        return lastSeenTime;
    }
    
    public void updateLastSeenTime() {
        this.lastSeenTime = System.currentTimeMillis();
    }
    
    public long getLastCommandTime() {
        return lastCommandTime;
    }
    
    public void updateLastCommandTime() {
        this.lastCommandTime = System.currentTimeMillis();
    }
    
    public boolean isConnected() {
        return isConnected && (System.currentTimeMillis() - lastSeenTime) < 30000;
    }
    
    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }
    
    public String getPairedControllerId() {
        return pairedControllerId;
    }
    
    public void setPairedControllerId(String pairedControllerId) {
        this.pairedControllerId = pairedControllerId;
    }
    
    public boolean isPaired() {
        return pairedControllerId != null;
    }
    
    public long getTimeSinceLastCommand() {
        return lastCommandTime > 0 ? System.currentTimeMillis() - lastCommandTime : -1;
    }
    
    public long getTimeSinceLastSeen() {
        return System.currentTimeMillis() - lastSeenTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Robot robot = (Robot) o;
        return Objects.equals(id, robot.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Robot{id='%s', name='%s', ip='%s', status='%s', connected=%s, paired=%s}", 
                           id, name, ipAddress, status, isConnected(), isPaired());
    }
}