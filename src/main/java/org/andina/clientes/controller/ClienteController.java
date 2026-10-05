package org.andina.clientes.controller;

import org.andina.clientes.domain.EstadoCliente;
import org.andina.clientes.domain.TipoCliente;
import org.andina.clientes.dto.CambioEstadoRequest;
import org.andina.clientes.dto.ClienteRequest;
import org.andina.clientes.dto.ClienteResponse;
import org.andina.clientes.dto.ClienteUpdateRequest;
import org.andina.clientes.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ClienteResponse> registrar(@Valid @RequestBody ClienteRequest request) {
        return service.registrar(request);
    }

    @GetMapping
    public Flux<ClienteResponse> listar(@RequestParam(required = false) TipoCliente tipo,
            @RequestParam(required = false) EstadoCliente estado) {
        return service.listar(tipo, estado);
    }

    @GetMapping("/{id}")
    public Mono<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/documento/{documento}")
    public Mono<ClienteResponse> buscarPorDocumento(@PathVariable String documento) {
        return service.buscarPorDocumento(documento);
    }

    @PutMapping("/{id}")
    public Mono<ClienteResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteUpdateRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public Mono<ClienteResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }
}
