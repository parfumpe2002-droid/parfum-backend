-- PARFUM: migración completa para decants, pedidos de invitado y regalo árabe.
-- Ejecutar UNA VEZ en Neon -> SQL Editor.

BEGIN;

ALTER TABLE IF EXISTS productos
    ADD COLUMN IF NOT EXISTS decant_disponible BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS decant_envases (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    mililitros INTEGER NOT NULL CHECK (mililitros > 0),
    descripcion VARCHAR(400),
    imagen_url VARCHAR(600),
    imagen_public_id VARCHAR(300),
    fallback_image VARCHAR(300),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    orden_visual INTEGER NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS producto_decants (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    envase_id BIGINT NOT NULL REFERENCES decant_envases(id),
    precio NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (precio >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    orden_visual INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_producto_decant_envase UNIQUE (producto_id, envase_id)
);

CREATE INDEX IF NOT EXISTS idx_producto_decants_producto
    ON producto_decants(producto_id);
CREATE INDEX IF NOT EXISTS idx_producto_decants_envase
    ON producto_decants(envase_id);

ALTER TABLE IF EXISTS detalle_pedido
    ADD COLUMN IF NOT EXISTS producto_decant_id BIGINT,
    ADD COLUMN IF NOT EXISTS tipo_item VARCHAR(20) DEFAULT 'BOTELLA',
    ADD COLUMN IF NOT EXISTS regalo BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE IF EXISTS pedidos
    ADD COLUMN IF NOT EXISTS cliente_nombre VARCHAR(100),
    ADD COLUMN IF NOT EXISTS cliente_correo VARCHAR(160),
    ADD COLUMN IF NOT EXISTS cliente_telefono VARCHAR(30);

INSERT INTO decant_envases
    (nombre, mililitros, descripcion, fallback_image, activo, orden_visual)
SELECT 'Vidrio negro 3 ml', 3,
       'Frasco atomizador de vidrio de 3 ml.',
       'imagen/decants/decant-3ml.png', TRUE, 10
WHERE NOT EXISTS (
    SELECT 1 FROM decant_envases WHERE LOWER(nombre) = LOWER('Vidrio negro 3 ml')
);

INSERT INTO decant_envases
    (nombre, mililitros, descripcion, fallback_image, activo, orden_visual)
SELECT 'Vidrio negro 5 ml', 5,
       'Frasco atomizador de vidrio de 5 ml.',
       'imagen/decants/decant-5ml.png', TRUE, 20
WHERE NOT EXISTS (
    SELECT 1 FROM decant_envases WHERE LOWER(nombre) = LOWER('Vidrio negro 5 ml')
);

INSERT INTO decant_envases
    (nombre, mililitros, descripcion, fallback_image, activo, orden_visual)
SELECT 'Premium degradé 10 ml', 10,
       'Presentación premium degradada de 10 ml. Se pueden agregar más diseños desde el panel.',
       'imagen/decants/decant-10ml-premium.png', TRUE, 30
WHERE NOT EXISTS (
    SELECT 1 FROM decant_envases WHERE LOWER(nombre) = LOWER('Premium degradé 10 ml')
);

-- Crea las tres opciones de decant para todos los perfumes ya existentes.
-- Comienzan en precio S/ 0 y stock 0 para que el administrador coloque los valores reales.
INSERT INTO producto_decants
    (producto_id, envase_id, precio, stock, activo, orden_visual)
SELECT p.id, e.id, 0, 0, TRUE, e.orden_visual
FROM productos p
CROSS JOIN decant_envases e
WHERE e.activo = TRUE
  AND NOT EXISTS (
      SELECT 1
      FROM producto_decants pd
      WHERE pd.producto_id = p.id AND pd.envase_id = e.id
  );

UPDATE productos SET decant_disponible = TRUE WHERE decant_disponible IS DISTINCT FROM TRUE;
UPDATE detalle_pedido SET tipo_item = 'BOTELLA' WHERE tipo_item IS NULL OR tipo_item = '';

COMMIT;
