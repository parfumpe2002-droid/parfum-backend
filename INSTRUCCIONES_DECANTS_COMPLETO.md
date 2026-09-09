# Parfum — actualización completa de decants

## Orden de instalación

1. En **Neon → SQL Editor**, ejecuta `migracion_decants_completa_neon.sql`.
2. Reemplaza el backend y haz push a GitHub. Espera que Render muestre `Deploy succeeded`.
3. Reemplaza el frontend y haz push. Espera el despliegue de Netlify.
4. En Parfum pulsa **Actualizar** cuando aparezca el aviso de nueva versión, o usa `Ctrl + F5`.
5. Inicia sesión como administrador.

## Configurar precios y stock

- En **Administración → Productos → Editar**, cada perfume muestra dos bloques:
  - botellas completas;
  - decants de 3 ml, 5 ml y 10 ml.
- Coloca el precio y stock de cada decant y guarda el producto.
- El botón **Pedir en decant** se muestra en Inicio, Productos, Ofertas y en el detalle.

## Administrar envases

En **Administración → Decants** puedes:

- editar la imagen de 3 ml, 5 ml o 10 ml;
- agregar varias presentaciones de 10 ml;
- cambiar nombre, mililitros, descripción e imagen;
- ocultar o eliminar un envase.

Cuando creas un envase nuevo, aparecerá en el editor de cada perfume para asignarle precio y stock.

## Promoción

Al tener tres o más unidades tipo DECANT en el carrito, aparece el selector de regalo. El cliente elige un perfume de categoría **Árabe** que tenga stock en decant de 3 ml. El detalle del regalo se guarda con precio cero y aparece en el pedido del administrador.

## MongoDB Atlas

No se ejecuta migración manual. El backend crea y usa automáticamente:

- `carrito_variantes`
- `favoritos_variantes`

Estas colecciones distinguen botella, mililitros y envase de decant, evitando que una presentación reemplace a otra.
