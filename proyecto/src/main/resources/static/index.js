/**
 * ============================================================================
 * JAVASCRIPT: Lógica de Interfaz y Conexión con Backend y Google Maps (Entrega 3)
 * Basado en las directrices de integración del Laboratorio 3
 * ============================================================================
 */

// Variables globales para el manejo del mapa, marcadores y trazado de rutas
let map = null;
let currentMarkers = [];
let currentPolyline = null;
let googleMapsLoaded = false;

// Coordenadas iniciales por defecto (Ibagué, Colombia)
const DEFAULT_CENTER = { lat: 4.43889, lng: -75.23222 };

// Evento disparado una vez se completa la carga del DOM
document.addEventListener("DOMContentLoaded", () => {
    // 1. Obtener la clave de API configurada en el backend y cargar Google Maps dinámicamente
    inicializarEntornoGoogleMaps();

    // 2. Cargar las rutas disponibles desde el backend
    cargarRutasDisponibles();

    // 3. Asociar evento al selector de rutas
    const routeSelect = document.getElementById("routeSelect");
    routeSelect.addEventListener("change", (e) => {
        const codigoRuta = e.target.value;
        if (codigoRuta) {
            cargarDetallesYRuta(codigoRuta);
        }
    });

    // 4. Botón de recarga manual
    const btnReload = document.getElementById("btnReload");
    btnReload.addEventListener("click", () => {
        const codigoRuta = document.getElementById("routeSelect").value;
        if (codigoRuta) {
            cargarDetallesYRuta(codigoRuta);
        } else {
            cargarRutasDisponibles();
        }
    });
});

/**
 * Consulta la API pública del backend para averiguar la clave de Google Maps
 * e inyecta dinámicamente el script de Google Maps API
 */
function inicializarEntornoGoogleMaps() {
    fetch("/api/public/config/maps-key")
        .then(response => response.json())
        .then(data => {
            const key = data.apiKey;
            const banner = document.getElementById("keyBanner");

            if (key && key.trim() !== "" && key !== "TU_API_KEY_AQUI") {
                cargarScriptGoogleMaps(key);
            } else {
                // Mostrar banner de configuración si aún no se coloca la clave
                banner.style.display = "block";
                renderizarMapaSimulado();
            }
        })
        .catch(err => {
            console.error("Error al consultar la configuración de Google Maps:", err);
            renderizarMapaSimulado();
        });
}

/**
 * Inyecta dinámicamente la etiqueta <script> de Google Maps
 */
function cargarScriptGoogleMaps(apiKey) {
    const script = document.createElement("script");
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&callback=initGoogleMap&libraries=geometry`;
    script.async = true;
    script.defer = true;
    window.initGoogleMap = initGoogleMap;
    document.head.appendChild(script);
}

// Variable para almacenar en memoria las paradas activas
let paradasActivas = [];

/**
 * Función callback ejecutada por Google Maps cuando su SDK termina de cargar
 */
function initGoogleMap() {
    googleMapsLoaded = true;
    const mapContainer = document.getElementById("map");

    map = new google.maps.Map(mapContainer, {
        center: DEFAULT_CENTER,
        zoom: 14,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        zoomControl: true,
        streetViewControl: true
    });

    console.log("Google Maps inicializado exitosamente.");

    // Si ya se habían cargado paradas antes de que el SDK de Maps estuviera listo, dibujarlas de inmediato
    if (paradasActivas && paradasActivas.length > 0) {
        dibujarRutaEnMapa(paradasActivas);
    }
}

/**
 * Renderiza una vista alternativa o mensaje si aún no hay clave configurada
 */
function renderizarMapaSimulado() {
    const mapDiv = document.getElementById("map");
    mapDiv.innerHTML = `
        <div style="display:flex; height:100%; flex-direction:column; align-items:center; justify-content:center; background:#eaedf2; color:#555; text-align:center; padding:20px;">
            <div style="font-size: 50px; margin-bottom: 15px;">🗺️</div>
            <h2 style="font-size: 20px; color:#2c3e50; margin-bottom: 10px;">Visualizador de Trayectos Listo</h2>
            <p style="max-width: 500px; font-size: 14px; line-height: 1.5; color:#7f8c8d;">
                Cuando coloques tu clave en <code>application.properties</code> (<code>google.maps.api.key</code>),
                el mapa satelital e interactivo de Google Maps se activará de forma automática.
            </p>
        </div>
    `;
}

/**
 * Consulta la lista de rutas disponibles desde el backend
 */
function cargarRutasDisponibles() {
    const routeSelect = document.getElementById("routeSelect");
    routeSelect.innerHTML = `<option value="">-- Seleccione una Ruta --</option>`;

    fetch("/api/public/rutas")
        .then(res => res.json())
        .then(rutas => {
            if (rutas && rutas.length > 0) {
                rutas.forEach(codigo => {
                    const opt = document.createElement("option");
                    opt.value = codigo;
                    opt.textContent = `Ruta: ${codigo}`;
                    routeSelect.appendChild(opt);
                });

                // Seleccionar automáticamente la primera ruta si existe
                routeSelect.value = rutas[0];
                cargarDetallesYRuta(rutas[0]);
            } else {
                routeSelect.innerHTML = `<option value="">No hay rutas registradas</option>`;
            }
        })
        .catch(err => {
            console.error("Error cargando rutas:", err);
            routeSelect.innerHTML = `<option value="">Error al cargar rutas</option>`;
        });
}

/**
 * Carga las paradas de la ruta seleccionada y dibuja los puntos en el mapa
 */
function cargarDetallesYRuta(codigoRuta) {
    fetch(`/api/public/rutas/${encodeURIComponent(codigoRuta)}/paradas`)
        .then(res => res.json())
        .then(paradas => {
            // Guardar paradas en memoria para sincronización con el mapa
            paradasActivas = paradas;

            // 1. Mostrar tarjeta de información con los datos del primer punto
            const infoCard = document.getElementById("routeInfoCard");
            infoCard.style.display = "block";

            const primerPunto = paradas[0];
            document.getElementById("lblRouteCode").textContent = `Ruta: ${codigoRuta}`;
            document.getElementById("lblVehicleName").textContent = `${primerPunto.vehiculo.marca} ${primerPunto.vehiculo.linea}`;
            document.getElementById("lblVehiclePlate").textContent = primerPunto.vehiculo.placa;
            document.getElementById("lblVehicleType").textContent = `${primerPunto.vehiculo.tipoVehiculo} (${primerPunto.vehiculo.tipoServicio})`;
            document.getElementById("lblDriverName").textContent = `${primerPunto.persona.nombres} ${primerPunto.persona.apellidos}`;

            // 2. Renderizar itinerario en la lista lateral
            renderizarListaParadas(paradas);

            // 3. Si Google Maps está cargado, dibujar marcadores y polilínea
            if (googleMapsLoaded && map) {
                dibujarRutaEnMapa(paradas);
            }
        })
        .catch(err => console.error("Error al obtener detalle de paradas:", err));
}

/**
 * Dibuja la línea de tiempo de paradas en el panel lateral
 */
function renderizarListaParadas(paradas) {
    const listContainer = document.getElementById("stopsList");
    listContainer.innerHTML = "";

    const total = paradas.length;

    paradas.forEach((parada, index) => {
        const item = document.createElement("div");
        item.className = "stop-item";

        // Distinción visual para parada inicial, intermedia o final
        let etiquetaParada = `Parada ${parada.ordenParada}`;
        if (index === 0) {
            item.classList.add("stop-start");
            etiquetaParada = "Parada Inicial (0)";
        } else if (index === total - 1) {
            item.classList.add("stop-end");
            etiquetaParada = "Parada Final";
        }

        const latTxt = parada.latitud ? parada.latitud.toFixed(5) : "Pendiente";
        const lngTxt = parada.longitud ? parada.longitud.toFixed(5) : "Pendiente";

        item.innerHTML = `
            <div class="stop-dot"></div>
            <div class="stop-order">${etiquetaParada}</div>
            <div class="stop-name">${parada.ubicacion}</div>
            <div class="stop-coords">Lat: ${latTxt} | Lng: ${lngTxt}</div>
        `;

        // Evento al hacer click sobre una parada para centrar en el mapa
        item.addEventListener("click", () => {
            if (googleMapsLoaded && map && parada.latitud && parada.longitud) {
                map.panTo({ lat: parada.latitud, lng: parada.longitud });
                map.setZoom(16);
            }
        });

        listContainer.appendChild(item);
    });
}

/**
 * Dibuja los marcadores de cada parada y la línea de unión en Google Maps
 */
function dibujarRutaEnMapa(paradas) {
    // 1. Limpiar marcadores y polilíneas anteriores
    currentMarkers.forEach(m => m.setMap(null));
    currentMarkers = [];

    if (currentPolyline) {
        currentPolyline.setMap(null);
    }

    const bounds = new google.maps.LatLngBounds();
    const pathCoordinates = [];

    const total = paradas.length;

    paradas.forEach((p, idx) => {
        if (!p.latitud || !p.longitud) return;

        const pos = { lat: p.latitud, lng: p.longitud };
        pathCoordinates.push(pos);
        bounds.extend(pos);

        // Definir color del marcador según posición
        let labelChar = (idx + 1).toString();
        if (idx === 0) labelChar = "A";
        else if (idx === total - 1) labelChar = "B";

        const marker = new google.maps.Marker({
            position: pos,
            map: map,
            title: `${p.ordenParada}: ${p.ubicacion}`,
            label: {
                text: labelChar,
                color: "#ffffff",
                fontWeight: "bold"
            }
        });

        // Ventana de información al hacer click sobre el marcador
        const infoWindow = new google.maps.InfoWindow({
            content: `
                <div style="font-size:13px; padding:4px;">
                    <strong>${p.ubicacion}</strong><br>
                    <span>Orden: ${p.ordenParada}</span><br>
                    <span>Vehículo: ${p.vehiculo.placa}</span>
                </div>
            `
        });

        marker.addListener("click", () => {
            infoWindow.open(map, marker);
        });

        currentMarkers.push(marker);
    });

    // Dibujar la polilínea que une todos los puntos de la ruta
    if (pathCoordinates.length > 1) {
        currentPolyline = new google.maps.Polyline({
            path: pathCoordinates,
            geodesic: true,
            strokeColor: "#2a5298",
            strokeOpacity: 0.9,
            strokeWeight: 5
        });

        currentPolyline.setMap(map);
    }

    // Ajustar el zoom del mapa para encuadrar todos los puntos
    if (pathCoordinates.length > 0) {
        map.fitBounds(bounds);
    }
}

