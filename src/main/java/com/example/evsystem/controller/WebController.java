package com.example.evsystem.controller;

import com.example.evsystem.dto.ChargingSessionResponse;
import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.dto.ReservationResponse;
import com.example.evsystem.dto.VehicleRequest;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.StationStatus;
import com.example.evsystem.service.ChargerService;
import com.example.evsystem.service.ChargingSessionService;
import com.example.evsystem.service.ReservationService;
import com.example.evsystem.service.StationService;
import com.example.evsystem.service.VehicleService;
import com.example.evsystem.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/ui")
public class WebController {

    private final VehicleService vehicleService;
    private final StationService stationService;
    private final ChargerService chargerService;
    private final ReservationService reservationService;
    private final ChargingSessionService chargingSessionService;

    public WebController(VehicleService vehicleService,
                         StationService stationService,
                         ChargerService chargerService,
                         ReservationService reservationService,
                         ChargingSessionService chargingSessionService) {
        this.vehicleService = vehicleService;
        this.stationService = stationService;
        this.chargerService = chargerService;
        this.reservationService = reservationService;
        this.chargingSessionService = chargingSessionService;
    }

    // Ana Sayfa
    @GetMapping({"", "/home"})
    public String home() {
        return "index";
    }

    // ─── VEHICLES ───
    @GetMapping("/vehicles")
    public String listVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "vehicles/list";
    }

    @GetMapping("/vehicles/add")
    public String addVehicleForm(Model model) {
        model.addAttribute("vehicle", new VehicleRequest());
        model.addAttribute("connectorTypes", ConnectorType.values());
        return "vehicles/add";
    }

    @PostMapping("/vehicles/add")
    public String addVehicle(@Valid @ModelAttribute("vehicle") VehicleRequest vehicle,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("connectorTypes", ConnectorType.values());
            return "vehicles/add";
        }
        try {
            vehicleService.save(vehicle);
            redirectAttrs.addFlashAttribute("successMsg", "Araç başarıyla eklendi!");
            return "redirect:/ui/vehicles";
        } catch (BusinessException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("connectorTypes", ConnectorType.values());
            return "vehicles/add";
        }
    }

    // ─── STATIONS ───
    @GetMapping("/stations")
    public String listStations(Model model) {
        model.addAttribute("stations", stationService.getAll());
        return "stations/list";
    }

    @GetMapping("/stations/{id}")
    public String stationDetail(@PathVariable Long id, Model model) {
        model.addAttribute("station", stationService.getById(id));
        model.addAttribute("chargers", chargerService.getByStationId(id));
        return "stations/detail";
    }

    @GetMapping("/stations/map")
    public String stationMap(Model model) {
        model.addAttribute("connectorTypes", ConnectorType.values());
        model.addAttribute("powerOutputs", PowerOutput.values());
        model.addAttribute("stationStatuses", StationStatus.values());
        return "stations/map";
    }

    // ─── RESERVATIONS ───
    @GetMapping("/reservations")
    public String listReservations(Model model) {
        model.addAttribute("reservations", reservationService.getAllResponses());
        return "reservations/list";
    }

    @GetMapping("/reservations/create")
    public String createReservationForm(@RequestParam(required = false) Long chargerId,
                                        @RequestParam(required = false) Long vehicleId,
                                        Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("chargers", chargerService.getAll());
        model.addAttribute("selectedChargerId", chargerId);
        model.addAttribute("selectedVehicleId", vehicleId);
        model.addAttribute("now", LocalDateTime.now().withSecond(0).withNano(0));
        return "reservations/create";
    }

    @PostMapping("/reservations/create")
    public String createReservation(@RequestParam Long vehicleId,
                                    @RequestParam Long chargerId,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime,
                                    Model model,
                                    RedirectAttributes redirectAttrs) {
        try {
            CreateReservationRequest request = new CreateReservationRequest();
            request.setVehicleId(vehicleId);
            request.setChargerId(chargerId);
            request.setStartTime(LocalDateTime.parse(startTime));
            request.setEndTime(LocalDateTime.parse(endTime));
            ReservationResponse response = reservationService.createResponse(request);
            redirectAttrs.addFlashAttribute("reservation", response);
            redirectAttrs.addFlashAttribute("compatible", true);
            return "redirect:/ui/reservations/" + response.getId() + "/result";
        } catch (BusinessException e) {
            model.addAttribute("errorMsg", e.getMessage());
            boolean isCompatError = e.getMessage() != null && e.getMessage().toLowerCase().contains("connector");
            model.addAttribute("compatible", !isCompatError);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("chargers", chargerService.getAll());
            model.addAttribute("selectedVehicleId", vehicleId);
            model.addAttribute("selectedChargerId", chargerId);
            model.addAttribute("now", LocalDateTime.now().withSecond(0).withNano(0));
            return "reservations/create";
        }
    }

    @GetMapping("/reservations/{id}/result")
    public String reservationResult(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("reservation")) {
            try {
                model.addAttribute("reservation", reservationService.getResponseById(id));
                model.addAttribute("compatible", true);
            } catch (BusinessException e) {
                model.addAttribute("errorMsg", e.getMessage());
                return "error";
            }
        }
        return "reservations/result";
    }

    // ─── SESSIONS ───
    @GetMapping("/sessions/start")
    public String startSessionForm(@RequestParam(required = false) Long reservationId, Model model) {
        model.addAttribute("reservations", reservationService.getAllResponses());
        model.addAttribute("selectedReservationId", reservationId);
        return "sessions/start";
    }

    @PostMapping("/sessions/start")
    public String startSession(@RequestParam Long reservationId,
                               @RequestParam(required = false) Float startBatteryLevel,
                               RedirectAttributes redirectAttrs,
                               Model model) {
        try {
            var session = chargingSessionService.startSession(reservationId, startBatteryLevel);
            return "redirect:/ui/sessions/" + session.getId() + "/end";
        } catch (BusinessException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("reservations", reservationService.getAllResponses());
            model.addAttribute("selectedReservationId", reservationId);
            return "sessions/start";
        }
    }

    @GetMapping("/sessions/{id}/end")
    public String endSessionForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("session", ChargingSessionResponse.from(chargingSessionService.getSessionById(id)));
        } catch (BusinessException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "error";
        }
        return "sessions/end";
    }

    @PostMapping("/sessions/{id}/end")
    public String endSession(@PathVariable Long id,
                             @RequestParam Float endBatteryLevel,
                             RedirectAttributes redirectAttrs,
                             Model model) {
        try {
            ChargingSessionResponse session = ChargingSessionResponse.from(
                    chargingSessionService.endSession(id, endBatteryLevel));
            redirectAttrs.addFlashAttribute("session", session);
            return "redirect:/ui/sessions/" + id + "/result";
        } catch (BusinessException e) {
            model.addAttribute("errorMsg", e.getMessage());
            try {
                model.addAttribute("session", ChargingSessionResponse.from(chargingSessionService.getSessionById(id)));
            } catch (Exception ignored) {}
            return "sessions/end";
        }
    }

    @GetMapping("/sessions/{id}/result")
    public String sessionResult(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("session")) {
            try {
                model.addAttribute("session", ChargingSessionResponse.from(chargingSessionService.getSessionById(id)));
            } catch (BusinessException e) {
                model.addAttribute("errorMsg", e.getMessage());
                return "error";
            }
        }
        return "sessions/result";
    }
}
