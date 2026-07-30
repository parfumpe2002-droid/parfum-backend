package com.parfum.mongo.repository;

import com.parfum.mongo.document.Favorito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FavoritoRepository extends MongoRepository<Favorito, String> {
    List<Favorito> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
    Optional<Favorito> findByUsuarioIdAndVarianteClave(Long usuarioId, String varianteClave);
    void deleteByUsuarioIdAndVarianteClave(Long usuarioId, String varianteClave);
    long countByUsuarioId(Long usuarioId);
}
