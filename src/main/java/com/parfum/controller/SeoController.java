package com.parfum.controller;

import com.parfum.jpa.repository.ProductoRepository;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/seo")
public class SeoController {
    private static final String SITE = "https://parfum.com.pe";
    private final ProductoRepository productos;

    public SeoController(ProductoRepository productos) {
        this.productos = productos;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder(16_384);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Páginas principales. Se mantienen sin lastmod para no inventar fechas.
        appendUrl(xml, SITE + "/", null);
        appendUrl(xml, SITE + "/productos.html", null);
        appendUrl(xml, SITE + "/contacto.html", null);
        appendUrl(xml, SITE + "/nosotros.html", null);

        // LinkedHashSet evita URLs duplicadas si alguna migración dejó slugs repetidos.
        Set<String> seen = new LinkedHashSet<>();
        for (ProductoRepository.SitemapRow row : productos.findSitemapRows()) {
            String slug = row.getSlug() == null ? "" : row.getSlug().trim();
            if (slug.isBlank() || !seen.add(slug.toLowerCase(java.util.Locale.ROOT))) continue;
            String encodedSlug = UriUtils.encodePathSegment(slug, StandardCharsets.UTF_8);
            String lastmod = row.getActualizadoEn() == null
                    ? null
                    : row.getActualizadoEn().atZone(ZoneOffset.UTC).toLocalDate().toString();
            appendUrl(xml, SITE + "/decants/" + encodedSlug + "/", lastmod);
        }

        xml.append("</urlset>\n");
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300, s-maxage=3600, stale-while-revalidate=86400")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.toString());
    }

    private static void appendUrl(StringBuilder xml, String loc, String lastmod) {
        xml.append("  <url><loc>").append(escapeXml(loc)).append("</loc>");
        if (lastmod != null && !lastmod.isBlank()) {
            xml.append("<lastmod>").append(lastmod).append("</lastmod>");
        }
        xml.append("</url>\n");
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
