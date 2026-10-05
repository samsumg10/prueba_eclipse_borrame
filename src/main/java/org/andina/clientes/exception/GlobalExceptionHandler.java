package org.andina.clientes.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> negocio(BusinessException ex) {
        return respuesta(ex.getStatus(), ex.getCodigo(), ex.getMessage(), null);
    }

    /** Falla alguna anotación de ClienteRequest (@NotBlank, @Pattern, @Email...). */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> validacion(WebExchangeBindException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "Datos de entrada inválidos", campos);
    }

    /** JSON mal formado o un valor de enum que no existe (por ejemplo, tipoCliente = "OTRO"). */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, Object>> entrada(ServerWebInputException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "ENTRADA_INVALIDA", ex.getReason(), null);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String codigo, String mensaje,
            Map<String, String> campos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("fecha", LocalDateTime.now());
        cuerpo.put("status", status.value());
        cuerpo.put("codigo", codigo);
        cuerpo.put("mensaje", mensaje);
        if (campos != null) {
            cuerpo.put("errores", campos);
        }
        return ResponseEntity.status(status).body(cuerpo);
    }
}
