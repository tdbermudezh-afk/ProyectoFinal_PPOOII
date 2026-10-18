// Paquete de clientes externos de APIs según arquitectura del Laboratorio 3
package com.PPOOII.proyecto.APIs.GoogleMaps;

// Importación de JsonNode de Jackson para recorrer la estructura JSON devuelta por Google Maps (Laboratorio 3)
import com.fasterxml.jackson.databind.JsonNode;
// Importación de ObjectMapper de Jackson para deserializar la respuesta JSON (Laboratorio 3)
import com.fasterxml.jackson.databind.ObjectMapper;
// Importación de Logger de SLF4J para trazabilidad de geocodificación
import org.slf4j.Logger;
// Importación de LoggerFactory de SLF4J
import org.slf4j.LoggerFactory;
// Importación de @Value para inyectar la clave de API desde application.properties
import org.springframework.beans.factory.annotation.Value;
// Importación de @Component de Spring
import org.springframework.stereotype.Component;

// Importación de IOException para manejo de excepciones de entrada/salida (Laboratorio 3)
import java.io.IOException;
// Importación de URI para construir la dirección del endpoint HTTP (Laboratorio 3)
import java.net.URI;
// Importación de URLEncoder para codificar caracteres especiales en la dirección (Laboratorio 3)
import java.net.URLEncoder;
// Importación de HttpClient nativo de Java 11+ (Laboratorio 3)
import java.net.http.HttpClient;
// Importación de HttpRequest nativo de Java 11+ (Laboratorio 3)
import java.net.http.HttpRequest;
// Importación de HttpResponse nativo de Java 11+ (Laboratorio 3)
import java.net.http.HttpResponse;
// Importación de StandardCharsets para codificación UTF-8
import java.nio.charset.StandardCharsets;
// Importación de Duration para definir tiempos máximos de espera HTTP (Laboratorio 3)
import java.time.Duration;

// Componente Spring para geocodificación de direcciones usando Google Maps Geocoding API (Laboratorio 3, pág. 8)
@Component
public class Geocoder {

    // Instancia del logger para registrar eventos de geocodificación
    private static final Logger logger = LoggerFactory.getLogger(Geocoder.class);

    // URL base del recurso de Geocoding de Google Maps según el Laboratorio 3 (pág. 8)
    private static final String GEOCODING_RESOURCE = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    // Inyección de la clave de API desde application.properties (o cadena vacía por defecto)
    @Value("${google.maps.api.key:}")
    private String apiKey;

    // Cliente HTTP reutilizable con timeout de conexión
    private final HttpClient httpClient;

    // Mapeador de Jackson para lectura de nodos JSON
    private final ObjectMapper objectMapper;

    // Constructor que inicializa el cliente HTTP y el mapeador JSON
    public Geocoder() {
        // Construye el cliente HTTP con un timeout de conexión de 5 segundos
        this.httpClient = HttpClient.newBuilder()
                // Define el tiempo máximo de conexión
                .connectTimeout(Duration.ofSeconds(5))
                // Crea la instancia de HttpClient
                .build();
        // Inicializa el ObjectMapper de Jackson
        this.objectMapper = new ObjectMapper();
    }

    // Método GeocodeSync del Laboratorio 3 (pág. 8) que realiza la petición sincrónica a Google Maps y retorna el cuerpo JSON
    public String GeocodeSync(String query) throws IOException, InterruptedException {
        // Codifica la dirección en formato URL UTF-8 tal como indica el Laboratorio 3
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        // Construye la URI completa concatenando el recurso base, la dirección codificada y la API Key
        String requestUri = GEOCODING_RESOURCE + encodedQuery + "&key=" + (apiKey != null ? apiKey.trim() : "");

        // Crea la solicitud HTTP GET con un timeout de 2000 milisegundos según el Laboratorio 3 (pág. 8)
        HttpRequest geocodingRequest = HttpRequest.newBuilder()
                // Configura el método GET
                .GET()
                // Asigna la URI de consulta a Google Maps
                .uri(URI.create(requestUri))
                // Establece el timeout de 2000 ms definido en el Laboratorio 3
                .timeout(Duration.ofMillis(2000))
                // Construye el objeto HttpRequest
                .build();

        // Envía la petición de forma sincrónica y recibe la respuesta como cadena de texto
        HttpResponse<String> geocodingResponse = httpClient.send(
                // Pasa la solicitud construida
                geocodingRequest,
                // Indica que el cuerpo de la respuesta se maneje como String
                HttpResponse.BodyHandlers.ofString()
        );

        // Retorna el cuerpo JSON crudo devuelto por el API de Google Maps
        return geocodingResponse.body();
    }

    // Método getLatLng del Laboratorio 3 (pág. 8) que extrae las coordenadas y las retorna en formato "latitud,longitud"
    public String getLatLng(String query) throws IOException, InterruptedException {
        // Invoca GeocodeSync para obtener la respuesta JSON desde Google Maps
        String responseBody = GeocodeSync(query);
        // Parsea la cadena JSON en un árbol de nodos JsonNode
        JsonNode responseJsonNode = objectMapper.readTree(responseBody);
        // Accede al arreglo 'results' dentro del objeto JSON de respuesta
        JsonNode items = responseJsonNode.get("results");

        // Verifica que exista al menos un resultado válido en el nodo 'results'
        if (items != null && items.isArray() && items.size() > 0) {
            // Extrae la latitud como texto desde results[0].geometry.location.lat (Laboratorio 3, pág. 8)
            String latitude = items.get(0).get("geometry").get("location").get("lat").asText();
            // Extrae la longitud como texto desde results[0].geometry.location.lng (Laboratorio 3, pág. 8)
            String longitude = items.get(0).get("geometry").get("location").get("lng").asText();
            // Retorna la cadena concatenada en el formato exacto "latitud,longitud" del Laboratorio 3
            return latitude + "," + longitude;
        }
        // Retorna nulo si Google Maps no devolvió resultados para la dirección consultada
        return null;
    }

    // Método que consulta el servicio web de Google Maps (vía getLatLng) y retorna un arreglo double[]{latitud, longitud}
    public double[] geocodificarDireccion(String direccion) {
        // Validar que se haya suministrado una dirección válida
        if (direccion == null || direccion.trim().isEmpty()) {
            // Registra advertencia si la dirección está vacía
            logger.warn("Dirección nula o vacía enviada a geocodificar.");
            // Retorna nulo al no haber dirección que procesar
            return null;
        }

        // Si no hay API Key configurada, advertir en el log y retornar nulo sin generar error fatal
        if (apiKey == null || apiKey.trim().isEmpty() || "TU_API_KEY_AQUI".equalsIgnoreCase(apiKey.trim())) {
            // Informa en consola que no se ha configurado la clave de Google Maps
            logger.info("Geocodificación omitida: Clave de Google Maps no configurada en application.properties (google.maps.api.key).");
            // Retorna nulo de forma controlada
            return null;
        }

        try {
            // Invoca el método getLatLng del Laboratorio 3 para obtener "latitud,longitud"
            String latLngStr = getLatLng(direccion.trim());
            // Verifica que se hayan obtenido coordenadas válidas separadas por coma
            if (latLngStr != null && latLngStr.contains(",")) {
                // Separa la cadena por la coma para extraer latitud y longitud
                String[] partes = latLngStr.split(",");
                // Convierte la primera parte a double (latitud)
                double lat = Double.parseDouble(partes[0]);
                // Convierte la segunda parte a double (longitud)
                double lng = Double.parseDouble(partes[1]);
                // Registra en el log el éxito de la geocodificación
                logger.info("Geocodificación exitosa para '{}': [lat: {}, lng: {}]", direccion, lat, lng);
                // Retorna el par de coordenadas como arreglo de doubles
                return new double[]{lat, lng};
            } else {
                // Registra advertencia cuando la API no encuentra coordenadas para la dirección
                logger.warn("Google Maps no retornó coordenadas para '{}'.", direccion);
                // Retorna nulo
                return null;
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            // Registra cualquier excepción ocurrida durante el llamado HTTP o el parseo numérico
            logger.error("Excepción durante la geocodificación de '{}': {}", direccion, e.getMessage());
            // Si fue una interrupción de hilo, restaura el estado de interrupción
            if (e instanceof InterruptedException) {
                // Marca el hilo actual como interrumpido
                Thread.currentThread().interrupt();
            }
            // Retorna nulo en caso de error
            return null;
        }
    }
}
