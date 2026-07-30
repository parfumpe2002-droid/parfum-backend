-- PARFUM: convertir una cuenta registrada en administrador
--
-- PASO PREVIO:
-- 1. Registra una cuenta normalmente en:
--    https://parfum-store-app.netlify.app/registro.html
-- 2. Sustituye el correo de abajo por el correo registrado.
-- 3. Ejecuta este archivo en Neon > SQL Editor.

UPDATE usuarios
SET rol = 'ADMIN',
    activo = TRUE
WHERE LOWER(email) = LOWER('TU_CORREO_ADMIN@GMAIL.COM');

-- Verificación: debe mostrar rol ADMIN y activo true.
SELECT id, nombre, apellido, email, rol, activo, creado_en
FROM usuarios
WHERE LOWER(email) = LOWER('TU_CORREO_ADMIN@GMAIL.COM');

-- IMPORTANTE:
-- No insertes una contraseña en texto plano en PostgreSQL.
-- La cuenta debe registrarse primero para que Spring Boot guarde el hash BCrypt.
