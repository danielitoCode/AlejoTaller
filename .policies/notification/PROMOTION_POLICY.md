# Promociones — Política B (clientes)

Alineado con dash_alejo_taller (2026-08-13).

- `product.price` = precio de **lista**
- `effectivePrice` = promo `product_discount` activa o lista
- Banner sin `productId` permitido
- Realtime: **Appwrite** `databases.{db}.collections.promotions.documents`
- Operador: no reinterpreta precio (amount congelado en Sale)
