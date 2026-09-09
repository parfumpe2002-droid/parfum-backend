# Parfum Backend v1.1.0 — Seguridad y protección de datos de Admin

Esta versión parte del ZIP actual entregado el 08/09/2026 y está preparada para conservar los cambios realizados desde el Panel Admin en Neon.

## Cambios principales

- `APP_CATALOG_SYNC=false` por defecto: un deploy ya no reimporta `perfumes.json` ni pisa precios, stock o imágenes cargadas desde Admin.
- CORS incluye `https://parfum.com.pe`, `https://www.parfum.com.pe` y el subdominio actual de Netlify.
- Rate limiting por IP en login, registro, contacto, actividad, creación de pedidos y carga/reemplazo de comprobantes.
- Corrección del checkout invitado: el correo escrito en checkout nunca se usa para localizar/modificar una cuenta registrada.
- Comprobantes: máximo 5 MB, solo JPG/PNG/WEBP, validación de firma real del archivo y Cloudinary limitado a `parfum/comprobantes/`.
- Imágenes de productos: máximo 8 MB, JPG/PNG/WEBP y validación de firma real; eliminación limitada a `parfum/productos/`.
- Contraseñas nuevas: mínimo 10 caracteres.
- Tokens: 7 días por defecto; al cambiar contraseña se revocan todas las sesiones existentes.
- Pedidos: máximo 50 ítems por solicitud.
- `/api/health` reporta versión `1.1.0`.

## Variables recomendadas en Render

```text
CORS_ORIGINS=https://parfum.com.pe,https://www.parfum.com.pe,https://parfum-store-app.netlify.app
APP_TOKEN_DAYS=7
APP_CATALOG_SYNC=false
APP_SEED_DATA=false
```

No borrar Neon ni Cloudinary. No activar `APP_CATALOG_SYNC=true` salvo que se quiera hacer una reparación controlada del catálogo.
