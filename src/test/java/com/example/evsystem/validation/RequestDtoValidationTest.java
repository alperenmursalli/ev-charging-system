package com.example.evsystem.validation;

import com.example.evsystem.dto.ChargerRequest;
import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.dto.VehicleRequest;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargerType;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void vehicleRequestRejectsBlankBrandModelAndPlateAndNegativeBatteryCapacity() {
        VehicleRequest request = new VehicleRequest();
        request.setBrand(" ");
        request.setModel("");
        request.setBatteryCapacity(-10d);
        request.setConnectorType(ConnectorType.CCS);
        request.setPlateNumber(" ");

        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);

        assertTrue(hasMessage(violations, "Brand cannot be blank."));
        assertTrue(hasMessage(violations, "Model cannot be blank."));
        assertTrue(hasMessage(violations, "Battery capacity must be greater than zero."));
        assertTrue(hasMessage(violations, "Plate number cannot be blank."));
    }

    @Test
    void chargerRequestRejectsNegativePrice() {
        ChargerRequest request = new ChargerRequest();
        request.setChargerCode("CHG-1");
        request.setChargerType(ChargerType.DC);
        request.setPowerOutput(PowerOutput.KW_50);
        request.setConnectorType(ConnectorType.CCS);
        request.setPricePerKwh(-1f);
        request.setStatus(ChargerStatus.AVAILABLE);
        request.setStationId(1L);

        Set<ConstraintViolation<ChargerRequest>> violations = validator.validate(request);

        assertTrue(hasMessage(violations, "Price per kWh must be greater than zero."));
    }

    @Test
    void reservationRequestRejectsEndTimeBeforeStartTime() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setVehicleId(1L);
        request.setChargerId(2L);
        request.setStartTime(LocalDateTime.of(2026, 4, 28, 12, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 28, 11, 0));

        Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);

        assertTrue(hasMessage(violations, "Reservation end time must be after start time."));
    }

    private boolean hasMessage(Set<? extends ConstraintViolation<?>> violations, String message) {
        return violations.stream().anyMatch(violation -> message.equals(violation.getMessage()));
    }
}
