package com.example.evsystem.exception;

import com.example.evsystem.controller.VehicleController;
import com.example.evsystem.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final VehicleService vehicleService = mock(VehicleService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new VehicleController(vehicleService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator())
            .build();

    @Test
    void validationErrorsAreReturnedAsJson() throws Exception {
        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brand": "",
                                  "model": " ",
                                  "batteryCapacity": -5,
                                  "connectorType": "CCS",
                                  "plateNumber": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.path").value("/vehicles"))
                .andExpect(jsonPath("$.validationErrors.brand").value("Brand cannot be blank."))
                .andExpect(jsonPath("$.validationErrors.model").value("Model cannot be blank."))
                .andExpect(jsonPath("$.validationErrors.batteryCapacity").value("Battery capacity must be greater than zero."))
                .andExpect(jsonPath("$.validationErrors.plateNumber").value("Plate number cannot be blank."));
    }

    @Test
    void businessExceptionsAreReturnedAsJson() throws Exception {
        when(vehicleService.getVehicleById(99L))
                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND, "Vehicle not found with id: 99"));

        mockMvc.perform(get("/vehicles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/vehicles/99"))
                .andExpect(jsonPath("$.validationErrors").isMap());
    }

    @Test
    void unreadableJsonErrorsAreReturnedAsJson() throws Exception {
        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brand": "Tesla",
                                  "model": "Model 3",
                                  "batteryCapacity": 75,
                                  "connectorType": "INVALID_ENUM",
                                  "plateNumber": "34ABC123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid enum value."))
                .andExpect(jsonPath("$.path").value("/vehicles"));
    }

    private static LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }
}
