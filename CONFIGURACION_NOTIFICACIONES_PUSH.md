NOTIFICACIONES WEB PUSH - PARFUM
================================

Objetivo
- Avisar a los administradores cuando entra un pedido nuevo.
- Avisar al cliente cuando cambia el estado del pedido o la verificación del pago.
- Al tocar el aviso, abrir directamente Pedidos del panel admin o Mis pedidos.

Variables obligatorias en Render
- VAPID_PUBLIC_KEY
- VAPID_PRIVATE_KEY
- VAPID_SUBJECT=https://parfum.com.pe

Seguridad
- La clave privada VAPID no debe guardarse en GitHub.
- Configúrala únicamente como variable secreta del servicio backend en Render.

Flujo de administrador
1. Inicia sesión con una cuenta ADMIN.
2. Abre Administración > Pedidos.
3. Pulsa Activar avisos y concede el permiso del navegador.
4. El dispositivo se registra en /api/notificaciones/suscribir.
5. Cuando entra una compra nueva, Parfum envía un Web Push.
6. Al tocar el aviso se abre /admin.html#orders.

Flujo de cliente
1. Inicia sesión y abre Mis pedidos.
2. Pulsa Activar avisos.
3. Cuando administración cambia el estado o la verificación del pago, recibe un Web Push.
4. Al tocar el aviso se abre /pedidos.html.

Compatibilidad
- Chrome/Android y navegadores Chromium compatibles.
- Firefox.
- Safari/iPhone cuando la web está instalada en pantalla de inicio y el sistema permite Web Push.
