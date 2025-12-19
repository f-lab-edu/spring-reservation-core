package com.example.reservation.reservationsystem.global.error;

import com.example.reservation.reservationsystem.global.error.exception.BusinessException;
import com.example.reservation.reservationsystem.global.error.exception.ErrorCode;
import com.example.reservation.reservationsystem.global.error.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.example.reservation.reservationsystem.global.error.exception.GlobalErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 오류를 캐치하여 정의되어있는 exception을 응답으로 내려준다.
     * protected 접근 제어자 사용 이유
     * - spring에서 사용할 것이라는 것을 명시
     * - public으로 할 경우 다른 패키지에서 호출이 되는 문제를 막기 위해
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("handle business exception :: ", e);
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * 도메인 오류 이외의 서버 오류 unchecked exception
     */
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("handle runtime exception :: ", e);
        ErrorResponse response = ErrorResponse.builder()
                .code(UNEXPECTED_RUNTIME_EXCEPTION.getCode())
                .message(UNEXPECTED_RUNTIME_EXCEPTION.getMessage())
                .build();

        return ResponseEntity.status(UNEXPECTED_RUNTIME_EXCEPTION.getStatus()).body(response);
    }

    /**
     * controller valid exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handle method argument NotValidException :: ", e);
        ErrorResponse response = ErrorResponse.of(INVALID_INPUT_VALUE.getCode(), INVALID_INPUT_VALUE.getMessage(),
                e.getBindingResult());
        return ResponseEntity.status(INVALID_INPUT_VALUE.getStatus()).body(response);
    }

    /**
     * checked exception
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handle unknown exception :: ", e);

        ErrorResponse response = ErrorResponse.builder()
                .code(CHECKED_EXCEPTION.getCode())
                .message(CHECKED_EXCEPTION.getMessage())
                .build();

        return ResponseEntity.status(CHECKED_EXCEPTION.getStatus()).body(response);
    }
}
