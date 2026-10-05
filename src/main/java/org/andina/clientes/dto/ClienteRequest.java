package org.andina.clientes.dto;

import java.math.BigDecimal;

import org.andina.clientes.domain.TipoCliente;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos para registrar un cliente. Las validaciones de formato cubren RN-02 y RN-05. */
public record ClienteRequest(

        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = "\\d{8}|\\d{11}", message = "Debe ser un DNI de 8 dígitos o un RUC de 11 dígitos")
        String documento,

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

        /** Opcional: si no se envía, se deriva del documento (RN-03). */
        TipoCliente tipoCliente,

        @NotNull(message = "El ingreso mensual es obligatorio")
        @Positive(message = "El ingreso mensual debe ser mayor que 0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal ingresoMensual) {
}
