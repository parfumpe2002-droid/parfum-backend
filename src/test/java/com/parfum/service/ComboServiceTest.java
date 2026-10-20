package com.parfum.service;

import com.parfum.dto.ComboDtos.ComboRequest;
import com.parfum.dto.ComboDtos.ComponenteRequest;
import com.parfum.jpa.entity.Combo;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.repository.ComboRepository;
import com.parfum.jpa.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ComboServiceTest {
    private final ComboRepository combos = mock(ComboRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final ComboService service = new ComboService(combos, productos);

    private Producto product(long id, long sizeId, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Perfume " + id);
        p.setMarca("Marca");
        p.setActivo(true);
        ProductoPresentacion presentation = new ProductoPresentacion();
        presentation.setId(sizeId);
        presentation.setMililitros(100);
        presentation.setPrecio(new BigDecimal("45.00"));
        presentation.setStock(stock);
        presentation.setActivo(true);
        p.agregarPresentacion(presentation);
        when(productos.findById(id)).thenReturn(Optional.of(p));
        return p;
    }

    private ComboRequest combo(boolean published) {
        return new ComboRequest("Combo verano", "Dos fragancias", new BigDecimal("70.00"),
                null,published,List.of(
                    new ComponenteRequest(1L, 11L, "BOTELLA", 1),
                    new ComponenteRequest(2L, 22L, "BOTELLA", 2)
                ));
    }

    @BeforeEach
    void setup() {
        when(combos.saveAndFlush(any(Combo.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void noAlteraPrecioNiStockOriginalAlCrearCombo() {
        Producto first = product(1L, 11L, 5);
        Producto second = product(2L, 22L, 8);

        var saved = service.guardar(null,combo(true));

        assertEquals(new BigDecimal("70.00"), saved.precio());
        assertEquals(4,saved.stockEstimado()); // 8 / 2, frente a 5 / 1
        assertEquals(5,first.getPresentaciones().get(0).getStock());
        assertEquals(8,second.getPresentaciones().get(0).getStock());
        verify(productos,never()).save(any());
    }

    @Test
    void impidePublicarSinStock() {
        product(1L,11L,5);
        product(2L,22L,1);
        assertThrows(ResponseStatusException.class, () -> service.guardar(null,combo(true)));
        verify(combos,never()).saveAndFlush(any());
    }

    @Test
    void mantieneCombosSinPublicarFueraDelCatalogoPublico() {
        when(combos.findByPublicadoTrueOrderByActualizadoEnDesc()).thenReturn(List.of());
        assertTrue(service.publicos().isEmpty());
        verify(combos).findByPublicadoTrueOrderByActualizadoEnDesc();
    }

    @Test
    void impideVarianteInexistente() {
        product(1L,11L,5);
        product(2L,22L,5);
        ComboRequest request = new ComboRequest("Combo",null,new BigDecimal("25.00"),null,false,
                List.of(new ComponenteRequest(1L,999L,"BOTELLA",1),
                        new ComponenteRequest(2L,22L,"BOTELLA",1)));
        assertThrows(ResponseStatusException.class, () -> service.guardar(null,request));
        verify(combos,never()).saveAndFlush(any());
    }
}
