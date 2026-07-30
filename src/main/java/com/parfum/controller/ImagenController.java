package com.parfum.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
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
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen válida");
        }
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
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

    @DeleteMapping
    public Map<String, String> delete(@RequestParam String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "publicId obligatorio");
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return Map.of("message", "Imagen eliminada");
    }
}
