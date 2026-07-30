package com.parfum.mongo.repository;

import com.parfum.mongo.document.ActividadSitio;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ActividadSitioRepository extends MongoRepository<ActividadSitio, String> {
    List<ActividadSitio> findByCreadoEnAfterOrderByCreadoEnDesc(Instant desde);
    List<ActividadSitio> findByOrderByCreadoEnDesc(Pageable pageable);
    long countByTipoAndCreadoEnAfter(String tipo, Instant desde);
    @Transactional
    long deleteByCreadoEnBefore(Instant limite);
}
