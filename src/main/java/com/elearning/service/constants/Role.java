package com.elearning.service.constants;

/**
 * User roles enumeration
 * 
 * @author Your Name
 * @version 1.0.0
 */
public enum Role {
    ADMIN("ADMIN"),
    TEACHER("TEACHER"), 
    STUDENT("STUDENT");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
