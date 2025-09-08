package com.elearning.service.constants;

/**
 * Active status enumeration
 * 
 * @author Your Name
 * @version 1.0.0
 */
public enum ActiveStatus {
    ACTIVE(1),
    INACTIVE(0),
    DELETED(-1);

    private final int value;

    ActiveStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ActiveStatus fromValue(int value) {
        for (ActiveStatus status : ActiveStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}
