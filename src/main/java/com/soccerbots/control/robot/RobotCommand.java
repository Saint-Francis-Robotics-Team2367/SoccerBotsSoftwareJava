package com.soccerbots.control.robot;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class RobotCommand {
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
    public RobotCommand() {
        this.parameters = new HashMap<>();
    }
    
    public RobotCommand(String type) {
        this.type = type;
        this.parameters = new HashMap<>();
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    public double getDoubleParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    public String getStringParameter(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }
    
    public long getLongParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
    
    @Override
    public String toString() {
        return String.format("RobotCommand{type='%s', parameters=%s}", type, parameters);
    }
}