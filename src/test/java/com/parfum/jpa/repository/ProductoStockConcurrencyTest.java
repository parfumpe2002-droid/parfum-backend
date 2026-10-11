package com.parfum.jpa.repository;

import com.parfum.jpa.entity.Producto;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = ProductoStockConcurrencyTest.JpaOnly.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductoStockConcurrencyTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.parfum.jpa.entity")
    @EnableJpaRepositories(basePackages = "com.parfum.jpa.repository")
    static class JpaOnly {}
    @Autowired ProductoRepository productos;
    @Autowired TransactionTemplate tx;

    @Test
    void soloUnaCompraConfirmaLaUltimaUnidad() throws Exception {
        Long id = tx.execute(status -> {
            Producto p = new Producto();
            p.setNombre("TEST CONCURRENCIA");
            p.setMarca("TEST");
            p.setCategoria("TEST");
            p.setSku("TEST-CONC-"+System.nanoTime());
            p.setSlug("test-conc-"+System.nanoTime());
            p.setPrecio(new BigDecimal("30.00"));
            p.setStock(1);
            return productos.saveAndFlush(p).getId();
        });

        var pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        Callable<Boolean> reservar = () -> {
            ready.countDown();
            if (!fire.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout");
            return tx.execute(st -> {
                Producto p = productos.findByIdForUpdate(id).orElseThrow();
                if (p.getStock() < 1) return false;
                p.setStock(p.getStock() - 1);
                productos.saveAndFlush(p);
                return true;
            });
        };

        try {
            Future<Boolean> a = pool.submit(reservar);
            Future<Boolean> b = pool.submit(reservar);
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            fire.countDown();
            int confirmadas = (a.get(20, TimeUnit.SECONDS) ? 1 : 0) + (b.get(20, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, confirmadas);
            Integer restante = tx.execute(st -> productos.findById(id).orElseThrow().getStock());
            assertNotNull(restante);
            assertEquals(0, restante.intValue());
        } finally {
            fire.countDown();
            pool.shutdownNow();
            tx.executeWithoutResult(st -> productos.deleteById(id));
        }
    }
}
