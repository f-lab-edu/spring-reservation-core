package com.example.reservation.reservationsystem.global.error.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Getter
@Builder
public class ErrorResponse {
    private final String code;
    private final String message;

    @JsonInclude(value = NON_EMPTY)
    private final List<ValidError> errors;

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorResponse of(String code, String message, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .errors(ValidError.of(bindingResult))
                .build();
    }


    @Getter
    @Builder
    public static class ValidError {
        private final String field;
        private final String value;
        private final String reason;

        public static List<ValidError> of(final String field, final String value, final String reason) {
            List<ValidError> validErrors = new ArrayList<>();
            validErrors.add(ValidError.builder()
                    .field(field)
                    .value(value)
                    .reason(reason)
                    .build());

            return validErrors;
        }

        public static List<ValidError> of(final BindingResult bindingResult) {
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            return fieldErrors.stream()
                    .map(error -> new ValidError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }
}
