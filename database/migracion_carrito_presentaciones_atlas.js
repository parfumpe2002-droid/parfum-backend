// Parfum: permite guardar el mismo perfume en distintos mililitros dentro del carrito.
// Ejecutar en mongosh conectado al clúster Atlas.

const dbName = "Parfum"; // Mantiene la P mayúscula usada por tu backend actual.
const parfum = db.getSiblingDB(dbName);

if (!parfum.getCollectionNames().includes("carrito")) {
  parfum.createCollection("carrito");
}

const carrito = parfum.getCollection("carrito");
for (const index of carrito.getIndexes()) {
  const fields = Object.keys(index.key || {});
  const isOldUnique = index.unique === true
    && fields.includes("usuarioId")
    && fields.includes("productoId")
    && !fields.includes("presentacionId")
    && index.name !== "_id_";
  if (isOldUnique) {
    print(`Eliminando índice anterior: ${index.name}`);
    carrito.dropIndex(index.name);
  }
}

carrito.createIndex(
  {usuarioId: 1, productoId: 1, presentacionId: 1},
  {name: "carrito_usuario_producto_presentacion", unique: true}
);
carrito.createIndex({usuarioId: 1, actualizadoEn: -1}, {name: "carrito_usuario_fecha"});

print("Migración completada en la base Parfum.");
printjson(carrito.getIndexes());
