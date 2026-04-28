(function () {
  const bootstrap = window.stationMapBootstrap || {};
  const state = {
    map: null,
    stations: [],
    markers: [],
    markerByStationId: new Map(),
    chargersByStationId: new Map(),
    selectedStationId: null,
    userPosition: null,
    userMarker: null,
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
    clearRoute: document.getElementById("clear-route"),
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
      return "Müsait";
    }
    if (status === "OCCUPIED") {
      return "Dolu";
    }
    return "Offline";
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
    dom.filterStatus.textContent = "İstasyonlar yükleniyor...";
    const response = await fetch(buildDiscoveryUrl());
    if (!response.ok) {
      throw new Error("İstasyonlar yüklenemedi.");
    }

    state.stations = await response.json();
    if (!state.stations.some((station) => station.id === state.selectedStationId)) {
      state.selectedStationId = null;
      dom.detailPanel.classList.remove("visible");
    }
    renderStationList();
    renderMarkers();

    dom.filterStatus.textContent = `${state.stations.length} istasyon bulundu.`;
  }

  function renderStationList() {
    if (!state.stations.length) {
      dom.list.innerHTML = '<div class="text-muted" style="padding:1rem;text-align:center;">Filtreye uyan istasyon bulunamadı.</div>';
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
            <span class="badge badge-blue">${station.availableChargerCount}/${station.totalChargerCount} müsait</span>
            ${station.distanceKm != null ? `<span class="badge badge-gray">${station.distanceKm} km</span>` : ""}
            ${station.minPricePerKwh != null ? `<span class="badge badge-gray">₺${station.minPricePerKwh}/kWh'den</span>` : ""}
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
      throw new Error("Charger detayları alınamadı.");
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
        <h3>Detay</h3>
        <p class="text-muted">Charger detayları yükleniyor...</p>
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
              ${charger.status === "AVAILABLE"
                ? `<a href="/ui/reservations/create?chargerId=${charger.id}" class="btn btn-primary btn-sm">Rezerve Et</a>`
                : `<span class="text-muted" style="font-size:0.78rem;">Bu charger şu an müsait değil.</span>`}
            </div>
          </div>
        `).join("")
      : '<p class="text-muted">Bu istasyon için charger listesi bulunamadı.</p>';

    dom.detailPanel.classList.add("visible");
    dom.detailContent.innerHTML = `
      <h2 style="font-size:1rem;padding-right:1.4rem;">${escapeHtml(station.name)}</h2>
      <p class="text-muted" style="margin-top:0.45rem;">${escapeHtml(station.address)}</p>
      <div class="station-meta" style="margin-top:0.8rem;">
        <span class="badge ${badgeClass(station.status)}">${mapStatusLabel(station.status)}</span>
        <span class="badge badge-blue">${station.availableChargerCount}/${station.totalChargerCount} müsait</span>
        ${station.distanceKm != null ? `<span class="badge badge-gray">${station.distanceKm} km</span>` : ""}
        ${station.minPricePerKwh != null ? `<span class="badge badge-gray">₺${station.minPricePerKwh}/kWh'den</span>` : ""}
      </div>
      ${state.routeSummary ? `
        <div class="detail-section">
          <h3>Rota</h3>
          <div class="station-meta">
            <span class="badge badge-blue">${escapeHtml(state.routeSummary.distanceText)}</span>
            <span class="badge badge-gray">${escapeHtml(state.routeSummary.durationText)}</span>
          </div>
        </div>
      ` : ""}
      <div class="detail-section">
        <h3>İstasyon</h3>
        <div class="charger-actions">
          <a href="/ui/stations/${station.id}" class="btn btn-outline btn-sm">Detay Sayfası</a>
          <button type="button" class="btn btn-outline btn-sm" id="route-to-station">Yol Tarifi</button>
          <a href="https://maps.google.com/?q=${station.latitude},${station.longitude}" target="_blank" class="btn btn-outline btn-sm">Google Maps</a>
        </div>
      </div>
      <div class="detail-section">
        <h3>Charger Listesi</h3>
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

  function drawRoute(station) {
    if (!state.userPosition) {
      alert("Önce 'Konumumu Bul' ile mevcut konumunuzu paylaşın.");
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
          durationText: leg.duration ? leg.duration.text : ""
        } : null;
        renderDetailPanel(station, state.chargersByStationId.get(station.id) || station.chargers || []);
      } else {
        alert("Yol tarifi alınamadı: " + status);
      }
    });
  }

  function setUserPosition(lat, lng) {
    state.userPosition = { lat, lng };

    if (state.googleLoaded && state.map) {
      if (state.userMarker) {
        state.userMarker.setMap(null);
      }

      state.userMarker = new google.maps.Marker({
        position: state.userPosition,
        map: state.map,
        title: "Konumunuz",
        icon: {
          path: google.maps.SymbolPath.CIRCLE,
          scale: 9,
          fillColor: "#2563eb",
          fillOpacity: 1,
          strokeColor: "#ffffff",
          strokeWeight: 3
        }
      });
      renderMarkers();
    }
  }

  function locateUser() {
    if (!navigator.geolocation) {
      alert("Tarayıcınız konum desteği vermiyor.");
      return;
    }

    dom.filterStatus.textContent = "Konum alınıyor...";
    navigator.geolocation.getCurrentPosition(async (position) => {
      setUserPosition(position.coords.latitude, position.coords.longitude);
      await loadStations();
      dom.filterStatus.textContent = "Konuma göre istasyonlar güncellendi.";
    }, () => {
      dom.filterStatus.textContent = "";
      alert("Konum alınamadı.");
    });
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

  dom.discoveryForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await loadStations();
  });

  dom.resetFilters.addEventListener("click", async () => {
    dom.discoveryForm.reset();
    await loadStations();
  });

  dom.locate.addEventListener("click", locateUser);
  dom.clearRoute.addEventListener("click", clearRoute);
  dom.detailClose.addEventListener("click", closeDetailPanel);
  dom.applyApiKey.addEventListener("click", () => {
    const apiKey = dom.apiKeyInput.value.trim();
    if (!apiKey) {
      alert("API key girin.");
      return;
    }
    bootstrap.apiKey = apiKey;
    loadGoogleMaps(apiKey);
  });

  loadStations().catch((error) => {
    dom.filterStatus.textContent = error.message;
  });

  if (selectedApiKey()) {
    loadGoogleMaps(selectedApiKey());
  }
})();
