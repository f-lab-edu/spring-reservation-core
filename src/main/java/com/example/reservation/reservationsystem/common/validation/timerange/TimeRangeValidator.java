package com.example.reservation.reservationsystem.common.validation.timerange;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, TimeRange> {

    private int minMinutes;

    @Override
    public void initialize(ValidTimeRange annotation) {
        this.minMinutes = annotation.minMinutes();
    }

    @Override
    public boolean isValid(TimeRange value, ConstraintValidatorContext context) {
        if (value == null)
            return true; // pass to not null validation
        if (value.startAt() == null || value.endAt() == null)
            return true; // pass to not null validation

        if (!value.endAt().isAfter(value.startAt())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("종료 시간은 시작 시간 이후여야 합니다.")
                    .addPropertyNode("endAt")
                    .addConstraintViolation();
            return false;
        }

        long minutes = Duration.between(value.startAt(), value.endAt()).toMinutes();
        if (minutes < minMinutes) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("시간 범위는 최소 " + minMinutes + "분 이상이어야 합니다.")
                    .addPropertyNode("endAt")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
