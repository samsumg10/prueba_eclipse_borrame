package org.andina.clientes.messaging;

import org.andina.clientes.service.ClienteService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViajeEventListener {

    private final ClienteService clienteService;

    /**
     * viaje.finalizado: suma el viaje al cliente. El listener corre en un hilo del contenedor de RabbitMQ
     * (no en uno de Netty), por lo que bloquear aquí es seguro; si falla, se reintenta y al final va a la .dlq.
     */
    @RabbitListener(queues = RabbitConfig.COLA_VIAJE_FINALIZADO)
    public void viajeFinalizado(ViajeEvento evento) {
        log.info("Evento {} recibido (viaje {}, cliente {})", evento.tipo(), evento.viajeId(), evento.clienteId());
        clienteService.registrarViajeCompletado(evento.eventId(), evento.clienteId()).block();
    }
}
