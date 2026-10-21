// ============================================================================
// JAVASCRIPT: Lógica de Interfaz, Conexión con Backend y Trazado Vial Exacto (Entrega 3)
// Basado en las directrices de integración del Laboratorio 3
// ============================================================================

// Instancia global del mapa de Google Maps
let map = null;
// Arreglo de marcadores activos sobre el mapa
let currentMarkers = [];
// Polilínea principal interior del trazado vial (#4285F4)
let currentPolyline = null;
// Polilínea exterior de borde del trazado vial (#185abc) estilo Google Maps
let currentBorderPolyline = null;
// Renderizador nativo de direcciones de Google Maps (si la API Directions está activa)
let directionsRenderer = null;
// Ventana flotante que muestra el tiempo estimado (min) y distancia (km) sobre la ruta
let routeBadgeInfoWindow = null;
// Bandera que indica si el SDK de Google Maps ya terminó de inicializar
let googleMapsLoaded = false;
// Arreglo en memoria con las paradas de la ruta actualmente seleccionada
let paradasActivas = [];

// Coordenadas iniciales por defecto correspondientes al centro de Ibagué, Tolima
const DEFAULT_CENTER = { lat: 4.43889, lng: -75.23222 };

// Evento disparado automáticamente cuando el documento HTML termina de cargarse
document.addEventListener("DOMContentLoaded", () => {
    // 1. Consulta la clave de API configurada en el backend e inicializa Google Maps
    inicializarEntornoGoogleMaps();

    // 2. Consulta y llena el selector con los códigos de ruta registrados en base de datos
    cargarRutasDisponibles();

    // 3. Registra el evento de cambio en el selector desplegable de rutas
    const routeSelect = document.getElementById("routeSelect");
    // Cuando el usuario selecciona otra ruta, consulta sus paradas y traza el recorrido vial
    routeSelect.addEventListener("change", (e) => {
        // Obtiene el código de ruta elegido
        const codigoRuta = e.target.value;
        // Si el código no está vacío, carga los detalles y dibuja la ruta
        if (codigoRuta) {
            // Invoca la carga de paradas de la ruta seleccionada
            cargarDetallesYRuta(codigoRuta);
        }
    });

    // 4. Registra el evento del botón de actualización manual de datos de ruta
    const btnReload = document.getElementById("btnReload");
    // Al hacer clic en actualizar, vuelve a consultar las paradas y redibuja el recorrido
    btnReload.addEventListener("click", () => {
        // Obtiene el valor actual del selector de rutas
        const codigoRuta = document.getElementById("routeSelect").value;
        // Si hay una ruta seleccionada, recarga sus paradas
        if (codigoRuta) {
            // Actualiza el detalle y trazado de la ruta activa
            cargarDetallesYRuta(codigoRuta);
        } else {
            // Si no había ruta seleccionada, recarga el listado general de rutas
            cargarRutasDisponibles();
        }
    });
});

// Consulta la API pública del backend para obtener la clave de Google Maps e inyectar el SDK
function inicializarEntornoGoogleMaps() {
    // Realiza petición GET al endpoint público de configuración
    fetch("/api/public/config/maps-key")
        // Convierte la respuesta HTTP a objeto JSON
        .then(response => response.json())
        // Procesa el objeto con la propiedad apiKey
        .then(data => {
            // Extrae la clave de Google Maps
            const key = data.apiKey;
            // Obtiene la referencia al banner informativo
            const banner = document.getElementById("keyBanner");

            // Verifica que exista una clave válida configurada en application.properties
            if (key && key.trim() !== "" && key !== "TU_API_KEY_AQUI") {
                // Inyecta dinámicamente el script de Google Maps JS API
                cargarScriptGoogleMaps(key);
            } else {
                // Muestra el banner informativo si no se ha configurado clave
                banner.style.display = "block";
                // Renderiza la vista informativa de respaldo
                renderizarMapaSimulado();
            }
        })
        // Captura errores de red al consultar la configuración
        .catch(err => {
            // Registra el error en la consola del navegador
            console.error("Error al consultar la configuración de Google Maps:", err);
            // Muestra la vista de respaldo
            renderizarMapaSimulado();
        });
}

// Inyecta dinámicamente la etiqueta <script> de Google Maps en el encabezado del documento
function cargarScriptGoogleMaps(apiKey) {
    // Crea el elemento script en el DOM
    const script = document.createElement("script");
    // Configura la URL del SDK de Google Maps con la librería geometry y el callback initGoogleMap
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&callback=initGoogleMap&libraries=geometry`;
    // Habilita carga asíncrona
    script.async = true;
    // Difiere la ejecución hasta que el documento esté listo
    script.defer = true;
    // Expone la función callback en el objeto global window
    window.initGoogleMap = initGoogleMap;
    // Inserta el script en el <head> del documento
    document.head.appendChild(script);
}

// Función callback ejecutada automáticamente por Google Maps cuando su SDK termina de cargar
function initGoogleMap() {
    // Marca la bandera indicando que Google Maps está listo
    googleMapsLoaded = true;
    // Obtiene el contenedor HTML donde se montará el mapa
    const mapContainer = document.getElementById("map");

    // Instancia el objeto google.maps.Map centrado en Ibagué
    map = new google.maps.Map(mapContainer, {
        // Centro geográfico inicial
        center: DEFAULT_CENTER,
        // Nivel de acercamiento inicial
        zoom: 14,
        // Tipo de mapa de carreteras estándar
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        // Habilita controles de zoom
        zoomControl: true,
        // Habilita el control de Street View
        streetViewControl: true
    });

    // Registra en consola la inicialización exitosa
    console.log("Google Maps inicializado exitosamente.");

    // Si las paradas ya habían llegado desde el backend antes de que cargara el mapa, las dibuja de inmediato
    if (paradasActivas && paradasActivas.length > 0) {
        // Traza la ruta siguiendo las calles reales
        dibujarRutaEnMapa(paradasActivas);
    }
}

// Renderiza un mensaje informativo cuando no se ha configurado clave de Google Maps
function renderizarMapaSimulado() {
    // Obtiene el contenedor del mapa
    const mapDiv = document.getElementById("map");
    // Inserta el contenido HTML informativo
    mapDiv.innerHTML = `
        <div style="display:flex; height:100%; flex-direction:column; align-items:center; justify-content:center; background:#eaedf2; color:#555; text-align:center; padding:20px;">
            <div style="font-size: 50px; margin-bottom: 15px;">🗺️</div>
            <h2 style="font-size: 20px; color:#2c3e50; margin-bottom: 10px;">Visualizador de Trayectos Listo</h2>
            <p style="max-width: 500px; font-size: 14px; line-height: 1.5; color:#7f8c8d;">
                Cuando coloques tu clave en <code>application.properties</code> (<code>google.maps.api.key</code>),
                el mapa interactivo de Google Maps se activará de forma automática.
            </p>
        </div>
    `; // Inserta el contenido HTML informativo
} // Fin de renderizarMapaSimulado

// Consulta la lista de códigos de ruta disponibles desde el endpoint público del backend
function cargarRutasDisponibles() {
    // Obtiene la referencia al elemento <select>
    const routeSelect = document.getElementById("routeSelect");
    // Inicializa la opción por defecto
    routeSelect.innerHTML = `<option value="">-- Seleccione una Ruta --</option>`;

    // Realiza la petición GET a /api/public/rutas
    fetch("/api/public/rutas")
        // Parsea la respuesta como JSON
        .then(res => res.json())
        // Procesa el arreglo de códigos de ruta
        .then(rutas => {
            // Verifica que existan rutas registradas
            if (rutas && rutas.length > 0) {
                // Crea una opción <option> por cada código de ruta
                rutas.forEach(codigo => {
                    // Crea el elemento option
                    const opt = document.createElement("option");
                    // Asigna el código de ruta como valor
                    opt.value = codigo;
                    // Asigna el texto visible de la opción
                    opt.textContent = `Ruta: ${codigo}`;
                    // Agrega la opción al selector
                    routeSelect.appendChild(opt);
                });

                // Selecciona automáticamente la primera ruta de la lista
                routeSelect.value = rutas[0];
                // Carga las paradas y dibuja el recorrido de la primera ruta
                cargarDetallesYRuta(rutas[0]);
            } else {
                // Muestra mensaje cuando no existen rutas en la base de datos
                routeSelect.innerHTML = `<option value="">No hay rutas registradas</option>`;
            }
        })
        // Captura errores al consultar las rutas
        .catch(err => {
            // Registra el error en consola
            console.error("Error cargando rutas:", err);
            // Informa el error en el selector
            routeSelect.innerHTML = `<option value="">Error al cargar rutas</option>`;
        });
}

// Consulta las paradas ordenadas de una ruta específica y actualiza tanto la barra lateral como el mapa
function cargarDetallesYRuta(codigoRuta) {
    // Realiza petición GET al endpoint público de paradas de la ruta
    fetch(`/api/public/rutas/${encodeURIComponent(codigoRuta)}/paradas`)
        // Convierte la respuesta a JSON
        .then(res => res.json())
        // Procesa el listado ordenado de paradas
        .then(paradas => {
            // Guarda las paradas activas en memoria
            paradasActivas = paradas;

            // Si no hay paradas, termina la ejecución
            if (!paradas || paradas.length === 0) return;

            // 1. Muestra la tarjeta de resumen del vehículo y conductor en la barra lateral
            const infoCard = document.getElementById("routeInfoCard");
            // Hace visible la tarjeta de información
            infoCard.style.display = "block";

            // Toma el primer registro para extraer los datos del vehículo y del conductor asignado
            const primerPunto = paradas[0];
            // Actualiza el código de la ruta en el encabezado de la tarjeta
            document.getElementById("lblRouteCode").textContent = `Ruta: ${codigoRuta}`;
            // Actualiza marca y línea del vehículo
            document.getElementById("lblVehicleName").textContent = `${primerPunto.vehiculo.marca} ${primerPunto.vehiculo.linea}`;
            // Actualiza el número de placa del vehículo
            document.getElementById("lblVehiclePlate").textContent = primerPunto.vehiculo.placa;
            // Actualiza el tipo de vehículo y tipo de servicio
            document.getElementById("lblVehicleType").textContent = `${primerPunto.vehiculo.tipoVehiculo} (${primerPunto.vehiculo.tipoServicio})`;
            // Actualiza el nombre completo del conductor
            document.getElementById("lblDriverName").textContent = `${primerPunto.persona.nombres} ${primerPunto.persona.apellidos}`;

            // 2. Renderiza la secuencia cronológica de paradas en la lista lateral
            renderizarListaParadas(paradas);

            // 3. Si el mapa de Google Maps ya está activo, traza el recorrido real por las calles
            if (googleMapsLoaded && map) {
                // Dibuja los marcadores y el trazado vial exacto
                dibujarRutaEnMapa(paradas);
            }
        })
        // Captura cualquier error al obtener el detalle de las paradas
        .catch(err => console.error("Error al obtener detalle de paradas:", err));
}

// Construye la línea de tiempo interactiva de paradas en el panel izquierdo
function renderizarListaParadas(paradas) {
    // Obtiene el contenedor de la lista de paradas
    const listContainer = document.getElementById("stopsList");
    // Limpia el contenido previo
    listContainer.innerHTML = "";

    // Cantidad total de paradas de la ruta
    const total = paradas.length;

    // Recorre cada parada para crear su tarjeta en la línea de tiempo
    paradas.forEach((parada, index) => {
        // Crea el contenedor div de la parada
        const item = document.createElement("div");
        // Asigna la clase base de estilo
        item.className = "stop-item";

        // Define la etiqueta según si es parada inicial, intermedia o final
        let etiquetaParada = `Parada ${parada.ordenParada}`;
        // Si es la primera parada (índice 0), aplica estilo de inicio verde
        if (index === 0) {
            // Clase CSS para parada inicial
            item.classList.add("stop-start");
            // Etiqueta de parada inicial
            etiquetaParada = "Parada Inicial (0)";
        } else if (index === total - 1) {
            // Si es la última parada, aplica estilo de destino final rojo
            item.classList.add("stop-end");
            // Etiqueta de parada final
            etiquetaParada = "Parada Final";
        }

        // Formatea la latitud a 5 decimales o muestra 'Pendiente'
        const latTxt = parada.latitud ? parada.latitud.toFixed(5) : "Pendiente";
        // Formatea la longitud a 5 decimales o muestra 'Pendiente'
        const lngTxt = parada.longitud ? parada.longitud.toFixed(5) : "Pendiente";

        // Define la estructura HTML interna de la parada
        item.innerHTML = `
            <div class="stop-dot"></div>
            <div class="stop-order">${etiquetaParada}</div>
            <div class="stop-name">${parada.ubicacion}</div>
            <div class="stop-coords">Lat: ${latTxt} | Lng: ${lngTxt}</div>
        `; // Define la estructura interna de la tarjeta de parada

        // Permite centrar el mapa en la parada al hacer clic sobre ella en la barra lateral
        item.addEventListener("click", () => {
            // Verifica que el mapa esté cargado y la parada tenga coordenadas
            if (googleMapsLoaded && map && parada.latitud && parada.longitud) {
                // Desplaza suavemente la cámara hacia la parada
                map.panTo({ lat: parada.latitud, lng: parada.longitud });
                // Acerca el zoom para visualizar el punto con detalle
                map.setZoom(16);
            }
        });

        // Añade el elemento al contenedor de la línea de tiempo
        listContainer.appendChild(item);
    });
}

// Limpia todos los marcadores, polilíneas y etiquetas flotantes previas del mapa
function limpiarElementosMapa() {
    // Elimina cada marcador previo del mapa
    currentMarkers.forEach(m => m.setMap(null));
    // Vacía el arreglo de marcadores
    currentMarkers = [];

    // Si existe una polilínea principal previa, la remueve del mapa
    if (currentPolyline) {
        // Quita la polilínea interior
        currentPolyline.setMap(null);
        // Limpia la referencia
        currentPolyline = null;
    }

    // Si existe una polilínea de borde previa, la remueve del mapa
    if (currentBorderPolyline) {
        // Quita la polilínea exterior de borde
        currentBorderPolyline.setMap(null);
        // Limpia la referencia
        currentBorderPolyline = null;
    }

    // Si existe un DirectionsRenderer previo, lo desvincula del mapa
    if (directionsRenderer) {
        // Quita el renderizador de direcciones
        directionsRenderer.setMap(null);
        // Limpia la referencia
        directionsRenderer = null;
    }

    // Si existe la etiqueta flotante de tiempo/distancia, la cierra
    if (routeBadgeInfoWindow) {
        // Cierra el InfoWindow flotante
        routeBadgeInfoWindow.close();
        // Limpia la referencia
        routeBadgeInfoWindow = null;
    }
}

// Dibuja los marcadores de cada parada y traza la ruta exacta siguiendo la malla vial (calles y carreras)
async function dibujarRutaEnMapa(paradas) {
    // 1. Limpia cualquier trazado o marcador de una ruta anterior
    limpiarElementosMapa();

    // Crea el objeto de límites geográficos para ajustar el encuadre de la cámara
    const bounds = new google.maps.LatLngBounds();
    // Arreglo con las paradas que cuentan con coordenadas válidas
    const paradasValidas = [];

    // Filtra únicamente las paradas que poseen latitud y longitud numéricas válidas
    paradas.forEach((p) => {
        // Verifica que latitud y longitud existan y sean distintas de 0
        if (p.latitud && p.longitud && p.latitud !== 0 && p.longitud !== 0) {
            // Agrega la parada a la lista de paradas válidas
            paradasValidas.push(p);
        }
    });

    // Cantidad total de paradas con coordenadas válidas
    const total = paradasValidas.length;
    // Si no hay paradas con coordenadas, finaliza la función
    if (total === 0) return;

    // 2. Crea los marcadores personalizados (A, paradas intermedias 1..N, y B)
    paradasValidas.forEach((p, idx) => {
        // Construye el objeto de posición LatLng
        const pos = { lat: p.latitud, lng: p.longitud };
        // Extiende los límites de cámara para incluir esta parada
        bounds.extend(pos);

        // Determina la etiqueta del marcador: 'A' para inicio, 'B' para destino final, o número de parada intermedia
        let labelChar = p.ordenParada ? p.ordenParada.toString() : idx.toString();
        // Color de fondo del marcador (azul para paradas intermedias)
        let pinColor = "#1a73e8";
        // Si es la primera parada (origen), usa etiqueta 'A' y color verde
        if (idx === 0) {
            // Etiqueta de origen
            labelChar = "A";
            // Color verde estilo Google Maps origen
            pinColor = "#1e8e3e";
        } else if (idx === total - 1) {
            // Si es la última parada (destino), usa etiqueta 'B' y color rojo
            labelChar = "B";
            // Color rojo estilo Google Maps destino
            pinColor = "#d93025";
        }

        // Crea el marcador sobre el mapa de Google Maps
        const marker = new google.maps.Marker({
            // Posición geográfica de la parada
            position: pos,
            // Instancia del mapa donde se dibuja
            map: map,
            // Texto emergente al pasar el cursor
            title: `Parada ${p.ordenParada}: ${p.ubicacion}`,
            // Etiqueta interna del marcador en blanco y negrita
            label: {
                // Texto de la etiqueta (A, 1, 2..., B)
                text: labelChar,
                // Color de texto blanco
                color: "#ffffff",
                // Grosor de fuente en negrita
                fontWeight: "bold",
                // Tamaño de fuente
                fontSize: "13px"
            },
            // Icono vectorial SVG tipo pin con borde blanco para máxima legibilidad sobre la ruta
            icon: {
                // Trazado SVG de pin clásico de mapa
                path: "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z",
                // Color de relleno según el tipo de parada
                fillColor: pinColor,
                // Opacidad total del relleno
                fillOpacity: 1,
                // Color del borde blanco
                strokeColor: "#ffffff",
                // Grosor del borde blanco
                strokeWeight: 2,
                // Escala del icono
                scale: 1.7,
                // Punto de anclaje en la punta inferior del pin
                anchor: new google.maps.Point(12, 22),
                // Posición centrada para la letra/número dentro del pin
                labelOrigin: new google.maps.Point(12, 9)
            },
            // Prioridad de capa alta para que los marcadores queden por encima de la polilínea vial
            zIndex: 100 + idx
        });

        // Crea la ventana de información detallada al hacer clic sobre el marcador
        const infoWindow = new google.maps.InfoWindow({
            // Contenido HTML con los datos de la parada y el vehículo
            content: `
                <div style="font-size:13px; padding:6px; line-height:1.4; color:#202124;">
                    <strong style="color:#1a73e8;">Parada ${p.ordenParada}: ${p.ubicacion}</strong><br>
                    <span><strong>Vehículo:</strong> ${p.vehiculo.placa} (${p.vehiculo.marca} ${p.vehiculo.linea})</span><br>
                    <span><strong>Conductor:</strong> ${p.persona.nombres} ${p.persona.apellidos}</span><br>
                    <span style="font-size:11px; color:#5f6368;">Lat: ${p.latitud.toFixed(5)}, Lng: ${p.longitud.toFixed(5)}</span>
                </div>
            ` // Contenido HTML del globo
        }); // Fin de creación de InfoWindow

        // Registra el evento click en el marcador para abrir su ventana de información
        marker.addListener("click", () => {
            // Muestra la ventana emergente sobre el marcador
            infoWindow.open(map, marker);
        });

        // Guarda el marcador en el arreglo global para poder limpiarlo después
        currentMarkers.push(marker);
    });

    // Ajusta inicialmente la vista del mapa para encuadrar todas las paradas
    map.fitBounds(bounds);

    // 3. Si hay al menos 2 paradas, obtiene la geometría vial exacta calle por calle
    if (total > 1) {
        // Intenta trazar la ruta vial exacta por las calles de la ciudad
        await trazarGeometriaVialExacta(paradasValidas, bounds);
    }
}

// Consulta el motor de enrutamiento vial (OSRM / Google Directions) y dibuja la doble polilínea estilo Google Maps
async function trazarGeometriaVialExacta(paradasValidas, bounds) {
    try {
        // Construye la cadena de coordenadas en formato "longitud,latitud;longitud,latitud" requerido por OSRM
        const coordsQuery = paradasValidas
            // Mapea cada parada al formato lng,lat
            .map(p => `${p.longitud},${p.latitud}`)
            // Une todas las paradas con punto y coma
            .join(";");

        // URL del servicio público de enrutamiento vehicular OSRM solicitando geometría completa GeoJSON calle por calle
        const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${coordsQuery}?overview=full&geometries=geojson&steps=true`;

        // Ejecuta la petición HTTP para obtener los vértices exactos de las calles y carreras
        const response = await fetch(osrmUrl);
        // Convierte la respuesta a JSON
        const data = await response.json();

        // Verifica que OSRM haya devuelto al menos una ruta vehicular válida con su geometría
        if (data && data.code === "Ok" && data.routes && data.routes.length > 0) {
            // Toma la ruta óptima principal
            const route = data.routes[0];
            // Extrae el arreglo de coordenadas [lng, lat] que siguen exactamente cada curva y esquina de las calles
            const geojsonCoords = route.geometry.coordinates;

            // Convierte cada par [lng, lat] al formato { lat, lng } de Google Maps
            const streetPath = geojsonCoords.map(coord => {
                // Crea el punto con latitud (índice 1) y longitud (índice 0)
                const pt = { lat: coord[1], lng: coord[0] };
                // Amplía los límites del mapa para asegurar que ninguna curva quede fuera de pantalla
                bounds.extend(pt);
                // Retorna el punto formateado
                return pt;
            });

            // 1. Dibuja la polilínea exterior de borde oscuro (#185abc) para dar el efecto auténtico de Google Maps
            currentBorderPolyline = new google.maps.Polyline({
                // Asigna los cientos de vértices que siguen las calles reales
                path: streetPath,
                // Desactiva geodesic porque la geometría ya sigue la curvatura exacta de las calles
                geodesic: false,
                // Color azul oscuro para el borde exterior
                strokeColor: "#185abc",
                // Opacidad del borde
                strokeOpacity: 0.95,
                // Grosor exterior mayor (9px)
                strokeWeight: 9,
                // Nivel de capa inferior al relleno
                zIndex: 10
            });
            // Renderiza el borde sobre el mapa
            currentBorderPolyline.setMap(map);

            // 2. Dibuja la polilínea interior principal en azul vibrante (#4285F4) estilo Google Maps Navigation
            currentPolyline = new google.maps.Polyline({
                // Asigna los mismos vértices viales exactos
                path: streetPath,
                // Geometría plana sobre calles
                geodesic: false,
                // Azul oficial de rutas activas de Google Maps
                strokeColor: "#4285F4",
                // Opacidad total
                strokeOpacity: 1.0,
                // Grosor interior (5.5px) que deja visible el borde de 9px
                strokeWeight: 6,
                // Nivel de capa superior al borde pero inferior a los marcadores
                zIndex: 11
            });
            // Renderiza la línea principal sobre el mapa
            currentPolyline.setMap(map);

            // 3. Calcula el tiempo estimado en minutos y la distancia total en kilómetros
            const duracionMin = Math.max(1, Math.round(route.duration / 60));
            // Convierte la distancia de metros a kilómetros con 1 decimal
            const distanciaKm = (route.distance / 1000).toFixed(1);

            // Obtiene el punto medio de la ruta vial para colocar la tarjeta flotante de tiempo y distancia
            const midIndex = Math.floor(streetPath.length / 2);
            // Coordenada central del recorrido
            const midPoint = streetPath[midIndex];

            // Crea la tarjeta flotante estilo Google Maps ("🚗 X min / X.X km")
            routeBadgeInfoWindow = new google.maps.InfoWindow({
                // Posición en la mitad del recorrido vial
                position: midPoint,
                // Diseño limpio con icono de vehículo, minutos en negrita y distancia en kilómetros
                content: `
                    <div style="display:flex; align-items:center; gap:8px; padding:2px 6px; font-family:'Segoe UI',Roboto,sans-serif;">
                        <span style="font-size:18px;">🚗</span>
                        <div style="line-height:1.25;">
                            <div style="font-weight:700; font-size:14px; color:#185abc;">${duracionMin} min</div>
                            <div style="font-size:12px; color:#5f6368; font-weight:600;">${distanciaKm} km</div>
                        </div>
                    </div>
                `,
                // Desplazamiento vertical ligero para no tapar la línea azul
                pixelOffset: new google.maps.Size(0, -8)
            });

            // Abre automáticamente la tarjeta de tiempo y distancia sobre el mapa
            routeBadgeInfoWindow.open(map);

            // Reajusta suavemente los límites del mapa para mostrar toda la ruta vial
            map.fitBounds(bounds);
            // Termina exitosamente el trazado vial
            return;
        }
    } catch (err) {
        // Si ocurre un problema de red con OSRM, registra advertencia e intenta con DirectionsService de Google
        console.warn("Intentando respaldo con Google DirectionsService:", err);
    }

    // 4. Respaldo secundario: Intenta con Google DirectionsService nativo
    const directionsService = new google.maps.DirectionsService();
    // Construye los waypoints intermedios (todas las paradas entre la primera y la última)
    const waypoints = paradasValidas.slice(1, paradasValidas.length - 1).map(p => ({
        // Ubicación de la parada intermedia
        location: { lat: p.latitud, lng: p.longitud },
        // Indica que es una parada obligatoria del itinerario
        stopover: true
    }));

    // Solicita la ruta vehicular a Google DirectionsService
    directionsService.route(
        {
            // Primera parada como origen
            origin: { lat: paradasValidas[0].latitud, lng: paradasValidas[0].longitud },
            // Última parada como destino
            destination: { lat: paradasValidas[paradasValidas.length - 1].latitud, lng: paradasValidas[paradasValidas.length - 1].longitud },
            // Paradas intermedias
            waypoints: waypoints,
            // Mantiene el orden exacto de las paradas del itinerario
            optimizeWaypoints: false,
            // Modo de conducción vehicular
            travelMode: google.maps.TravelMode.DRIVING
        },
        (result, status) => {
            // Si Google DirectionsService responde OK, dibuja la polilínea con los pasos detallados
            if (status === "OK" && result) {
                // Extrae todos los puntos detallados del overview_path
                const detailedPath = result.routes[0].overview_path;
                // Dibuja el borde exterior
                currentBorderPolyline = new google.maps.Polyline({
                    // Ruta detallada
                    path: detailedPath,
                    // Color de borde
                    strokeColor: "#185abc",
                    // Opacidad
                    strokeOpacity: 0.95,
                    // Grosor exterior
                    strokeWeight: 9,
                    // Capa
                    zIndex: 10
                });
                // Asigna al mapa
                currentBorderPolyline.setMap(map);

                // Dibuja la línea interior
                currentPolyline = new google.maps.Polyline({
                    // Ruta detallada
                    path: detailedPath,
                    // Color principal
                    strokeColor: "#4285F4",
                    // Opacidad
                    strokeOpacity: 1.0,
                    // Grosor interior
                    strokeWeight: 6,
                    // Capa
                    zIndex: 11
                });
                // Asigna al mapa
                currentPolyline.setMap(map);
            } else {
                // Último respaldo en caso de estar sin conexión a internet: une los puntos directamente
                const fallbackCoords = paradasValidas.map(p => ({ lat: p.latitud, lng: p.longitud }));
                // Crea polilínea simple de respaldo
                currentPolyline = new google.maps.Polyline({
                    // Coordenadas de paradas
                    path: fallbackCoords,
                    // Geodésica
                    geodesic: true,
                    // Color azul
                    strokeColor: "#4285F4",
                    // Opacidad
                    strokeOpacity: 0.9,
                    // Grosor
                    strokeWeight: 5
                });
                // Asigna al mapa
                currentPolyline.setMap(map);
            }
        }
    );
}
