package com.example.evsystem.validation;

import com.example.evsystem.dto.CreateReservationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidReservationTimeValidator implements ConstraintValidator<ValidReservationTime, CreateReservationRequest> {

    @Override
    public boolean isValid(CreateReservationRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getStartTime() == null || value.getEndTime() == null) {
            return true;
        }

        boolean valid = value.getEndTime().isAfter(value.getStartTime());
        if (valid) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("endTime")
                .addConstraintViolation();
        return false;
    }
}
