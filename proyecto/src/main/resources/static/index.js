/**
 * ============================================================================
 * JAVASCRIPT: Lógica de Interfaz y Conexión con Backend y Google Maps (Entrega 3)
 * Basado en las directrices de integración del Laboratorio 3
 * ============================================================================
 */

// Variables globales para el manejo del mapa, marcadores, servicios de direcciones y polilíneas
let map = null; // Instancia principal del mapa de Google Maps
let currentMarkers = []; // Arreglo para almacenar los marcadores activos en el mapa
let currentPolyline = null; // Polilínea principal de la ruta sobre las calles
let currentBorderPolyline = null; // Polilínea de borde oscuro estilo Google Maps
let directionsService = null; // Servicio nativo de Google Maps para calcular rutas por calles
let directionsRenderer = null; // Renderizador nativo de Google Maps para dibujar el camino vial
let routeCalloutWindow = null; // Etiqueta flotante en el mapa con el tiempo estimado y kilómetros (ej. 27 min / 7.1 km)
let googleMapsLoaded = false; // Bandera que indica si el SDK de Google Maps ya cargó

// Coordenadas iniciales por defecto (Ibagué, Colombia)
const DEFAULT_CENTER = { lat: 4.43889, lng: -75.23222 }; // Centro geográfico de Ibagué

// Evento disparado una vez se completa la carga del DOM
document.addEventListener("DOMContentLoaded", () => { // Escucha cuando el documento HTML está completamente cargado
    // 1. Obtener la clave de API configurada en el backend y cargar Google Maps dinámicamente
    inicializarEntornoGoogleMaps(); // Llama a la función que obtiene el API Key de Google Maps

    // 2. Cargar las rutas disponibles desde el backend
    cargarRutasDisponibles(); // Consulta las rutas registradas en la base de datos

    // 3. Asociar evento al selector de rutas
    const routeSelect = document.getElementById("routeSelect"); // Obtiene la referencia al elemento <select> de rutas
    routeSelect.addEventListener("change", (e) => { // Escucha cuando el usuario cambia la ruta seleccionada
        const codigoRuta = e.target.value; // Extrae el código de ruta elegido
        if (codigoRuta) { // Verifica que el código no esté vacío
            cargarDetallesYRuta(codigoRuta); // Carga las paradas y dibuja el recorrido vial en el mapa
        } // Fin del condicional de código de ruta
    }); // Fin del evento change

    // 4. Botón de recarga manual
    const btnReload = document.getElementById("btnReload"); // Obtiene la referencia al botón de actualizar
    btnReload.addEventListener("click", () => { // Escucha el clic en el botón de recarga
        const codigoRuta = document.getElementById("routeSelect").value; // Lee la ruta seleccionada actualmente
        if (codigoRuta) { // Si hay una ruta seleccionada
            cargarDetallesYRuta(codigoRuta); // Recarga las paradas y vuelve a trazar la ruta por las calles
        } else { // Si no hay ruta seleccionada
            cargarRutasDisponibles(); // Recarga el listado general de rutas
        } // Fin del condicional
    }); // Fin del evento click
}); // Fin de DOMContentLoaded

/**
 * Consulta la API pública del backend para averiguar la clave de Google Maps
 * e inyecta dinámicamente el script de Google Maps API
 */
function inicializarEntornoGoogleMaps() { // Función para inicializar Google Maps con la llave del servidor
    fetch("/api/public/config/maps-key") // Realiza petición GET al endpoint público de configuración
        .then(response => response.json()) // Convierte la respuesta a JSON
        .then(data => { // Procesa el objeto recibido
            const key = data.apiKey; // Extrae el valor de apiKey
            const banner = document.getElementById("keyBanner"); // Obtiene el banner de aviso de configuración

            if (key && key.trim() !== "" && key !== "TU_API_KEY_AQUI") { // Si existe una clave configurada válida
                cargarScriptGoogleMaps(key); // Inyecta el script oficial de Google Maps
            } else { // Si no se ha configurado la clave
                banner.style.display = "block"; // Muestra el banner informativo
                renderizarMapaSimulado(); // Muestra el contenedor de espera
            } // Fin de validación de clave
        })
        .catch(err => { // Captura errores de red
            console.error("Error al consultar la configuración de Google Maps:", err); // Registra el error en consola
            renderizarMapaSimulado(); // Muestra vista alternativa
        }); // Fin de la promesa fetch
} // Fin de inicializarEntornoGoogleMaps

/**
 * Inyecta dinámicamente la etiqueta <script> de Google Maps
 */
function cargarScriptGoogleMaps(apiKey) { // Función que agrega el script de Google Maps al <head>
    const script = document.createElement("script"); // Crea un elemento <script>
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&callback=initGoogleMap&libraries=geometry`; // Define la URL con la API Key y librería geometry
    script.async = true; // Carga de manera asíncrona
    script.defer = true; // Difiere la ejecución hasta que el documento esté listo
    window.initGoogleMap = initGoogleMap; // Expone la función callback en el objeto global window
    document.head.appendChild(script); // Inserta el script en el encabezado del documento
} // Fin de cargarScriptGoogleMaps

// Variable para almacenar en memoria las paradas activas
let paradasActivas = []; // Arreglo con las paradas de la ruta actualmente seleccionada

/**
 * Función callback ejecutada por Google Maps cuando su SDK termina de cargar
 */
function initGoogleMap() { // Inicializa el mapa interactivo de Google Maps
    googleMapsLoaded = true; // Marca que el SDK de Google Maps ya está disponible
    const mapContainer = document.getElementById("map"); // Obtiene el contenedor HTML del mapa

    map = new google.maps.Map(mapContainer, { // Crea la instancia del mapa de Google
        center: DEFAULT_CENTER, // Centra el mapa inicialmente en Ibagué
        zoom: 14, // Nivel de zoom urbano ideal para ver calles y carreras
        mapTypeId: google.maps.MapTypeId.ROADMAP, // Tipo de mapa vial estándar de Google Maps
        zoomControl: true, // Habilita los botones de zoom
        streetViewControl: true, // Habilita el control de Street View
        fullscreenControl: true // Habilita el botón de pantalla completa
    }); // Fin de configuración del mapa

    console.log("Google Maps inicializado exitosamente."); // Mensaje de confirmación en consola

    // Si ya se habían cargado paradas antes de que el SDK de Maps estuviera listo, dibujarlas de inmediato
    if (paradasActivas && paradasActivas.length > 0) { // Verifica si ya hay paradas en memoria
        dibujarRutaEnMapa(paradasActivas); // Traza la ruta siguiendo las calles de la ciudad
    } // Fin de verificación de paradas previas
} // Fin de initGoogleMap

/**
 * Renderiza una vista alternativa o mensaje si aún no hay clave configurada
 */
function renderizarMapaSimulado() { // Muestra mensaje informativo cuando no hay API Key
    const mapDiv = document.getElementById("map"); // Obtiene el div del mapa
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

/**
 * Consulta la lista de rutas disponibles desde el backend
 */
function cargarRutasDisponibles() { // Obtiene los códigos de ruta registrados en la tabla trayecto
    const routeSelect = document.getElementById("routeSelect"); // Obtiene el elemento <select>
    routeSelect.innerHTML = `<option value="">-- Seleccione una Ruta --</option>`; // Opción inicial por defecto

    fetch("/api/public/rutas") // Consulta el endpoint público de rutas disponibles
        .then(res => res.json()) // Convierte la respuesta a JSON
        .then(rutas => { // Procesa el arreglo de códigos de ruta
            if (rutas && rutas.length > 0) { // Si existen rutas registradas
                rutas.forEach(codigo => { // Recorre cada código de ruta
                    const opt = document.createElement("option"); // Crea una etiqueta <option>
                    opt.value = codigo; // Asigna el valor del código de ruta
                    opt.textContent = `Ruta: ${codigo}`; // Define el texto visible en el desplegable
                    routeSelect.appendChild(opt); // Agrega la opción al selector
                }); // Fin del forEach

                // Seleccionar automáticamente la primera ruta si existe
                routeSelect.value = rutas[0]; // Selecciona la primera ruta por defecto
                cargarDetallesYRuta(rutas[0]); // Carga los detalles y dibuja el recorrido en el mapa
            } else { // Si no hay rutas en la base de datos
                routeSelect.innerHTML = `<option value="">No hay rutas registradas</option>`; // Muestra aviso sin rutas
            } // Fin del condicional
        })
        .catch(err => { // Captura errores de conexión
            console.error("Error cargando rutas:", err); // Imprime el error en consola
            routeSelect.innerHTML = `<option value="">Error al cargar rutas</option>`; // Muestra error en el selector
        }); // Fin de la promesa fetch
} // Fin de cargarRutasDisponibles

/**
 * Carga las paradas de la ruta seleccionada y dibuja el recorrido vial en el mapa
 */
function cargarDetallesYRuta(codigoRuta) { // Obtiene todas las paradas ordenadas de un código de ruta
    fetch(`/api/public/rutas/${encodeURIComponent(codigoRuta)}/paradas`) // Petición GET al detalle de paradas de la ruta
        .then(res => res.json()) // Convierte la respuesta a JSON
        .then(paradas => { // Procesa la lista de paradas recibida
            // Guardar paradas en memoria para sincronización con el mapa
            paradasActivas = paradas; // Almacena las paradas activas

            // 1. Mostrar tarjeta de información con los datos del primer punto
            const infoCard = document.getElementById("routeInfoCard"); // Obtiene la tarjeta de resumen lateral
            infoCard.style.display = "block"; // Hace visible la tarjeta

            const primerPunto = paradas[0]; // Toma el primer registro para extraer vehículo y conductor
            document.getElementById("lblRouteCode").textContent = `Ruta: ${codigoRuta}`; // Muestra el código de la ruta
            document.getElementById("lblVehicleName").textContent = `${primerPunto.vehiculo.marca} ${primerPunto.vehiculo.linea}`; // Muestra marca y línea
            document.getElementById("lblVehiclePlate").textContent = primerPunto.vehiculo.placa; // Muestra la placa del vehículo
            document.getElementById("lblVehicleType").textContent = `${primerPunto.vehiculo.tipoVehiculo} (${primerPunto.vehiculo.tipoServicio})`; // Muestra tipo y servicio
            document.getElementById("lblDriverName").textContent = `${primerPunto.persona.nombres} ${primerPunto.persona.apellidos}`; // Muestra nombre del conductor

            // 2. Renderizar itinerario en la lista lateral
            renderizarListaParadas(paradas); // Construye la línea de tiempo en la barra izquierda

            // 3. Si Google Maps está cargado, trazar la ruta siguiendo las calles
            if (googleMapsLoaded && map) { // Verifica que el mapa ya esté inicializado
                dibujarRutaEnMapa(paradas); // Invoca el trazado vial por las calles
            } // Fin de verificación de mapa
        })
        .catch(err => console.error("Error al obtener detalle de paradas:", err)); // Registra cualquier error en consola
} // Fin de cargarDetallesYRuta

/**
 * Dibuja la línea de tiempo de paradas en el panel lateral
 */
function renderizarListaParadas(paradas) { // Construye los ítems de paradas en el panel izquierdo
    const listContainer = document.getElementById("stopsList"); // Obtiene el contenedor de la lista de paradas
    listContainer.innerHTML = ""; // Limpia el contenido previo

    const total = paradas.length; // Cantidad total de paradas de la ruta

    paradas.forEach((parada, index) => { // Recorre cada parada en orden
        const item = document.createElement("div"); // Crea el contenedor div para la parada
        item.className = "stop-item"; // Asigna la clase CSS de parada

        // Distinción visual para parada inicial, intermedia o final
        let etiquetaParada = `Parada Intermedia (${parada.ordenParada})`; // Texto por defecto para paradas intermedias
        if (index === 0) { // Si es la primera parada (orden 0)
            item.classList.add("stop-start"); // Aplica estilo de parada inicial
            etiquetaParada = "Parada Inicial (0)"; // Etiqueta de inicio
        } else if (index === total - 1) { // Si es la última parada de la ruta
            item.classList.add("stop-end"); // Aplica estilo de parada final
            etiquetaParada = `Parada Final (${parada.ordenParada})`; // Etiqueta de destino final
        } // Fin de clasificación visual de parada

        const latTxt = parada.latitud ? parada.latitud.toFixed(5) : "Pendiente"; // Formatea la latitud a 5 decimales
        const lngTxt = parada.longitud ? parada.longitud.toFixed(5) : "Pendiente"; // Formatea la longitud a 5 decimales

        item.innerHTML = `
            <div class="stop-dot"></div>
            <div class="stop-order">${etiquetaParada}</div>
            <div class="stop-name">${parada.ubicacion}</div>
            <div class="stop-coords">Lat: ${latTxt} | Lng: ${lngTxt}</div>
        `; // Define la estructura interna de la tarjeta de parada

        // Evento al hacer click sobre una parada para centrar en el mapa
        item.addEventListener("click", () => { // Permite enfocar la parada en el mapa al hacer clic
            if (googleMapsLoaded && map && parada.latitud && parada.longitud) { // Si la parada ya tiene coordenadas
                map.panTo({ lat: parada.latitud, lng: parada.longitud }); // Desplaza suavemente el mapa hacia la parada
                map.setZoom(16); // Acerca el zoom para ver la calle en detalle
            } // Fin de validación de coordenadas
        }); // Fin del evento click

        listContainer.appendChild(item); // Agrega el ítem al listado lateral
    }); // Fin del forEach
} // Fin de renderizarListaParadas

/**
 * Limpia todos los elementos gráficos previos del mapa antes de dibujar una nueva ruta
 */
function limpiarElementosMapa() { // Función para remover marcadores, polilíneas y etiquetas previas
    currentMarkers.forEach(m => m.setMap(null)); // Quita cada marcador anterior del mapa
    currentMarkers = []; // Reinicia el arreglo de marcadores

    if (currentPolyline) { // Si existe una polilínea principal previa
        currentPolyline.setMap(null); // La oculta del mapa
        currentPolyline = null; // Libera la referencia
    } // Fin de limpieza de polilínea principal

    if (currentBorderPolyline) { // Si existe una polilínea de borde oscuro previa
        currentBorderPolyline.setMap(null); // La oculta del mapa
        currentBorderPolyline = null; // Libera la referencia
    } // Fin de limpieza de polilínea de borde

    if (routeCalloutWindow) { // Si existe la tarjeta flotante de tiempo/distancia en el mapa
        routeCalloutWindow.close(); // Cierra la tarjeta flotante
        routeCalloutWindow = null; // Libera la referencia
    } // Fin de limpieza de tarjeta flotante
} // Fin de limpiarElementosMapa

/**
 * Dibuja los marcadores de cada parada y traza el camino real por las calles de la ciudad
 */
function dibujarRutaEnMapa(paradas) { // Función principal para graficar el recorrido vial en Google Maps
    // 1. Limpiar marcadores, polilíneas y rutas anteriores
    limpiarElementosMapa(); // Asegura que el mapa quede limpio antes de trazar la nueva ruta

    // Filtrar únicamente las paradas que ya cuentan con coordenadas geocodificadas
    const paradasValidas = paradas.filter(p => p.latitud != null && p.longitud != null); // Filtra paradas con latitud y longitud válidas
    if (paradasValidas.length === 0) return; // Si ninguna parada tiene coordenadas aún, termina la ejecución

    const bounds = new google.maps.LatLngBounds(); // Objeto para encuadrar automáticamente toda la ruta en pantalla
    const total = paradasValidas.length; // Total de paradas con coordenadas

    // 2. Crear los marcadores de paradas estilo Google Maps (nodos circulares blancos con borde oscuro y pin rojo en el destino final)
    paradasValidas.forEach((p, idx) => { // Recorre cada parada válida
        const pos = { lat: p.latitud, lng: p.longitud }; // Construye el objeto de coordenadas LatLng
        bounds.extend(pos); // Expande los límites del mapa para incluir esta parada

        const esUltima = (idx === total - 1 && total > 1); // Determina si es la parada final del recorrido

        // Configurar ícono: Nodos circulares blancos con borde oscuro para inicio/intermedias y Pin Rojo clásico en la parada final
        const iconoMarcador = esUltima ? null : { // Si no es la última parada, usa un círculo estilo nodo de ruta de Google Maps
            path: google.maps.SymbolPath.CIRCLE, // Forma circular vectorial
            scale: idx === 0 ? 7 : 5.5, // El punto inicial es ligeramente más grande que las paradas intermedias
            fillColor: "#ffffff", // Relleno blanco puro como en Google Maps
            fillOpacity: 1, // Totalmente opaco
            strokeColor: "#1f2937", // Borde gris oscuro/negro
            strokeWeight: idx === 0 ? 3.5 : 2.5 // Grosor del anillo exterior
        }; // Fin de configuración del ícono

        const marker = new google.maps.Marker({ // Crea el marcador en el mapa
            position: pos, // Posición geográfica de la parada
            map: map, // Mapa donde se renderiza
            title: `Parada ${p.ordenParada}: ${p.ubicacion}`, // Tooltip al pasar el cursor
            icon: iconoMarcador, // Ícono personalizado o pin rojo en el destino final
            zIndex: esUltima ? 100 : (50 + idx) // Asegura que el destino final quede por encima de la línea
        }); // Fin de creación del marcador

        // Ventana de información detallada al hacer clic sobre cualquier parada
        const infoWindow = new google.maps.InfoWindow({ // Crea el globo informativo de la parada
            content: `
                <div style="font-size:13px; padding:4px; line-height:1.4;">
                    <strong style="color:#1e3c72;">Parada ${p.ordenParada}: ${p.ubicacion}</strong><br>
                    <span>🚗 Vehículo: <b>${p.vehiculo.placa}</b> (${p.vehiculo.marca})</span><br>
                    <span>👤 Conductor: ${p.persona.nombres} ${p.persona.apellidos}</span>
                </div>
            ` // Contenido HTML del globo
        }); // Fin de creación de InfoWindow

        marker.addListener("click", () => { // Abre el globo informativo al hacer clic en la parada
            infoWindow.open(map, marker); // Muestra el InfoWindow sobre el marcador
        }); // Fin del listener click

        currentMarkers.push(marker); // Guarda el marcador en el arreglo global para poder limpiarlo después
    }); // Fin del recorrido de paradas

    // Si solo hay una parada, centrar el mapa en ella
    if (paradasValidas.length === 1) { // Verifica si la ruta tiene un único punto
        map.setCenter({ lat: paradasValidas[0].latitud, lng: paradasValidas[0].longitud }); // Centra el mapa en ese punto
        map.setZoom(15); // Ajusta el zoom
        return; // Finaliza porque no hay tramo vial que unir
    } // Fin de caso de una sola parada

    // 3. Trazar el camino real siguiendo las calles y avenidas entre todas las paradas en su orden exacto
    trazarCaminoConMotorVialOSRM(paradasValidas, bounds); // Obtiene la geometría calle por calle sin depender de Directions API Legacy
} // Fin de dibujarRutaEnMapa

/**
 * Motor vial por calles (OSRM Driving API) que obtiene la geometría exacta de las calles y carreras de Ibagué
 * para que la ruta siempre siga el camino real de las vías.
 */
function trazarCaminoConMotorVialOSRM(paradasValidas, bounds) { // Consulta la red vial real entre todas las paradas
    // Formatear coordenadas en el estándar longitud,latitud separadas por punto y coma
    const coordsString = paradasValidas // Toma las paradas válidas en orden
        .map(p => `${p.longitud},${p.latitud}`) // Convierte cada parada a "lng,lat"
        .join(";"); // Une todas las paradas con ";"

    const urlOSRM = `https://router.project-osrm.org/route/v1/driving/${coordsString}?overview=full&geometries=geojson`; // URL de enrutamiento vehicular detallado

    fetch(urlOSRM) // Realiza la petición HTTP al servicio de ruteo vial
        .then(res => res.json()) // Parsea la respuesta JSON
        .then(data => { // Procesa la geometría vial recibida
            if (data && data.code === "Ok" && data.routes && data.routes.length > 0) { // Si encontró el camino por las calles
                const route = data.routes[0]; // Toma la ruta óptima por las vías
                const geojsonCoords = route.geometry.coordinates; // Arreglo de cientos de puntos siguiendo cada calle y giro

                // Convertir las coordenadas [lng, lat] del GeoJSON a objetos { lat, lng } de Google Maps
                const pathVial = geojsonCoords.map(coord => ({ // Mapea cada vértice de la calle
                    lat: coord[1], // Latitud del vértice vial
                    lng: coord[0] // Longitud del vértice vial
                })); // Fin de conversión de coordenadas

                // Dibujar la ruta sobre las calles con el estilo visual idéntico a Google Maps
                pintarDoblePolilineaEstiloGoogle(pathVial, route.distance, route.duration); // Pinta la ruta y la etiqueta de tiempo/km
            } else { // Si por alguna razón externa fallara el servidor vial
                pintarPolilineaSimpleRespaldo(paradasValidas, bounds); // Dibuja unión de respaldo
            } // Fin de verificación de respuesta OSRM
        })
        .catch(err => { // En caso de no tener conexión a internet hacia el motor vial
            console.error("Error consultando geometría vial:", err); // Registra el error
            pintarPolilineaSimpleRespaldo(paradasValidas, bounds); // Dibuja unión de respaldo
        }); // Fin de fetch OSRM
} // Fin de trazarCaminoConMotorVialOSRM

/**
 * Pinta la ruta sobre las calles con doble trazo (borde azul marino oscuro + centro azul Google Maps)
 * y coloca la etiqueta flotante con el ícono de vehículo, minutos y kilómetros igual a Google Maps.
 */
function pintarDoblePolilineaEstiloGoogle(pathVial, distanciaMetros, duracionSegundos) { // Función de renderizado visual de la vía
    // 1. Polilínea exterior (borde oscuro que da relieve sobre el mapa claro)
    currentBorderPolyline = new google.maps.Polyline({ // Crea la línea de contorno exterior
        path: pathVial, // Geometría completa calle por calle
        geodesic: true, // Sigue la curvatura terrestre
        strokeColor: "#122b85", // Azul marino profundo para el borde de la ruta
        strokeOpacity: 0.95, // Opacidad alta
        strokeWeight: 9, // Más ancha para formar el borde de 1.5px a cada lado
        zIndex: 10 // Nivel inferior de la ruta
    }); // Fin de currentBorderPolyline
    currentBorderPolyline.setMap(map); // Dibuja el borde en el mapa

    // 2. Polilínea interior (color azul vibrante clásico de navegación de Google Maps)
    currentPolyline = new google.maps.Polyline({ // Crea la línea central de la ruta
        path: pathVial, // Misma geometría calle por calle
        geodesic: true, // Sigue la curvatura terrestre
        strokeColor: "#2b5cfe", // Azul intenso característico de Google Maps
        strokeOpacity: 1.0, // Totalmente opaca
        strokeWeight: 6, // Grosor interno que deja ver el borde oscuro alrededor
        zIndex: 11 // Nivel superior sobre el borde
    }); // Fin de currentPolyline
    currentPolyline.setMap(map); // Dibuja la línea principal en el mapa

    // 3. Ajustar los límites del mapa (bounds) para encuadrar toda la geometría vial
    const routeBounds = new google.maps.LatLngBounds(); // Crea un nuevo encuadre para toda la ruta vial
    pathVial.forEach(pt => routeBounds.extend(pt)); // Incluye cada curva de la ruta en el encuadre
    map.fitBounds(routeBounds); // Ajusta el zoom y centro del mapa automáticamente

    // 4. Mostrar etiqueta flotante estilo Google Maps con el tiempo estimado (min) y distancia (km) a mitad del recorrido
    if (distanciaMetros && duracionSegundos && pathVial.length > 2) { // Verifica que existan métricas de distancia y duración
        const kmTxt = (distanciaMetros / 1000).toFixed(1) + " km"; // Convierte metros a kilómetros con 1 decimal (ej. 7.1 km)
        const minTxt = Math.max(1, Math.round(duracionSegundos / 60)) + " min"; // Convierte segundos a minutos redondeados (ej. 27 min)
        const puntoMedio = pathVial[Math.floor(pathVial.length / 2)]; // Ubica el punto central a lo largo del recorrido vial

        routeCalloutWindow = new google.maps.InfoWindow({ // Crea la etiqueta flotante sobre la ruta
            position: puntoMedio, // La posiciona en la mitad del camino vial
            disableAutoPan: true, // Evita que desplace brusco el mapa al abrirse
            content: `
                <div style="display:flex; align-items:center; gap:7px; padding:2px 6px; font-family:'Segoe UI', Roboto, sans-serif;">
                    <span style="font-size:16px;">🚗</span>
                    <div style="line-height:1.2;">
                        <div style="font-weight:700; font-size:13px; color:#1f2937;">${minTxt}</div>
                        <div style="font-size:11.5px; color:#4b5563;">${kmTxt}</div>
                    </div>
                </div>
            ` // Diseño compacto con ícono de auto, minutos en negrita y kilómetros debajo
        }); // Fin de creación de etiqueta de ruta
        routeCalloutWindow.open(map); // Muestra la etiqueta flotante sobre el mapa
    } // Fin de etiqueta de tiempo y distancia
} // Fin de pintarDoblePolilineaEstiloGoogle

/**
 * Respaldo básico únicamente si no hubiera conexión a internet para consultar la malla vial
 */
function pintarPolilineaSimpleRespaldo(paradasValidas, bounds) { // Función de contingencia sin red
    const coords = paradasValidas.map(p => ({ lat: p.latitud, lng: p.longitud })); // Extrae coordenadas de paradas
    pintarDoblePolilineaEstiloGoogle(coords, null, null); // Dibuja con el mismo estilo de doble línea
    map.fitBounds(bounds); // Ajusta el encuadre del mapa
} // Fin de pintarPolilineaSimpleRespaldo


