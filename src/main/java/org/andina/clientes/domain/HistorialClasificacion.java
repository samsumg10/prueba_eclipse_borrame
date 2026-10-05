package org.andina.clientes.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tabla historial_clasificacion: deja constancia de cada cambio de tipo de cliente y su motivo (RN-07). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("historial_clasificacion")
public class HistorialClasificacion {

    @Id
    private Long id;
    private Long clienteId;
    private TipoCliente tipoAnterior;
    private TipoCliente tipoNuevo;
    private String motivo;
    private LocalDateTime fecha;
}
