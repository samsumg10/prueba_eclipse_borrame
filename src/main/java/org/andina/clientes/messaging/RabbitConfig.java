package org.andina.clientes.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de RabbitMQ de ms-clientes. El exchange taxi.eventos lo comparten todos los servicios (se declara
 * igual en cada uno); la cola es propia de ms-clientes, de modo que recibe su propia copia de cada evento
 * aunque ms-taxis consuma el mismo. Los mensajes que fallan tras los reintentos pasan a la cola .dlq.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_EVENTOS = "taxi.eventos";
    public static final String COLA_VIAJE_FINALIZADO = "clientes.viaje-finalizado";
    public static final String COLA_VIAJE_FINALIZADO_DLQ = COLA_VIAJE_FINALIZADO + ".dlq";

    @Bean
    public TopicExchange taxiEventos() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public Queue colaViajeFinalizado() {
        return QueueBuilder.durable(COLA_VIAJE_FINALIZADO)
                .deadLetterExchange("")                          // exchange por defecto: enruta por nombre de cola
                .deadLetterRoutingKey(COLA_VIAJE_FINALIZADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaViajeFinalizadoDlq() {
        return QueueBuilder.durable(COLA_VIAJE_FINALIZADO_DLQ).build();
    }

    @Bean
    public Binding bindingViajeFinalizado() {
        return BindingBuilder.bind(colaViajeFinalizado()).to(taxiEventos()).with("viaje.finalizado");
    }

    /** Los eventos viajan como JSON; Spring Boot lo aplica a los @RabbitListener. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
