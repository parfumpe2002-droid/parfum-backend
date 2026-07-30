package com.parfum.mongo.repository;

import com.parfum.mongo.document.Contacto;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ContactoRepository extends MongoRepository<Contacto, String> {
    List<Contacto> findAllByOrderByCreadoEnDesc();
    long countByEstado(String estado);
    @Transactional
    long deleteByEstado(String estado);
}
