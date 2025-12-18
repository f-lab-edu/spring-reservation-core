package com.example.reservation.reservationsystem.global.error.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {
    String getCode();
    String getMessage();
    int getStatus();
}
