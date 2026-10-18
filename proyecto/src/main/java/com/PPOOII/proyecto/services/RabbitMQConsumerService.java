package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.config.RabbitMQConfig;
import com.PPOOII.proyecto.entities.MensajeAlertaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageListenerContainer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RabbitMQConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumerService.class);

    private final RabbitListenerEndpointRegistry registry;
    // Historial en memoria de mensajes recibidos por el consumidor para demostración
    private final List<MensajeAlertaDTO> mensajesProcesados = Collections.synchronizedList(new ArrayList<>());

    public RabbitMQConsumerService(RabbitListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    /**
     * Consumidor que escucha la cola definida en la configuración.
     * Cuenta con el id RabbitMQConfig.LISTENER_ID_CONSUMER para poder pausarlo/iniciarlo dinámicamente.
     */
    @RabbitListener(id = RabbitMQConfig.LISTENER_ID_CONSUMER, queues = "${app.rabbitmq.queue:cola_vehiculos_alertas}")
    public void recibirMensaje(MensajeAlertaDTO mensaje) {
        logger.info("[CONSUMIDOR] ¡Mensaje procesado con éxito desde la cola! ID: {}, Evento: {}, Placa: {}, Contenido: {}", 
                mensaje.getId(), mensaje.getTipoEvento(), mensaje.getPlacaVehiculo(), mensaje.getContenido());
        mensajesProcesados.add(mensaje);
    }

    public List<MensajeAlertaDTO> getMensajesProcesados() {
        return new ArrayList<>(mensajesProcesados);
    }

    public void limpiarHistorial() {
        mensajesProcesados.clear();
    }

    /**
     * Permite detener el consumidor para demostrar que el productor sigue enviando a la cola
     * sin que el consumidor esté activo.
     */
    public boolean detenerConsumidor() {
        MessageListenerContainer listener = registry.getListenerContainer(RabbitMQConfig.LISTENER_ID_CONSUMER);
        if (listener != null && listener.isRunning()) {
            listener.stop();
            logger.warn("[CONSUMIDOR] El consumidor ha sido DETENIDO para la prueba de concepto.");
            return true;
        }
        return false;
    }

    /**
     * Reanuda el consumidor: procesará todos los mensajes que quedaron acumulados en RabbitMQ.
     */
    public boolean reanudarConsumidor() {
        MessageListenerContainer listener = registry.getListenerContainer(RabbitMQConfig.LISTENER_ID_CONSUMER);
        if (listener != null && !listener.isRunning()) {
            listener.start();
            logger.info("[CONSUMIDOR] El consumidor ha sido REANUDADO. Procesando mensajes pendientes...");
            return true;
        }
        return false;
    }

    public boolean estaActivo() {
        MessageListenerContainer listener = registry.getListenerContainer(RabbitMQConfig.LISTENER_ID_CONSUMER);
        return listener != null && listener.isRunning();
    }
}
