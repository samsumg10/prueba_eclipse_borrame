package org.andina.clientes.dto;

import org.andina.clientes.domain.EstadoCliente;

import jakarta.validation.constraints.NotNull;

/** Activar, inactivar o bloquear a un cliente. Solo un cliente ACTIVO puede solicitar viajes. */
public record CambioEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoCliente estado) {
}
