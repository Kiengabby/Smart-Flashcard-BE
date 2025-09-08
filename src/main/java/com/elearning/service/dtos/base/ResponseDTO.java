package com.elearning.service.dtos.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard response wrapper for all API responses
 * 
 * @param <T> the type of data being returned
 * @author Your Name
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(true, "Success", data, null);
    }

    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>(true, message, data, null);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(false, message, null, null);
    }

    public static <T> ResponseDTO<T> error(String message, String errorCode) {
        return new ResponseDTO<>(false, message, null, errorCode);
    }
}
