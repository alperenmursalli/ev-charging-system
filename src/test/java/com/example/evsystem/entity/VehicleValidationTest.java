package com.example.evsystem.entity;

import com.example.evsystem.enums.ConnectorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Vehicle buildValidVehicle() {
        Vehicle v = new Vehicle();
        v.setBrand("Tesla");
        v.setModel("Model 3");
        v.setBatteryCapacity(75.0);
        v.setConnectorType(ConnectorType.TYPE2);
        v.setPlateNumber("34ABC123");
        return v;
    }

    private boolean hasViolationOn(Set<ConstraintViolation<Vehicle>> violations, String field) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    // -----------------------------------------------------------------------
    // 1) Does validation fail when brand is blank?
    // -----------------------------------------------------------------------
    @Test
    void shouldFailValidation_whenBrandIsBlank() {
        Vehicle vehicle = buildValidVehicle();
        vehicle.setBrand("");

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "brand"));
    }

    // -----------------------------------------------------------------------
    // 2) Does validation fail when model is blank?
    // -----------------------------------------------------------------------
    @Test
    void shouldFailValidation_whenModelIsBlank() {
        Vehicle vehicle = buildValidVehicle();
        vehicle.setModel("");

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "model"));
    }

    // -----------------------------------------------------------------------
    // 3) Does validation fail when battery capacity is negative?
    // -----------------------------------------------------------------------
    @Test
    void shouldFailValidation_whenBatteryCapacityIsNegative() {
        Vehicle vehicle = buildValidVehicle();
        vehicle.setBatteryCapacity(-10.0);

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "batteryCapacity"));
    }

    // -----------------------------------------------------------------------
    // 4) Does validation fail when battery capacity is zero?
    // -----------------------------------------------------------------------
    @Test
    void shouldFailValidation_whenBatteryCapacityIsZero() {
        Vehicle vehicle = buildValidVehicle();
        vehicle.setBatteryCapacity(0.0);

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "batteryCapacity"));
    }

    // -----------------------------------------------------------------------
    // 5) Does validation fail when connector type is null?
    // -----------------------------------------------------------------------
    @Test
    void shouldFailValidation_whenConnectorTypeIsNull() {
        Vehicle vehicle = buildValidVehicle();
        vehicle.setConnectorType(null);

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "connectorType"));
    }

    // -----------------------------------------------------------------------
    // 6) Does validation fail when plate number is blank?
    // -----------------------------------------------------------------------
    @Test
    void shouldFailValidation_whenPlateNumberIsBlank() {
        Vehicle vehicle = buildValidVehicle();
        vehicle.setPlateNumber("");

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "plateNumber"));
    }

    // -----------------------------------------------------------------------
    // 7) Does validation pass when all fields are valid?
    // -----------------------------------------------------------------------
    @Test
    void shouldPassValidation_whenAllFieldsAreValid() {
        Vehicle vehicle = buildValidVehicle();

        Set<ConstraintViolation<Vehicle>> violations = validator.validate(vehicle);

        assertTrue(violations.isEmpty());
    }
}
