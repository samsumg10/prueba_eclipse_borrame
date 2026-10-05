package org.andina.clientes.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla cliente de db_clientes. R2DBC mapea camelCase a snake_case
 * (tipoCliente -> tipo_cliente) y los enums como texto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cliente")
public class Cliente {

    @Id
    private Long id;
    private TipoDocumento tipoDocumento;
    private String documento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private String direccion;
    private TipoCliente tipoCliente;
    private BigDecimal ingresoMensual;
    private EstadoCliente estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    /** Viajes finalizados; lo actualiza el evento viaje.finalizado y decide la promoción a VIP. */
    private Integer viajesCompletados;
}
