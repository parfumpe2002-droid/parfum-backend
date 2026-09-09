package com.parfum.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {
    private final Cloudinary cloudinary;
    private final String cloudName;
    public ImagenController(Cloudinary cloudinary, @Value("${cloudinary.cloud-name}") String cloudName) {
        this.cloudinary = cloudinary;
        this.cloudName = cloudName;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (cloudName == null || cloudName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary aún no está configurado");
        }
        if (file.isEmpty() || file.getContentType() == null
                || !List.of("image/jpeg", "image/png", "image/webp")
                        .contains(file.getContentType().toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen JPG, PNG o WEBP");
        }
        if (file.getSize() > 8L * 1024L * 1024L) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La imagen no puede superar 8 MB");
        }
        byte[] bytes = file.getBytes();
        if (!looksLikeAllowedImage(bytes, file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contenido del archivo no corresponde a una imagen válida");
        }
        Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", "parfum/productos",
                "resource_type", "image",
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false));
        Object width = result.get("width");
        Object height = result.get("height");
        return Map.of(
                "url", String.valueOf(result.get("secure_url")),
                "publicId", String.valueOf(result.get("public_id")),
                "width", width == null ? 0 : width,
                "height", height == null ? 0 : height);
    }

    private boolean looksLikeAllowedImage(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length < 12 || contentType == null) return false;
        String type = contentType.toLowerCase(Locale.ROOT);
        if ("image/jpeg".equals(type)) {
            return (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        }
        if ("image/png".equals(type)) {
            int[] signature = {0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
            for (int i = 0; i < signature.length; i++) if ((bytes[i] & 0xff) != signature[i]) return false;
            return true;
        }
        if ("image/webp".equals(type)) {
            return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        }
        return false;
    }

    @DeleteMapping
    public Map<String, String> delete(@RequestParam String publicId) throws IOException {
        if (publicId == null || !publicId.startsWith("parfum/productos/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "publicId de producto inválido");
        }
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return Map.of("message", "Imagen eliminada");
    }
}
