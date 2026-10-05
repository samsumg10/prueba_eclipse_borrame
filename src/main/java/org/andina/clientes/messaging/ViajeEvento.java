package org.andina.clientes.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Evento que ms-viajes publica en el exchange taxi.eventos. Cada microservicio mantiene su propia copia:
 * el contrato es el JSON, no una clase compartida.
 *
 * @param eventId identificador único del evento (permite consumidores idempotentes)
 * @param tipo    routing key: viaje.solicitado, viaje.asignado, viaje.iniciado, viaje.finalizado, viaje.cancelado
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViajeEvento(
        UUID eventId,
        String tipo,
        Long viajeId,
        Long clienteId,
        Long taxiId,
        String origen,
        String destino,
        LocalDateTime fecha) {
}
