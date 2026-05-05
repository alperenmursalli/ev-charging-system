(function () {
  const bootstrap = window.stationMapBootstrap || {};
  const labels = bootstrap.labels || {};
  const state = {
    map: null,
    stations: [],
    markers: [],
    markerByStationId: new Map(),
    chargersByStationId: new Map(),
    selectedStationId: null,
    userPosition: null,
    userAccuracyMeters: null,
    userMarker: null,
    manualAdjustMode: false,
    directionsService: null,
    directionsRenderer: null,
    googleLoaded: false,
    routeSummary: null
  };

  const dom = {
    list: document.getElementById("station-list"),
    detailPanel: document.getElementById("detail-panel"),
    detailContent: document.getElementById("detail-content"),
    detailClose: document.getElementById("detail-close"),
    locate: document.getElementById("btn-locate"),
    adjustLocation: document.getElementById("btn-adjust-location"),
    clearRoute: document.getElementById("clear-route"),
    locationDebug: document.getElementById("location-debug"),
    emptyState: document.getElementById("map-empty-state"),
    apiKeyInput: document.getElementById("api-key-input"),
    applyApiKey: document.getElementById("apply-api-key"),
    discoveryForm: document.getElementById("discovery-form"),
    filterStatus: document.getElementById("filter-status"),
    resetFilters: document.getElementById("reset-filters")
  };

  function selectedApiKey() {
    return (bootstrap.apiKey || "").trim();
  }

  function label(key, fallback) {
    return labels[key] || fallback;
  }

  function formatLabel(key, fallback) {
    const template = label(key, fallback);
    const values = Array.prototype.slice.call(arguments, 2);
    return values.reduce((result, value, index) => result.replace(`{${index}}`, value), template);
  }

  function markerColor(status) {
    if (status === "AVAILABLE") {
      return "#16a34a";
    }
    if (status === "OCCUPIED") {
      return "#ca8a04";
    }
    return "#dc2626";
  }

  function badgeClass(status) {
    if (status === "AVAILABLE") {
      return "badge-green";
    }
    if (status === "OCCUPIED") {
      return "badge-yellow";
    }
    return "badge-red";
  }

  function mapStatusLabel(status) {
    if (status === "AVAILABLE") {
      return label("available", "Available");
    }
    if (status === "OCCUPIED") {
      return label("occupied", "Occupied");
    }
    return label("offline", "Offline");
  }

  function buildDiscoveryUrl() {
    const params = new URLSearchParams();
    const formData = new FormData(dom.discoveryForm);

    for (const [key, value] of formData.entries()) {
      if (value != null && String(value).trim() !== "") {
        params.set(key, String(value).trim());
      }
    }

    if (state.userPosition) {
      params.set("userLatitude", String(state.userPosition.lat));
      params.set("userLongitude", String(state.userPosition.lng));
    }

    const query = params.toString();
    return query ? `/stations/discovery?${query}` : "/stations/discovery";
  }

  async function loadStations() {
    dom.filterStatus.textContent = label("loadingStations", "Loading stations...");
    const response = await fetch(buildDiscoveryUrl());
    if (!response.ok) {
      throw new Error(label("stationsLoadError", "Failed to load stations."));
    }

    state.stations = await response.json();
    if (!state.stations.some((station) => station.id === state.selectedStationId)) {
      state.selectedStationId = null;
      dom.detailPanel.classList.remove("visible");
    }
    renderStationList();
    renderMarkers();

    dom.filterStatus.textContent = formatLabel("stationsFound", "{0} stations found.", state.stations.length);
  }

  function renderStationList() {
    if (!state.stations.length) {
      dom.list.innerHTML = `<div class="text-muted" style="padding:1rem;text-align:center;">${escapeHtml(label("noStationsFound", "No stations matched the filters."))}</div>`;
      return;
    }

    dom.list.innerHTML = state.stations.map((station) => {
      const isSelected = station.id === state.selectedStationId ? " selected" : "";
      return `
        <div class="station-item${isSelected}" data-station-id="${station.id}">
          <div class="station-name">
            <span class="dot ${badgeClass(station.status) === "badge-green" ? "dot-green" : badgeClass(station.status) === "badge-yellow" ? "dot-yellow" : "dot-red"}"></span>
            ${escapeHtml(station.name)}
          </div>
          <div class="station-address">${escapeHtml(station.address)}</div>
          <div class="station-meta">
            <span class="badge ${badgeClass(station.status)}">${mapStatusLabel(station.status)}</span>
            <span class="badge badge-blue">${escapeHtml(formatLabel("availableCount", "{0}/{1} available", station.availableChargerCount, station.totalChargerCount))}</span>
            ${station.distanceKm != null ? `<span class="badge badge-gray">${escapeHtml(formatLabel("distanceKm", "{0} km", station.distanceKm))}</span>` : ""}
            ${station.minPricePerKwh != null ? `<span class="badge badge-gray">${escapeHtml(formatLabel("priceFrom", "From ₺{0}/kWh", station.minPricePerKwh))}</span>` : ""}
          </div>
        </div>
      `;
    }).join("");

    dom.list.querySelectorAll(".station-item").forEach((element) => {
      element.addEventListener("click", () => {
        const stationId = Number(element.dataset.stationId);
        focusStation(stationId);
      });
    });
  }

  function renderMarkers() {
    if (!state.googleLoaded || !state.map) {
      return;
    }

    state.markers.forEach((marker) => marker.setMap(null));
    state.markers = [];
    state.markerByStationId.clear();

    const bounds = new google.maps.LatLngBounds();
    let hasPoint = false;

    state.stations.forEach((station) => {
      if (station.latitude == null || station.longitude == null) {
        return;
      }

      const marker = new google.maps.Marker({
        position: { lat: station.latitude, lng: station.longitude },
        map: state.map,
        title: station.name,
        icon: {
          path: google.maps.SymbolPath.CIRCLE,
          scale: 11,
          fillColor: markerColor(station.status),
          fillOpacity: 1,
          strokeColor: "#ffffff",
          strokeWeight: 2.5
        }
      });

      marker.addListener("click", () => focusStation(station.id));
      state.markers.push(marker);
      state.markerByStationId.set(station.id, marker);
      bounds.extend(marker.getPosition());
      hasPoint = true;
    });

    if (state.userMarker) {
      bounds.extend(state.userMarker.getPosition());
      hasPoint = true;
    }

    if (hasPoint) {
      state.map.fitBounds(bounds, 80);
    }
  }

  function renderLocationDebug() {
    if (!dom.locationDebug) {
      return;
    }
    if (!state.userPosition) {
      dom.locationDebug.textContent = label("locationDebugEmpty", "Location not selected yet.");
      return;
    }
    const accuracy = state.userAccuracyMeters != null ? Math.round(state.userAccuracyMeters) : "-";
    dom.locationDebug.innerHTML = `<strong>${escapeHtml(formatLabel(
      "locationDebugSet",
      "Lat: {0}, Lng: {1}, Accuracy: {2} m",
      state.userPosition.lat.toFixed(6),
      state.userPosition.lng.toFixed(6),
      accuracy
    ))}</strong>`;
  }

  async function loadChargers(stationId) {
    const station = state.stations.find((item) => item.id === stationId);
    if (station && Array.isArray(station.chargers) && station.chargers.length) {
      state.chargersByStationId.set(stationId, station.chargers);
      return station.chargers;
    }

    if (state.chargersByStationId.has(stationId)) {
      return state.chargersByStationId.get(stationId);
    }

    const response = await fetch(`/stations/${stationId}/chargers`);
    if (!response.ok) {
      throw new Error(label("chargerDetailsError", "Failed to fetch charger details."));
    }

    const chargers = await response.json();
    state.chargersByStationId.set(stationId, chargers);
    return chargers;
  }

  async function focusStation(stationId) {
    const station = state.stations.find((item) => item.id === stationId);
    if (!station) {
      return;
    }

    state.selectedStationId = stationId;
    state.routeSummary = null;
    renderStationList();

    const marker = state.markerByStationId.get(stationId);
    if (marker && state.map) {
      state.map.panTo(marker.getPosition());
      state.map.setZoom(Math.max(state.map.getZoom() || 14, 14));
    }

    renderDetailPanelLoading(station);

    try {
      const chargers = await loadChargers(stationId);
      renderDetailPanel(station, chargers);
      if (state.userPosition && state.googleLoaded && state.directionsService && state.directionsRenderer) {
        drawRoute(station, chargers, true);
      }
    } catch (error) {
      renderDetailPanelError(station, error.message);
    }
  }

  function renderDetailPanelLoading(station) {
    dom.detailPanel.classList.add("visible");
    dom.detailContent.innerHTML = `
      <h2 style="font-size:1rem;padding-right:1.4rem;">${escapeHtml(station.name)}</h2>
      <p class="text-muted" style="margin-top:0.45rem;">${escapeHtml(station.address)}</p>
      <div class="detail-section">
        <h3>${escapeHtml(label("detailSection", "Details"))}</h3>
        <p class="text-muted">${escapeHtml(label("loadingChargerDetails", "Loading charger details..."))}</p>
      </div>
    `;
  }

  function renderDetailPanelError(station, message) {
    dom.detailPanel.classList.add("visible");
    dom.detailContent.innerHTML = `
      <h2 style="font-size:1rem;padding-right:1.4rem;">${escapeHtml(station.name)}</h2>
      <p class="text-muted" style="margin-top:0.45rem;">${escapeHtml(station.address)}</p>
      <div class="alert alert-danger" style="margin-top:1rem;">${escapeHtml(message)}</div>
    `;
  }

  function renderDetailPanel(station, chargers) {
    const chargerMarkup = chargers.length
      ? chargers.map((charger) => `
          <div class="charger-card">
            <div class="charger-card-head">
              <div class="charger-code">${escapeHtml(charger.chargerCode || "Charger")}</div>
              <span class="badge ${badgeClass(charger.status)}">${mapStatusLabel(charger.status)}</span>
            </div>
            <div class="charger-meta">
              <span class="badge badge-blue">${escapeHtml(charger.chargerType || "-")}</span>
              <span class="badge badge-gray">${escapeHtml(charger.connectorType || "-")}</span>
              <span class="badge badge-gray">${escapeHtml(charger.powerOutput || "-")}</span>
              <span class="badge badge-gray">₺${charger.pricePerKwh ?? "-"}/kWh</span>
            </div>
            <div class="charger-actions">
              ${(charger.status === "AVAILABLE" || charger.status === "OCCUPIED")
                ? `<a href="${buildReservationUrl(charger.id)}" class="btn btn-primary btn-sm">${escapeHtml(label("reserve", "Reserve"))}</a>`
                : `<span class="text-muted" style="font-size:0.78rem;">${escapeHtml(label("chargerUnavailable", "This charger is not available right now."))}</span>`}
            </div>
          </div>
        `).join("")
      : `<p class="text-muted">${escapeHtml(label("noChargers", "No chargers found for this station."))}</p>`;

    dom.detailPanel.classList.add("visible");
    dom.detailContent.innerHTML = `
      <h2 style="font-size:1rem;padding-right:1.4rem;">${escapeHtml(station.name)}</h2>
      <p class="text-muted" style="margin-top:0.45rem;">${escapeHtml(station.address)}</p>
      <div class="station-meta" style="margin-top:0.8rem;">
        <span class="badge ${badgeClass(station.status)}">${mapStatusLabel(station.status)}</span>
        <span class="badge badge-blue">${escapeHtml(formatLabel("availableCount", "{0}/{1} available", station.availableChargerCount, station.totalChargerCount))}</span>
        ${station.distanceKm != null ? `<span class="badge badge-gray">${escapeHtml(formatLabel("distanceKm", "{0} km", station.distanceKm))}</span>` : ""}
        ${station.minPricePerKwh != null ? `<span class="badge badge-gray">${escapeHtml(formatLabel("priceFrom", "From ₺{0}/kWh", station.minPricePerKwh))}</span>` : ""}
      </div>
      ${state.routeSummary ? `
        <div class="detail-section">
          <h3>${escapeHtml(label("routeSection", "Route"))}</h3>
          <div class="station-meta">
            <span class="badge badge-blue">${escapeHtml(state.routeSummary.distanceText)}</span>
            <span class="badge badge-gray">${escapeHtml(state.routeSummary.durationText)}</span>
          </div>
        </div>
      ` : ""}
      <div class="detail-section">
        <h3>${escapeHtml(label("stationSection", "Station"))}</h3>
        <div class="charger-actions">
          <a href="/ui/stations/${station.id}" class="btn btn-outline btn-sm">${escapeHtml(label("stationDetailPage", "Station Page"))}</a>
          <button type="button" class="btn btn-outline btn-sm" id="route-to-station">${escapeHtml(label("directions", "Directions"))}</button>
          <a href="https://maps.google.com/?q=${station.latitude},${station.longitude}" target="_blank" class="btn btn-outline btn-sm">${escapeHtml(label("openGoogleMaps", "Google Maps"))}</a>
        </div>
      </div>
      <div class="detail-section">
        <h3>${escapeHtml(label("chargerList", "Charger List"))}</h3>
        ${chargerMarkup}
      </div>
    `;

    const routeButton = document.getElementById("route-to-station");
    if (routeButton) {
      routeButton.addEventListener("click", () => drawRoute(station));
    }
  }

  function clearRoute() {
    if (state.directionsRenderer) {
      state.directionsRenderer.setDirections({ routes: [] });
    }
    state.routeSummary = null;
    if (state.selectedStationId != null) {
      const station = state.stations.find((item) => item.id === state.selectedStationId);
      const chargers = state.chargersByStationId.get(state.selectedStationId) || (station ? station.chargers : null);
      if (station && chargers) {
        renderDetailPanel(station, chargers);
      }
    }
  }

  function closeDetailPanel() {
    dom.detailPanel.classList.remove("visible");
    state.selectedStationId = null;
    renderStationList();
  }

  function drawRoute(station, chargers, silent) {
    if (!state.userPosition) {
      if (!silent) {
        alert(label("shareLocationFirst", "Share your location first with Find My Location."));
      }
      return;
    }

    state.directionsService.route({
      origin: state.userPosition,
      destination: { lat: station.latitude, lng: station.longitude },
      travelMode: google.maps.TravelMode.DRIVING
    }, (result, status) => {
      if (status === "OK") {
        state.directionsRenderer.setDirections(result);
        const leg = result.routes && result.routes[0] && result.routes[0].legs
          ? result.routes[0].legs[0]
          : null;
        state.routeSummary = leg ? {
          distanceText: leg.distance ? leg.distance.text : "",
          durationText: leg.duration ? leg.duration.text : "",
          durationMinutes: leg.duration && Number.isFinite(leg.duration.value)
            ? Math.ceil(leg.duration.value / 60)
            : null
        } : null;
        renderDetailPanel(station, chargers || state.chargersByStationId.get(station.id) || station.chargers || []);
      } else {
        if (!silent) {
          alert(label("routeUnavailable", "Could not get directions: ") + status);
        }
      }
    });
  }

  function setUserPosition(lat, lng, accuracyMeters) {
    state.userPosition = { lat, lng };
    state.userAccuracyMeters = accuracyMeters != null ? accuracyMeters : state.userAccuracyMeters;
    renderLocationDebug();

    if (state.googleLoaded && state.map) {
      if (state.userMarker) {
        state.userMarker.setMap(null);
      }

      state.userMarker = new google.maps.Marker({
        position: state.userPosition,
        map: state.map,
        title: label("yourLocation", "Your Location"),
        draggable: state.manualAdjustMode,
        icon: {
          path: google.maps.SymbolPath.CIRCLE,
          scale: 9,
          fillColor: "#2563eb",
          fillOpacity: 1,
          strokeColor: "#ffffff",
          strokeWeight: 3
        }
      });
      state.userMarker.addListener("dragend", async (event) => {
        state.userPosition = {
          lat: event.latLng.lat(),
          lng: event.latLng.lng()
        };
        state.userAccuracyMeters = null;
        renderLocationDebug();
        await loadStations();
        if (state.selectedStationId != null) {
          focusStation(state.selectedStationId);
        }
      });
      renderMarkers();
    }
  }

  function locateUser() {
    if (!navigator.geolocation) {
      alert(label("geolocationUnsupported", "Your browser does not support geolocation."));
      return;
    }

    dom.filterStatus.textContent = label("locating", "Getting your location...");
    navigator.geolocation.getCurrentPosition(async (position) => {
      state.manualAdjustMode = false;
      setUserPosition(position.coords.latitude, position.coords.longitude, position.coords.accuracy);
      await loadStations();
      dom.filterStatus.textContent = label("locationUpdated", "Stations updated for your location.");
    }, () => {
      dom.filterStatus.textContent = "";
      alert(label("locationFailed", "Could not get your location."));
    }, {
      enableHighAccuracy: true,
      timeout: 15000,
      maximumAge: 0
    });
  }

  async function enableManualAdjust() {
    if (!state.userPosition) {
      await new Promise((resolve) => {
        if (!navigator.geolocation) {
          alert(label("geolocationUnsupported", "Your browser does not support geolocation."));
          resolve();
          return;
        }
        navigator.geolocation.getCurrentPosition((position) => {
          state.manualAdjustMode = true;
          setUserPosition(position.coords.latitude, position.coords.longitude, position.coords.accuracy);
          resolve();
        }, () => {
          alert(label("locationFailed", "Could not get your location."));
          resolve();
        }, {
          enableHighAccuracy: true,
          timeout: 15000,
          maximumAge: 0
        });
      });
    }
    state.manualAdjustMode = true;
    if (state.userMarker) {
      state.userMarker.setDraggable(true);
    } else if (state.userPosition) {
      setUserPosition(state.userPosition.lat, state.userPosition.lng, state.userAccuracyMeters);
    }
    dom.filterStatus.textContent = label("adjustingLocation", "Drag the blue marker to correct your location.");
  }

  function loadGoogleMaps(apiKey) {
    if (state.googleLoaded) {
      return;
    }

    window.initStationMap = function initStationMap() {
      state.googleLoaded = true;
      const mapOptions = {
        zoom: 12,
        center: { lat: 38.4237, lng: 27.1428 },
        mapTypeControl: false,
        streetViewControl: false,
        fullscreenControl: false
      };

      if (bootstrap.mapId) {
        mapOptions.mapId = bootstrap.mapId;
      }

      state.map = new google.maps.Map(document.getElementById("map"), mapOptions);
      state.directionsService = new google.maps.DirectionsService();
      state.directionsRenderer = new google.maps.DirectionsRenderer({ suppressMarkers: false });
      state.directionsRenderer.setMap(state.map);
      dom.emptyState.style.display = "none";
      renderMarkers();
    };

    const script = document.createElement("script");
    script.async = true;
    script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&callback=initStationMap`;
    document.head.appendChild(script);
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function buildReservationUrl(chargerId) {
    const params = new URLSearchParams();
    params.set("chargerId", String(chargerId));
    if (state.routeSummary && Number.isFinite(state.routeSummary.durationMinutes)) {
      params.set("travelDurationMinutes", String(state.routeSummary.durationMinutes));
    }
    return `/ui/reservations/create?${params.toString()}`;
  }

  dom.discoveryForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await loadStations();
  });

  dom.resetFilters.addEventListener("click", async () => {
    dom.discoveryForm.reset();
    await loadStations();
  });

  dom.locate.addEventListener("click", locateUser);
  dom.adjustLocation.addEventListener("click", enableManualAdjust);
  dom.clearRoute.addEventListener("click", clearRoute);
  dom.detailClose.addEventListener("click", closeDetailPanel);
  dom.applyApiKey.addEventListener("click", () => {
    const apiKey = dom.apiKeyInput.value.trim();
    if (!apiKey) {
      alert(label("enterApiKey", "Enter an API key."));
      return;
    }
    bootstrap.apiKey = apiKey;
    loadGoogleMaps(apiKey);
  });

  loadStations().catch((error) => {
    dom.filterStatus.textContent = error.message;
  });
  renderLocationDebug();

  if (selectedApiKey()) {
    loadGoogleMaps(selectedApiKey());
  }
})();
