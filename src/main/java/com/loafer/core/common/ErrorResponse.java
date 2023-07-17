package com.loafer.core.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse<T> {
    private int code;
    private String message;
    private T data;

    public ErrorResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}