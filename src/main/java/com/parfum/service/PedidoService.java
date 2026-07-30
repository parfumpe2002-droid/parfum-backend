package com.parfum.service;

import com.parfum.dto.CommerceDtos.CrearPedidoRequest;
import com.parfum.dto.CommerceDtos.DetallePedidoResponse;
import com.parfum.dto.CommerceDtos.ItemPedidoRequest;
import com.parfum.dto.CommerceDtos.PedidoResponse;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.DetallePedido;
import com.parfum.jpa.entity.EstadoPago;
import com.parfum.jpa.entity.EstadoPedido;
import com.parfum.jpa.entity.Pedido;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.entity.Rol;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.PedidoRepository;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.jpa.repository.UsuarioRepository;
import com.parfum.mongo.repository.CarritoRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         CarritoRepository carritoRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PedidoResponse crear(Usuario user, CrearPedidoRequest request) {
        Usuario owner = user != null ? user : resolveGuestUser(request);
        Pedido pedido = new Pedido();
        pedido.setUsuario(owner);
        pedido.setClienteNombre(firstNonBlank(request.nombreCliente(), owner.getNombre()));
        pedido.setClienteCorreo(firstNonBlank(request.correoCliente(), owner.getEmail()));
        pedido.setClienteTelefono(firstNonBlank(request.telefonoContacto(), owner.getTelefono()));
        pedido.setMetodoPago(normalizePaymentMethod(request.metodoPago()));
        pedido.setNumeroOperacion(clean(request.numeroOperacion()));

        String proofUrl = clean(request.comprobanteUrl());
        String proofPublicId = clean(request.comprobantePublicId());
        if (proofUrl != null) {
            if (!proofUrl.startsWith("https://res.cloudinary.com/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comprobante de pago no es válido");
            }
            pedido.setComprobanteUrl(proofUrl);
            pedido.setComprobantePublicId(proofPublicId);
        }
        pedido.setEstadoPago(EstadoPago.PENDIENTE_VERIFICACION);
        pedido.setDireccionEntrega(request.direccionEntrega().trim());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        int paidDecantUnits = 0;

        for (ItemPedidoRequest item : request.items()) {
            Producto producto = findProduct(item.productoId());
            String type = normalizeItemType(item.tipoItem(), item.productoDecantId());

            if ("DECANT".equals(type)) {
                ProductoDecant decant = resolveDecant(producto, item.productoDecantId());
                validateCommercial(decant.getPrecio(), decant.getStock(), item.cantidad(), producto.getNombre());
                decant.setStock(decant.getStock() - item.cantidad());
                pedido.agregarDetalle(createDecantDetail(producto, decant, item.cantidad(), false));
                total = total.add(decant.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
                paidDecantUnits += item.cantidad();
            } else {
                ProductoPresentacion presentacion = resolvePresentation(producto, item.presentacionId());
                BigDecimal price = presentacion == null ? producto.getPrecio() : presentacion.getPrecio();
                Integer stock = presentacion == null ? producto.getStock() : presentacion.getStock();
                validateCommercial(price, stock, item.cantidad(), producto.getNombre());
                if (presentacion == null) {
                    producto.setStock(stock - item.cantidad());
                } else {
                    presentacion.setStock(stock - item.cantidad());
                    producto.recalcularResumenComercial();
                }
                pedido.agregarDetalle(createBottleDetail(producto, presentacion, price, item.cantidad()));
                total = total.add(price.multiply(BigDecimal.valueOf(item.cantidad())));
            }
        }

        if (paidDecantUnits >= 3 && request.regaloProductoId() != null) {
            addArabicGift(pedido, request.regaloProductoId());
        }

        pedido.setTotal(total);
        Pedido saved = pedidoRepository.save(pedido);
        if (user != null) carritoRepository.deleteByUsuarioId(user.getId());
        return toResponse(saved);
    }

    private Producto findProduct(Long productId) {
        return productoRepository.findById(productId)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: " + productId));
    }

    private ProductoDecant resolveDecant(Producto producto, Long productoDecantId) {
        if (!producto.isDecantDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    producto.getNombre() + " no está disponible en decant");
        }
        if (productoDecantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona el envase del decant");
        }
        return producto.getDecants().stream()
                .filter(ProductoDecant::isActivo)
                .filter(item -> item.getEnvase() != null && item.getEnvase().isActivo())
                .filter(item -> productoDecantId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Presentación de decant no disponible para " + producto.getNombre()));
    }

    private ProductoPresentacion resolvePresentation(Producto producto, Long presentationId) {
        List<ProductoPresentacion> active = producto.getPresentaciones().stream()
                .filter(ProductoPresentacion::isActivo)
                .sorted(Comparator.comparing(ProductoPresentacion::getOrdenVisual)
                        .thenComparing(ProductoPresentacion::getMililitros))
                .toList();
        if (active.isEmpty()) return null;
        if (presentationId != null) {
            return active.stream().filter(item -> presentationId.equals(item.getId())).findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Presentación no disponible para " + producto.getNombre()));
        }
        return active.stream()
                .filter(item -> item.getPrecio() != null && item.getPrecio().signum() > 0)
                .filter(item -> item.getStock() != null && item.getStock() > 0)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay presentaciones disponibles para " + producto.getNombre()));
    }

    private void addArabicGift(Pedido pedido, Long giftProductId) {
        Producto giftProduct = findProduct(giftProductId);
        if (!normalize(giftProduct.getCategoria()).contains("arabe")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El decant gratuito debe pertenecer a la categoría Árabe");
        }
        ProductoDecant giftDecant = giftProduct.getDecants().stream()
                .filter(ProductoDecant::isActivo)
                .filter(item -> item.getEnvase() != null && item.getEnvase().isActivo())
                .filter(item -> Integer.valueOf(3).equals(item.getEnvase().getMililitros()))
                .filter(item -> item.getStock() != null && item.getStock() > 0)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "El perfume árabe elegido no tiene decant de 3 ml disponible"));
        giftDecant.setStock(giftDecant.getStock() - 1);
        pedido.agregarDetalle(createDecantDetail(giftProduct, giftDecant, 1, true));
    }

    private DetallePedido createBottleDetail(Producto producto,
                                              ProductoPresentacion presentacion,
                                              BigDecimal price,
                                              int quantity) {
        DetallePedido detail = new DetallePedido();
        detail.setProductoId(producto.getId());
        detail.setPresentacionId(presentacion == null ? null : presentacion.getId());
        detail.setProductoDecantId(null);
        detail.setTipoItem("BOTELLA");
        detail.setRegalo(false);
        detail.setMililitros(presentacion == null ? null : presentacion.getMililitros());
        detail.setPresentacion(presentacion == null ? "Presentación estándar" : presentacion.getEtiqueta());
        detail.setNombreProducto(producto.getNombre());
        detail.setImagenUrl(producto.getImagenUrl() != null ? producto.getImagenUrl() : producto.getFallbackImage());
        detail.setPrecioUnitario(price);
        detail.setCantidad(quantity);
        return detail;
    }

    private DetallePedido createDecantDetail(Producto producto,
                                              ProductoDecant decant,
                                              int quantity,
                                              boolean gift) {
        DecantEnvase envase = decant.getEnvase();
        DetallePedido detail = new DetallePedido();
        detail.setProductoId(producto.getId());
        detail.setPresentacionId(null);
        detail.setProductoDecantId(decant.getId());
        detail.setTipoItem(gift ? "REGALO_DECANT" : "DECANT");
        detail.setRegalo(gift);
        detail.setMililitros(envase.getMililitros());
        detail.setPresentacion((gift ? "REGALO · " : "DECANT · ")
                + envase.getMililitros() + " ml · " + envase.getNombre());
        detail.setNombreProducto(producto.getNombre());
        detail.setImagenUrl(envase.getImagenUrl() != null ? envase.getImagenUrl() : envase.getFallbackImage());
        detail.setPrecioUnitario(gift ? BigDecimal.ZERO : decant.getPrecio());
        detail.setCantidad(quantity);
        return detail;
    }

    private void validateCommercial(BigDecimal price, Integer stock, int quantity, String productName) {
        if (price == null || price.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Precio no disponible para " + productName);
        }
        if (stock == null || stock < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para " + productName);
        }
    }

    private Usuario resolveGuestUser(CrearPedidoRequest request) {
        String phone = clean(request.telefonoContacto());
        String name = firstNonBlank(request.nombreCliente(), "Cliente invitado");
        if (phone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ingresa un número de celular para coordinar tu pedido");
        }
        String normalizedPhone = phone.replaceAll("[^0-9+]", "");
        String email = clean(request.correoCliente());
        if (email == null) {
            String localPhone = normalizedPhone.replaceAll("[^0-9]", "");
            if (localPhone.isBlank()) {
                localPhone = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            }
            email = "guest-" + localPhone + "@parfum.local";
        } else {
            email = email.toLowerCase(Locale.ROOT);
        }
        Usuario guest = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (guest == null) {
            guest = new Usuario();
            guest.setNombre(name);
            guest.setEmail(email);
            guest.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            guest.setRol(Rol.USER);
            guest.setActivo(true);
        }
        guest.setNombre(name);
        guest.setTelefono(phone);
        return usuarioRepository.save(guest);
    }

    private String normalizePaymentMethod(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "YAPE" -> "YAPE";
            case "TRANSFERENCIA_BCP", "TRANSFERENCIA", "BCP" -> "TRANSFERENCIA_BCP";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Método de pago inválido. Solo se acepta Yape o transferencia BCP"
            );
        };
    }

    private String normalizeItemType(String raw, Long productoDecantId) {
        if (productoDecantId != null) return "DECANT";
        return raw != null && "DECANT".equalsIgnoreCase(raw.trim()) ? "DECANT" : "BOTELLA";
    }

    private EstadoPago paymentStatusOf(Pedido pedido) {
        return pedido.getEstadoPago() == null ? EstadoPago.PENDIENTE_VERIFICACION : pedido.getEstadoPago();
    }

    public PedidoResponse toResponse(Pedido pedido) {
        List<DetallePedidoResponse> details = pedido.getDetalles().stream().map(detail ->
                new DetallePedidoResponse(
                        detail.getProductoId(), detail.getPresentacionId(), detail.getProductoDecantId(),
                        detail.getTipoItem(), detail.getMililitros(), detail.getPresentacion(),
                        detail.getNombreProducto(), detail.getImagenUrl(), detail.getPrecioUnitario(),
                        detail.getCantidad(), detail.isRegalo())).toList();
        return new PedidoResponse(
                pedido.getId(), pedido.getUsuario().getId(), pedido.getUsuario().getNombre(),
                pedido.getUsuario().getEmail(), pedido.getClienteNombre(), pedido.getClienteCorreo(),
                pedido.getClienteTelefono(), pedido.getTotal(), pedido.getEstado().name(),
                pedido.getMetodoPago(), paymentStatusOf(pedido).name(), pedido.getNumeroOperacion(),
                pedido.getComprobanteUrl(), pedido.getObservacionPago(), pedido.getPagadoEn(),
                pedido.getDireccionEntrega(), pedido.getCreadoEn(), details);
    }

    private String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String firstNonBlank(String first, String fallback) {
        String cleaned = clean(first);
        return cleaned != null ? cleaned : clean(fallback);
    }

    private String normalize(String value) {
        if (value == null) return "";
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
