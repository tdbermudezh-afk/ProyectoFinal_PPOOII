package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.MensajeAlertaDTO;
import com.PPOOII.proyecto.services.RabbitMQConsumerService;
import com.PPOOII.proyecto.services.RabbitMQProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rabbitmq")
@Tag(name = "RabbitMQ - Prueba de Concepto", description = "Endpoints para interactuar con la cola de mensajería RabbitMQ (Productor y Consumidor)")
public class RabbitMQController {

    private final RabbitMQProducerService producerService;
    private final RabbitMQConsumerService consumerService;

    public RabbitMQController(RabbitMQProducerService producerService, RabbitMQConsumerService consumerService) {
        this.producerService = producerService;
        this.consumerService = consumerService;
    }

    @PostMapping("/producir")
    @Operation(summary = "Publicar un mensaje en la cola de RabbitMQ (Productor)", 
               description = "Envía un mensaje o alerta a la cola sin importar si el consumidor está vivo o caído.")
    public ResponseEntity<Map<String, Object>> enviarMensaje(@RequestBody MensajeAlertaDTO mensaje) {
        MensajeAlertaDTO mensajeEnviado = producerService.enviarMensaje(mensaje);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("estado", "EXITOSO");
        respuesta.put("mensaje", "Mensaje publicado exitosamente en la cola de RabbitMQ.");
        respuesta.put("consumidorActivoActualmente", consumerService.estaActivo());
        respuesta.put("datos", mensajeEnviado);
        
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/producir/lote")
    @Operation(summary = "Publicar múltiples mensajes en lote", 
               description = "Envía una cantidad N de mensajes de prueba a la cola para evidenciar la acumulación cuando el consumidor está detenido.")
    public ResponseEntity<Map<String, Object>> enviarMensajesLote(
            @RequestParam(defaultValue = "5") int cantidad,
            @RequestParam(defaultValue = "ABC123") String placa) {
        
        for (int i = 1; i <= cantidad; i++) {
            MensajeAlertaDTO msg = new MensajeAlertaDTO();
            msg.setTitulo("Alerta de Prueba #" + i);
            msg.setContenido("Verificación de desacoplamiento encolando mensaje número " + i);
            msg.setPlacaVehiculo(placa);
            msg.setTipoEvento("TEST_CONCURRENCIA");
            producerService.enviarMensaje(msg);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("estado", "EXITOSO");
        respuesta.put("mensajesEnviados", cantidad);
        respuesta.put("consumidorActivo", consumerService.estaActivo());
        respuesta.put("nota", "Revisa la consola de RabbitMQ en http://localhost:15672 para ver los mensajes en cola.");

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/consumidor/estado")
    @Operation(summary = "Consultar estado del Consumidor", 
               description = "Indica si el consumidor de la API está escuchando actualmente la cola.")
    public ResponseEntity<Map<String, Object>> estadoConsumidor() {
        Map<String, Object> res = new HashMap<>();
        res.put("consumidorActivo", consumerService.estaActivo());
        res.put("totalMensajesProcesados", consumerService.getMensajesProcesados().size());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/consumidor/detener")
    @Operation(summary = "Detener el Consumidor (Prueba de desacoplamiento)", 
               description = "Pausa la escucha del consumidor para demostrar que el productor sigue funcionando y los mensajes se acumulan en RabbitMQ.")
    public ResponseEntity<Map<String, Object>> detenerConsumidor() {
        boolean detenido = consumerService.detenerConsumidor();
        Map<String, Object> res = new HashMap<>();
        res.put("consumidorActivo", consumerService.estaActivo());
        res.put("accion", detenido ? "Consumidor detenido exitosamente." : "El consumidor ya estaba detenido.");
        res.put("instruccion", "Ahora envía mensajes con /producir y verás en RabbitMQ Management cómo se acumulan en Ready.");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/consumidor/reanudar")
    @Operation(summary = "Reanudar el Consumidor", 
               description = "Vuelve a activar el consumidor para que consuma todos los mensajes acumulados en la cola.")
    public ResponseEntity<Map<String, Object>> reanudarConsumidor() {
        boolean reanudado = consumerService.reanudarConsumidor();
        Map<String, Object> res = new HashMap<>();
        res.put("consumidorActivo", consumerService.estaActivo());
        res.put("accion", reanudado ? "Consumidor reanudado. Procesando mensajes acumulados..." : "El consumidor ya estaba en ejecución.");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/consumidor/mensajes-procesados")
    @Operation(summary = "Ver mensajes procesados por el consumidor", 
               description = "Lista los mensajes que el consumidor ha recibido y procesado desde la cola.")
    public ResponseEntity<List<MensajeAlertaDTO>> obtenerMensajesProcesados() {
        return ResponseEntity.ok(consumerService.getMensajesProcesados());
    }

    @DeleteMapping("/consumidor/mensajes-procesados")
    @Operation(summary = "Limpiar historial de mensajes procesados")
    public ResponseEntity<String> limpiarHistorial() {
        consumerService.limpiarHistorial();
        return ResponseEntity.ok("Historial limpiado.");
    }
}

