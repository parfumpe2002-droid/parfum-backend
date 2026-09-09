package com.parfum.mongo.repository;

import com.parfum.mongo.document.Resena;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResenaRepository extends MongoRepository<Resena, String> {
    List<Resena> findByProductoIdOrderByCreadoEnDesc(Long productoId);
    Optional<Resena> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    long countByProductoId(Long productoId);
}
