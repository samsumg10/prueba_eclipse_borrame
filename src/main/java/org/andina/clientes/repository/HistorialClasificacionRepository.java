package org.andina.clientes.repository;

import org.andina.clientes.domain.HistorialClasificacion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface HistorialClasificacionRepository extends ReactiveCrudRepository<HistorialClasificacion, Long> {
}
