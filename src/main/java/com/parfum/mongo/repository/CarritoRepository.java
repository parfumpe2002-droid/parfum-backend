package com.parfum.mongo.repository;

import com.parfum.mongo.document.CarritoItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarritoRepository extends MongoRepository<CarritoItem, String> {
    List<CarritoItem> findByUsuarioIdOrderByActualizadoEnDesc(Long usuarioId);
    Optional<CarritoItem> findByUsuarioIdAndVarianteClave(Long usuarioId, String varianteClave);
    void deleteByUsuarioIdAndVarianteClave(Long usuarioId, String varianteClave);
    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    void deleteByUsuarioId(Long usuarioId);
}
