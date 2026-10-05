package org.andina.clientes.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.andina.clientes.domain.Cliente;
import org.andina.clientes.domain.EstadoCliente;
import org.andina.clientes.domain.HistorialClasificacion;
import org.andina.clientes.domain.TipoCliente;
import org.andina.clientes.domain.TipoDocumento;
import org.andina.clientes.dto.ClienteRequest;
import org.andina.clientes.dto.ClienteResponse;
import org.andina.clientes.dto.ClienteUpdateRequest;
import org.andina.clientes.exception.BusinessException;
import org.andina.clientes.repository.ClienteRepository;
import org.andina.clientes.repository.HistorialClasificacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final HistorialClasificacionRepository historial;
    private final DatabaseClient databaseClient;
    private final ReactiveTransactionManager transactionManager;

    /** Viajes finalizados que convierten a un cliente en VIP (configurable: clientes.vip.viajes-requeridos). */
    @Value("${clientes.vip.viajes-requeridos:5}")
    private int viajesParaVip;

    public Mono<ClienteResponse> registrar(ClienteRequest request) {
        return Mono.fromCallable(() -> construirCliente(request))                 // RN-02, RN-03, RN-04
                .flatMap(cliente -> repository.existsByDocumento(cliente.getDocumento())
                        .flatMap(existe -> existe
                                ? Mono.<Cliente>error(documentoDuplicado(cliente.getDocumento()))  // RN-01
                                : repository.save(cliente)))
                // Dos registros simultáneos pueden pasar el exists: el UNIQUE de la tabla es la última defensa
                .onErrorMap(DataIntegrityViolationException.class, e -> documentoDuplicado(request.documento()))
                .map(ClienteResponse::from);
    }

    public Flux<ClienteResponse> listar(TipoCliente tipo, EstadoCliente estado) {
        return repository.findAll()
                .filter(c -> tipo == null || c.getTipoCliente() == tipo)
                .filter(c -> estado == null || c.getEstado() == estado)
                .map(ClienteResponse::from);
    }

    public Mono<ClienteResponse> buscarPorId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(noEncontrado("id " + id)))
                .map(ClienteResponse::from);
    }

    public Mono<ClienteResponse> buscarPorDocumento(String documento) {
        return repository.findByDocumento(documento)
                .switchIfEmpty(Mono.error(noEncontrado("documento " + documento)))
                .map(ClienteResponse::from);
    }

    /** Actualiza los datos de contacto. El documento y el tipo no cambian. */
    public Mono<ClienteResponse> actualizar(Long id, ClienteUpdateRequest r) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(noEncontrado("id " + id)))
                .flatMap(cliente -> {
                    // RN-04: los apellidos son obligatorios salvo para empresas (RUC 20)
                    boolean esEmpresa = cliente.getTipoDocumento() == TipoDocumento.RUC
                            && cliente.getDocumento().startsWith("20");
                    if (!esEmpresa && (r.apellidos() == null || r.apellidos().isBlank())) {
                        return Mono.<Integer>error(new BusinessException(HttpStatus.BAD_REQUEST, "RN-04",
                                "Los apellidos son obligatorios para clientes con DNI o RUC 10"));
                    }
                    return repository.actualizarDatos(id, r.nombres().trim(),
                            esEmpresa ? null : r.apellidos().trim(), r.telefono(),
                            r.correo().trim().toLowerCase(), r.direccion().trim(), r.ingresoMensual());
                })
                .then(repository.findById(id))
                .map(ClienteResponse::from);
    }

    /** Activar, inactivar o bloquear. Solo un cliente ACTIVO puede solicitar viajes (lo verifica ms-viajes). */
    public Mono<ClienteResponse> cambiarEstado(Long id, EstadoCliente estado) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(noEncontrado("id " + id)))
                .flatMap(cliente -> repository.actualizarEstado(id, estado.name()))
                .then(repository.findById(id))
                .map(ClienteResponse::from);
    }

    /**
     * Consume el evento viaje.finalizado: suma un viaje al cliente y, al llegar al mínimo, lo promueve a VIP
     * dejando constancia en historial_clasificacion. Todo ocurre en una transacción y es idempotente:
     * si RabbitMQ entrega dos veces el mismo evento (eventId), el segundo se ignora.
     */
    public Mono<Void> registrarViajeCompletado(UUID eventId, Long clienteId) {
        Mono<Void> proceso = marcarEventoProcesado(eventId)
                .filter(esNuevo -> esNuevo)
                .flatMap(nuevo -> repository.findById(clienteId))
                .flatMap(cliente -> repository.incrementarViajesCompletados(clienteId)
                        .then(repository.promoverAVip(clienteId, viajesParaVip))
                        .filter(filas -> filas > 0)
                        .flatMap(promovido -> historial.save(HistorialClasificacion.builder()
                                .clienteId(clienteId)
                                .tipoAnterior(cliente.getTipoCliente())
                                .tipoNuevo(TipoCliente.VIP)
                                .motivo("Alcanzó " + viajesParaVip + " viajes completados")
                                .fecha(LocalDateTime.now())
                                .build()))
                        .doOnNext(h -> log.info("Cliente {} promovido a VIP ({} viajes)", clienteId, viajesParaVip)))
                .then();
        return TransactionalOperator.create(transactionManager).transactional(proceso);
    }

    /** true si el evento es nuevo; false si ya se había procesado (INSERT ... ON CONFLICT DO NOTHING). */
    private Mono<Boolean> marcarEventoProcesado(UUID eventId) {
        return databaseClient.sql("INSERT INTO evento_procesado (event_id) VALUES (:id) ON CONFLICT (event_id) DO NOTHING")
                .bind("id", eventId)
                .fetch()
                .rowsUpdated()
                .map(filas -> filas > 0);
    }

    private Cliente construirCliente(ClienteRequest r) {
        String documento = r.documento();
        TipoDocumento tipoDocumento = documento.length() == 8 ? TipoDocumento.DNI : TipoDocumento.RUC;

        // RN-02: el RUC debe empezar con 10 (persona con negocio) o 20 (empresa)
        if (tipoDocumento == TipoDocumento.RUC && !documento.startsWith("10") && !documento.startsWith("20")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "RN-02", "El RUC debe empezar con 10 o 20");
        }

        TipoCliente tipo = r.tipoCliente() != null ? r.tipoCliente() : tipoPorDefecto(documento);
        validarTipo(documento, tipo);

        // RN-04: los apellidos son obligatorios salvo para empresas (RUC 20)
        boolean esEmpresa = documento.startsWith("20") && tipoDocumento == TipoDocumento.RUC;
        if (!esEmpresa && (r.apellidos() == null || r.apellidos().isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "RN-04",
                    "Los apellidos son obligatorios para clientes con DNI o RUC 10");
        }

        return Cliente.builder()
                .tipoDocumento(tipoDocumento)
                .documento(documento)
                .nombres(r.nombres().trim())
                .apellidos(esEmpresa ? null : r.apellidos().trim())
                .telefono(r.telefono())
                .correo(r.correo().trim().toLowerCase())
                .direccion(r.direccion().trim())
                .tipoCliente(tipo)
                .ingresoMensual(r.ingresoMensual())
                .estado(EstadoCliente.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .viajesCompletados(0)
                .build();
    }

    /** RN-03: si no se envía el tipo, se deriva del documento. */
    private TipoCliente tipoPorDefecto(String documento) {
        if (documento.length() == 8) {
            return TipoCliente.NORMAL;
        }
        return documento.startsWith("10") ? TipoCliente.COMERCIAL : TipoCliente.EMPRESARIAL;
    }

    /** RN-03: coherencia entre el tipo de cliente y el documento. VIP no se declara, se obtiene por promoción. */
    private void validarTipo(String documento, TipoCliente tipo) {
        boolean valido = switch (tipo) {
            case VIP -> false;
            case NORMAL -> documento.length() == 8;
            case COMERCIAL -> documento.length() == 11;
            case EMPRESARIAL -> documento.startsWith("20") && documento.length() == 11;
        };
        if (!valido) {
            String mensaje = tipo == TipoCliente.VIP
                    ? "El tipo VIP no se declara al registrar: se obtiene por promoción"
                    : "El tipo " + tipo + " no corresponde al documento " + documento;
            throw new BusinessException(HttpStatus.BAD_REQUEST, "RN-03", mensaje);
        }
    }

    private BusinessException documentoDuplicado(String documento) {
        return new BusinessException(HttpStatus.CONFLICT, "RN-01",
                "Ya existe un cliente registrado con el documento " + documento);
    }

    private BusinessException noEncontrado(String criterio) {
        return new BusinessException(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "No existe un cliente con " + criterio);
    }
}
