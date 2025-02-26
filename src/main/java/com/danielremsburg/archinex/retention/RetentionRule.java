package com.danielremsburg.archinex.retention;

public class RetentionRule {
    private String type;
    private String unit;
    private int value;
    private RetentionAction action;

    public RetentionRule() {} // For Gson deserialization (important!)

    public RetentionRule(String type, String unit, int value) {
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.action = RetentionAction.DELETE; // Default action
    }

    public RetentionRule(String type, String unit, int value, RetentionAction action) {
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public int getValue() {
        return value;
    }

    public RetentionAction getAction() {
        return action == null ? RetentionAction.DELETE : action;
    }

    @Override
    public String toString() {
        return "RetentionRule{" +
                "type='" + type + '\'' +
                ", unit='" + unit + '\'' +
                ", value=" + value +
                ", action=" + action +
                '}';
    }
}