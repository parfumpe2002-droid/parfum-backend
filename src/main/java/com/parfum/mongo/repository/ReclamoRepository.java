package com.parfum.mongo.repository;

import com.parfum.mongo.document.Reclamo;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReclamoRepository extends MongoRepository<Reclamo, String> {
    List<Reclamo> findAllByOrderByCreadoEnDesc();
}
