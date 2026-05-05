package com.example.evsystem.controller;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.entity.Station;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargerType;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.GlobalExceptionHandler;
import com.example.evsystem.service.ChargerService;
import com.example.evsystem.service.ReservationService;
import com.example.evsystem.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllerHappyPathTest {

    private VehicleService vehicleService;
    private ChargerService chargerService;
    private ReservationService reservationService;
    private MockMvc vehicleMockMvc;
    private MockMvc chargerMockMvc;
    private MockMvc reservationMockMvc;

    @BeforeEach
    void setUp() {
        vehicleService = mock(VehicleService.class);
        chargerService = mock(ChargerService.class);
        reservationService = mock(ReservationService.class);

        vehicleMockMvc = buildMockMvc(new VehicleController(vehicleService));
        chargerMockMvc = buildMockMvc(new ChargerController(chargerService));
        reservationMockMvc = buildMockMvc(new ReservationController(reservationService));
    }

    @Test
    void createVehicleReturnsCreatedVehicleResponse() throws Exception {
        Vehicle vehicle = new Vehicle();
        setId(vehicle, 1L);
        vehicle.setBrand("Tesla");
        vehicle.setModel("Model 3");
        vehicle.setBatteryCapacity(75.0);
        vehicle.setConnectorType(ConnectorType.CCS);
        vehicle.setPlateNumber("34ABC123");

        when(vehicleService.save(any())).thenReturn(vehicle);

        vehicleMockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brand": "Tesla",
                                  "model": "Model 3",
                                  "batteryCapacity": 75.0,
                                  "connectorType": "CCS",
                                  "plateNumber": "34ABC123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Tesla"))
                .andExpect(jsonPath("$.model").value("Model 3"))
                .andExpect(jsonPath("$.batteryCapacity").value(75.0))
                .andExpect(jsonPath("$.connectorType").value("CCS"))
                .andExpect(jsonPath("$.plateNumber").value("34ABC123"));
    }

    @Test
    void createChargerReturnsCreatedChargerResponse() throws Exception {
        Station station = new Station();
        setId(station, 1L);

        Charger charger = new Charger();
        setId(charger, 10L);
        charger.setChargerCode("DC-50-03");
        charger.setChargerType(ChargerType.DC);
        charger.setPowerOutput(PowerOutput.KW_150);
        charger.setConnectorType(ConnectorType.CCS);
        charger.setPricePerKwh(5.5f);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(station);

        when(chargerService.save(any())).thenReturn(charger);

        chargerMockMvc.perform(post("/chargers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargerCode": "DC-50-03",
                                  "chargerType": "DC",
                                  "powerOutput": "KW_150",
                                  "connectorType": "CCS",
                                  "pricePerKwh": 5.5,
                                  "status": "AVAILABLE",
                                  "stationId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.chargerCode").value("DC-50-03"))
                .andExpect(jsonPath("$.chargerType").value("DC"))
                .andExpect(jsonPath("$.powerOutput").value("KW_150"))
                .andExpect(jsonPath("$.connectorType").value("CCS"))
                .andExpect(jsonPath("$.pricePerKwh").value(5.5))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.stationId").value(1));
    }

    @Test
    void createReservationReturnsReservationResponse() throws Exception {
        Vehicle vehicle = new Vehicle();
        setId(vehicle, 1L);

        Charger charger = new Charger();
        setId(charger, 2L);

        Reservation reservation = new Reservation();
        setId(reservation, 20L);
        reservation.setVehicle(vehicle);
        reservation.setCharger(charger);
        reservation.setStartTime(LocalDateTime.of(2026, 4, 30, 10, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 4, 30, 11, 0));
        reservation.setStatus(ReservationStatus.ACTIVE);

        when(reservationService.createResponse(any()))
                .thenReturn(com.example.evsystem.dto.ReservationResponse.from(reservation));

        reservationMockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "chargerId": 2,
                                  "startTime": "2026-04-30T10:00:00",
                                  "endTime": "2026-04-30T11:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.vehicleId").value(1))
                .andExpect(jsonPath("$.chargerId").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void docsRedirectsToMintlifyDocumentation() throws Exception {
        buildMockMvc(new RootController("https://egeuniversity.mintlify.app/"))
                .perform(get("/docs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://egeuniversity.mintlify.app/"));
    }

    @Test
    void docsWithTrailingSlashRedirectsToMintlifyDocumentation() throws Exception {
        buildMockMvc(new RootController("https://egeuniversity.mintlify.app/"))
                .perform(get("/docs/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://egeuniversity.mintlify.app/"));
    }

    private MockMvc buildMockMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator())
                .build();
    }

    private static LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }

    private static void setId(Object target, Long id) {
        try {
            Field idField = target.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
