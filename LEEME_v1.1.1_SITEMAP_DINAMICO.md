# Parfum backend v1.1.1 - Sitemap dinámico

- Nuevo endpoint público: `GET /api/seo/sitemap.xml`.
- Lee en tiempo real los productos activos de Neon y genera sus URLs usando el `slug`.
- Incluye `lastmod` con la fecha de la última actualización del producto.
- Los productos creados desde el Panel Admin aparecen automáticamente, sin editar archivos HTML.
- El endpoint usa caché pública para reducir consultas repetidas.
- `/api/health` reporta versión `1.1.1`.

No se modifica el catálogo ni se reactivan sincronizaciones automáticas. Los datos editados desde Admin siguen siendo la fuente principal.
