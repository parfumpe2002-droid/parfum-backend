-- Parfum: presentaciones por mililitros, precio y stock
-- Ejecutar una sola vez en Neon > SQL Editor.
-- Los precios y stocks quedan en 0 para que el administrador los complete.

BEGIN;

CREATE TABLE IF NOT EXISTS producto_presentaciones (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    mililitros INTEGER NOT NULL CHECK (mililitros > 0),
    precio NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (precio >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    orden_visual INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_producto_presentacion_ml UNIQUE (producto_id, mililitros)
);

CREATE INDEX IF NOT EXISTS idx_presentaciones_producto ON producto_presentaciones(producto_id);

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'erba-pura'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'erba-pura'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'torino-21'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'torino-21'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'invictus-victory-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'invictus-victory-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'stronger-with-you-intensely'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'stronger-with-you-intensely'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 150, 0, 0, TRUE, 2
FROM productos WHERE slug = 'stronger-with-you-intensely'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'dior-homme-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'dior-homme-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 150, 0, 0, TRUE, 2
FROM productos WHERE slug = 'dior-homme-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'armani-code-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 1
FROM productos WHERE slug = 'armani-code-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 2
FROM productos WHERE slug = 'armani-code-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'one-million-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'one-million-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'one-million-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 30, 0, 0, TRUE, 0
FROM productos WHERE slug = 'acqua-di-gio-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 1
FROM productos WHERE slug = 'acqua-di-gio-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 2
FROM productos WHERE slug = 'acqua-di-gio-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 3
FROM productos WHERE slug = 'acqua-di-gio-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 4
FROM productos WHERE slug = 'acqua-di-gio-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'y-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 60, 0, 0, TRUE, 1
FROM productos WHERE slug = 'y-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'y-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 3
FROM productos WHERE slug = 'y-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'the-most-wanted-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'the-most-wanted-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 30, 0, 0, TRUE, 0
FROM productos WHERE slug = 'sauvage-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 60, 0, 0, TRUE, 1
FROM productos WHERE slug = 'sauvage-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'sauvage-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 3
FROM productos WHERE slug = 'sauvage-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 0
FROM productos WHERE slug = 'le-male-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 1
FROM productos WHERE slug = 'le-male-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'le-male-elixir'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 0
FROM productos WHERE slug = 'le-beau-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 1
FROM productos WHERE slug = 'le-beau-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'eros-flame-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'eros-flame-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'eros-flame-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 60, 0, 0, TRUE, 0
FROM productos WHERE slug = 'french-riviera'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 120, 0, 0, TRUE, 1
FROM productos WHERE slug = 'french-riviera'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'invictus-victory-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'invictus-victory-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'invictus-victory-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'scandal-pour-homme-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'scandal-pour-homme-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 150, 0, 0, TRUE, 2
FROM productos WHERE slug = 'scandal-pour-homme-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-coral-fantasy'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-coral-fantasy'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-green-stravaganza'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'valentino-uomo-born-in-roma-green-stravaganza'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'one-million-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'one-million-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'one-million-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'prada-luna-rossa-ocean-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'prada-luna-rossa-ocean-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 150, 0, 0, TRUE, 2
FROM productos WHERE slug = 'prada-luna-rossa-ocean-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'million-gold'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'million-gold'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'million-gold'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'acqua-di-gio-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 1
FROM productos WHERE slug = 'acqua-di-gio-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 2
FROM productos WHERE slug = 'acqua-di-gio-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 30, 0, 0, TRUE, 0
FROM productos WHERE slug = 'acqua-di-gio-profondo-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 1
FROM productos WHERE slug = 'acqua-di-gio-profondo-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'acqua-di-gio-profondo-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 30, 0, 0, TRUE, 0
FROM productos WHERE slug = 'acqua-di-gio-profondo-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 1
FROM productos WHERE slug = 'acqua-di-gio-profondo-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'acqua-di-gio-profondo-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 3
FROM productos WHERE slug = 'acqua-di-gio-profondo-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'myslf-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 60, 0, 0, TRUE, 1
FROM productos WHERE slug = 'myslf-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'myslf-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 150, 0, 0, TRUE, 3
FROM productos WHERE slug = 'myslf-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'the-most-wanted-eau-de-parfum-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'the-most-wanted-eau-de-parfum-intense'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 30, 0, 0, TRUE, 0
FROM productos WHERE slug = 'sauvage-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 60, 0, 0, TRUE, 1
FROM productos WHERE slug = 'sauvage-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'sauvage-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 3
FROM productos WHERE slug = 'sauvage-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 60, 0, 0, TRUE, 0
FROM productos WHERE slug = 'sauvage-eau-forte'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'sauvage-eau-forte'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 0
FROM productos WHERE slug = 'le-male-elixir-absolu'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 1
FROM productos WHERE slug = 'le-male-elixir-absolu'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'le-male-elixir-absolu'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 0
FROM productos WHERE slug = 'le-male-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 1
FROM productos WHERE slug = 'le-male-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'le-male-le-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 0
FROM productos WHERE slug = 'ultra-male'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 1
FROM productos WHERE slug = 'ultra-male'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'le-male-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 1
FROM productos WHERE slug = 'le-male-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 2
FROM productos WHERE slug = 'le-male-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 3
FROM productos WHERE slug = 'le-male-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = 'eros-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = 'eros-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = 'eros-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'polo-red-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 1
FROM productos WHERE slug = 'polo-red-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 2
FROM productos WHERE slug = 'polo-red-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'polo-blue-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 1
FROM productos WHERE slug = 'polo-blue-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 2
FROM productos WHERE slug = 'polo-blue-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 0
FROM productos WHERE slug = '212-vip-black-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 1
FROM productos WHERE slug = '212-vip-black-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 2
FROM productos WHERE slug = '212-vip-black-eau-de-parfum'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 30, 0, 0, TRUE, 0
FROM productos WHERE slug = 'boss-bottled-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 50, 0, 0, TRUE, 1
FROM productos WHERE slug = 'boss-bottled-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 2
FROM productos WHERE slug = 'boss-bottled-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 200, 0, 0, TRUE, 3
FROM productos WHERE slug = 'boss-bottled-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 40, 0, 0, TRUE, 0
FROM productos WHERE slug = 'hugo-just-different-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 1
FROM productos WHERE slug = 'hugo-just-different-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 125, 0, 0, TRUE, 2
FROM productos WHERE slug = 'hugo-just-different-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;

INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 75, 0, 0, TRUE, 0
FROM productos WHERE slug = 'blue-jeans-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;


INSERT INTO producto_presentaciones (producto_id, mililitros, precio, stock, activo, orden_visual)
SELECT id, 100, 0, 0, TRUE, 0
FROM productos WHERE slug = 'nautica-voyage-eau-de-toilette'
ON CONFLICT (producto_id, mililitros) DO NOTHING;



ALTER TABLE detalle_pedido ADD COLUMN IF NOT EXISTS presentacion_id BIGINT;
ALTER TABLE detalle_pedido ADD COLUMN IF NOT EXISTS mililitros INTEGER;
ALTER TABLE detalle_pedido ADD COLUMN IF NOT EXISTS presentacion VARCHAR(60);

COMMIT;

-- Verificación:
SELECT p.nombre, pp.mililitros, pp.precio, pp.stock, pp.activo
FROM producto_presentaciones pp
JOIN productos p ON p.id = pp.producto_id
ORDER BY p.nombre, pp.orden_visual, pp.mililitros;
