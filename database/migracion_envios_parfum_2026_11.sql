-- Ejecutar únicamente si se desactiva spring.jpa.hibernate.ddl-auto=update.
-- No modifica pedidos existentes ni precios cobrados.
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS metodo_envio VARCHAR(20);
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS nombre_destinatario VARCHAR(100);
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS agencia_envio VARCHAR(120);
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS documento_destinatario VARCHAR(30);
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS envio_gratis BOOLEAN;
