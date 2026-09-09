BEGIN;

ALTER TABLE pedidos
    ADD COLUMN IF NOT EXISTS estado_pago VARCHAR(40),
    ADD COLUMN IF NOT EXISTS numero_operacion VARCHAR(80),
    ADD COLUMN IF NOT EXISTS comprobante_url VARCHAR(800),
    ADD COLUMN IF NOT EXISTS comprobante_public_id VARCHAR(300),
    ADD COLUMN IF NOT EXISTS observacion_pago VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pagado_en TIMESTAMPTZ;

UPDATE pedidos
SET estado_pago = 'PENDIENTE_VERIFICACION'
WHERE estado_pago IS NULL OR BTRIM(estado_pago) = '';

ALTER TABLE pedidos
    ALTER COLUMN estado_pago SET DEFAULT 'PENDIENTE_VERIFICACION';

CREATE INDEX IF NOT EXISTS idx_pedidos_estado_pago
    ON pedidos (estado_pago);

CREATE INDEX IF NOT EXISTS idx_pedidos_numero_operacion
    ON pedidos (numero_operacion);

COMMIT;

-- Comprobación:
-- SELECT id, metodo_pago, estado_pago, numero_operacion, comprobante_url
-- FROM pedidos ORDER BY id DESC LIMIT 20;
