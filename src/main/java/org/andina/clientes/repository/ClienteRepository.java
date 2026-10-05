package org.andina.clientes.repository;

import java.math.BigDecimal;

import org.andina.clientes.domain.Cliente;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface ClienteRepository extends ReactiveCrudRepository<Cliente, Long> {

    Mono<Boolean> existsByDocumento(String documento);

    Mono<Cliente> findByDocumento(String documento);

    /**
     * Actualiza solo los datos editables. Un UPDATE parcial (en vez de save) no pisa viajes_completados ni
     * tipo_cliente si un evento de viaje los modifica entre la lectura y la escritura.
     */
    @Modifying
    @Query("UPDATE cliente SET nombres = :nombres, apellidos = :apellidos, telefono = :telefono, "
            + "correo = :correo, direccion = :direccion, ingreso_mensual = :ingreso, "
            + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> actualizarDatos(@Param("id") Long id, @Param("nombres") String nombres,
            @Param("apellidos") String apellidos, @Param("telefono") String telefono,
            @Param("correo") String correo, @Param("direccion") String direccion,
            @Param("ingreso") BigDecimal ingreso);

    @Modifying
    @Query("UPDATE cliente SET estado = :estado, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> actualizarEstado(@Param("id") Long id, @Param("estado") String estado);

    /** Incremento atómico en la BD: dos viajes que terminan a la vez no se pisan el contador. */
    @Modifying
    @Query("UPDATE cliente SET viajes_completados = viajes_completados + 1, "
            + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> incrementarViajesCompletados(@Param("id") Long id);

    /** Promueve a VIP solo si aún no lo es y alcanzó el mínimo de viajes. Devuelve las filas modificadas (0 o 1). */
    @Modifying
    @Query("UPDATE cliente SET tipo_cliente = 'VIP', fecha_actualizacion = CURRENT_TIMESTAMP "
            + "WHERE id = :id AND tipo_cliente <> 'VIP' AND viajes_completados >= :minimo")
    Mono<Integer> promoverAVip(@Param("id") Long id, @Param("minimo") int minimo);
}
