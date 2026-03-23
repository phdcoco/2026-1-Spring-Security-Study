package com.gdghongik.springsecurity.global.exception;

public record ErrorResponse(
    String errorCodeName,
    String errorMessage
) {
    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.name(), errorCode.getErrorMessage());
    }
}
