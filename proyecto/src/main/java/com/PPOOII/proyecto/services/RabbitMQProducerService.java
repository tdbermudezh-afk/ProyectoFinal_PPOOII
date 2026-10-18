package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.MensajeAlertaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitMQProducerService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQProducerService.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:exchange_vehiculos}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey:routingkey_vehiculos}")
    private String routingKey;

    public RabbitMQProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica un mensaje DTO en el exchange de RabbitMQ
     */
    public MensajeAlertaDTO enviarMensaje(MensajeAlertaDTO mensaje) {
        if (mensaje.getId() == null || mensaje.getId().trim().isEmpty()) {
            mensaje.setId(UUID.randomUUID().toString());
        }
        logger.info("[PRODUCTOR] Enviando mensaje a exchange='{}', routingKey='{}' -> {}", exchange, routingKey, mensaje);
        rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);
        return mensaje;
    }
}

