package org.andina.clientes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos modificables de un cliente. El documento y el tipo no cambian: el documento identifica al cliente
 * y el tipo VIP se obtiene solo por promoción.
 */
public record ClienteUpdateRequest(

        @NotBlank(message = "Los nombres o la razón social son obligatorios")
        @Size(max = 150)
        String nombres,

        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
        String telefono,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120)
        String correo,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200)
        String direccion,

        @NotNull(message = "El ingreso mensual es obligatorio")
        @Positive(message = "El ingreso mensual debe ser mayor que 0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal ingresoMensual) {
}
