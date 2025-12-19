package com.example.reservation.reservationsystem.common.validation.timerange;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface ValidTimeRange {
    String message() default "시간 범위가 올바르지 않습니다.";

    /**
     * {@link Constraint}
     */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minMinutes() default 1;
}
