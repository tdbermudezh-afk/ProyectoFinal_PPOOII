// Paquete de clientes externos de APIs según arquitectura de Laboratorio 3
package com.PPOOII.proyecto.APIs.GoogleMaps;

// Importaciones de Jackson para procesar respuestas JSON
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Importaciones para peticiones HTTP nativas en Java
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

// Componente Spring para geocodificación de direcciones usando Google Maps Geocoding API
@Component
public class Geocoder {

    // Instancia del logger para registrar eventos de geocodificación
    private static final Logger logger = LoggerFactory.getLogger(Geocoder.class);

    // URL base del recurso de Geocoding de Google Maps
    private static final String GEOCODING_RESOURCE = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    // Inyección de la clave de API desde application.properties (o valor por defecto si no está configurada)
    @Value("${google.maps.api.key:}")
    private String apiKey;

    // Cliente HTTP reutilizable con timeout de conexión
    private final HttpClient httpClient;

    // Mapeador de Jackson para lectura de nodos JSON
    private final ObjectMapper objectMapper;

    // Constructor que inicializa el cliente HTTP y el mapeador JSON
    public Geocoder() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // Método que consulta el servicio web de Google Maps y extrae latitud y longitud
    public double[] geocodificarDireccion(String direccion) {
        // Validar que se haya suministrado una dirección válida
        if (direccion == null || direccion.trim().isEmpty()) {
            logger.warn("Dirección nula o vacía enviada a geocodificar.");
            return null;
        }

        // Si no hay API Key configurada, advertir en el log y retornar nulo sin generar error fatal
        if (apiKey == null || apiKey.trim().isEmpty() || "TU_API_KEY_AQUI".equalsIgnoreCase(apiKey.trim())) {
            logger.info("Geocodificación omitida: Clave de Google Maps no configurada en application.properties (google.maps.api.key).");
            return null;
        }

        try {
            // Codificar la dirección en formato UTF-8 para URL segura
            String encodedAddress = URLEncoder.encode(direccion.trim(), StandardCharsets.UTF_8);

            // Construir la URL completa de la petición
            String requestUrl = GEOCODING_RESOURCE + encodedAddress + "&key=" + apiKey.trim();

            // Construir la solicitud HTTP GET con timeout
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            // Ejecutar la petición HTTP de manera sincrónica
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Si la respuesta HTTP no es 200 OK, registrar advertencia
            if (response.statusCode() != 200) {
                logger.error("Error HTTP al consultar Google Maps Geocoding: Código {}", response.statusCode());
                return null;
            }

            // Parsear la respuesta JSON retornada por Google Maps
            JsonNode rootNode = objectMapper.readTree(response.body());
            String status = rootNode.path("status").asText();

            // Si el estado de la API es OK y existen resultados
            if ("OK".equalsIgnoreCase(status) && rootNode.has("results") && rootNode.get("results").size() > 0) {
                JsonNode locationNode = rootNode.get("results").get(0)
                        .path("geometry")
                        .path("location");

                double lat = locationNode.path("lat").asDouble();
                double lng = locationNode.path("lng").asDouble();

                logger.info("Geocodificación exitosa para '{}': [lat: {}, lng: {}]", direccion, lat, lng);
                return new double[]{lat, lng};
            } else {
                logger.warn("Google Maps no retornó coordenadas para '{}'. Estado API: {}", direccion, status);
                return null;
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Excepción durante la geocodificación de '{}': {}", direccion, e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

