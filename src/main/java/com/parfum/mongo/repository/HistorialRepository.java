package com.parfum.mongo.repository;

import com.parfum.mongo.document.HistorialVisita;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HistorialRepository extends MongoRepository<HistorialVisita, String> {
    List<HistorialVisita> findTop30ByUsuarioIdOrderByVistoEnDesc(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}
