# Parfum Backend

Backend Spring Boot del e-commerce Parfum.

## Integraciones

- Neon/PostgreSQL: usuarios, productos, pedidos y tokens de sesión.
- MongoDB Atlas: carrito, favoritos, historial, reseñas, contactos y actividad del sitio.
- Cloudinary: imágenes definitivas de productos.
- Render: despliegue del backend.
- Netlify: frontend estático.

## Panel administrador

El panel permite:

- Crear y editar productos, incluyendo categorías nuevas como `Árabe`.
- Cambiar nombre, marca, descripción, notas, precio, stock, estado y destacado.
- Subir o reemplazar imágenes en Cloudinary.
- Gestionar pedidos, usuarios y roles.
- Leer, responder, clasificar y eliminar mensajes de contacto.
- Consultar visitas, visitantes únicos y acciones recientes.

La actividad se almacena en la colección MongoDB `actividad_sitio`. La colección se crea automáticamente cuando el frontend registra la primera visita.

## Variables de Render

Consulta `.env.example`. Las principales son:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_DATA_MONGODB_URI
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
CORS_ORIGINS
APP_SEED_DATA
```

Variables opcionales para crear o promover un administrador al iniciar:

```text
APP_ADMIN_EMAIL
APP_ADMIN_PASSWORD
APP_ADMIN_NAME
```

Si el correo ya existe, el backend lo promueve a `ADMIN`. Si no existe, requiere `APP_ADMIN_PASSWORD` para crear la cuenta.

## CORS

El backend autoriza el sitio:

```text
https://parfum-store-app.netlify.app
```

También admite vistas previas `https://*.netlify.app` y entornos locales. Las solicitudes `OPTIONS` están permitidas para registro, login y CRUD.

## Health check

```text
GET /api/health
```

## Endpoints administrativos principales

```text
GET    /api/admin/resumen
GET    /api/admin/productos
GET    /api/admin/categorias
GET    /api/admin/usuarios
GET    /api/admin/actividad
POST   /api/productos
PUT    /api/productos/{id}
DELETE /api/productos/{id}
POST   /api/imagenes/upload
GET    /api/contactos
PATCH  /api/contactos/{id}/estado
DELETE /api/contactos/{id}
```
