package org.andina.clientes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.andina.clientes.domain.Cliente;
import org.andina.clientes.domain.EstadoCliente;
import org.andina.clientes.domain.TipoCliente;
import org.andina.clientes.domain.TipoDocumento;

/** Lo que se devuelve al exterior: la entidad nunca se expone directamente. */
public record ClienteResponse(
        Long id,
        TipoDocumento tipoDocumento,
        String documento,
        String nombres,
        String apellidos,
        String telefono,
        String correo,
        String direccion,
        TipoCliente tipoCliente,
        BigDecimal ingresoMensual,
        EstadoCliente estado,
        LocalDateTime fechaRegistro,
        Integer viajesCompletados) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getTipoDocumento(), c.getDocumento(), c.getNombres(),
                c.getApellidos(), c.getTelefono(), c.getCorreo(), c.getDireccion(), c.getTipoCliente(),
                c.getIngresoMensual(), c.getEstado(), c.getFechaRegistro(), c.getViajesCompletados());
    }
}
