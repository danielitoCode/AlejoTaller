# Core 3 — Política de anulación y corrección de entradas

**Fecha:** 2026-08-29  
**Estado:** aceptada para implementación B3 en back-office.

## 1. Alcance

La anulación pertenece al back-office y queda restringida a **owner/admin**. El cliente B2C y el operador no anulan ni corrigen entradas.

## 2. Inmutabilidad y trazabilidad

- Una entrada confirmada no se elimina físicamente.
- No se edita retrospectivamente la entrada original.
- El contrato B3 usa `purchase_entry.status`: `ACTIVE` → `CANCELLED`.
- Entradas legacy sin `status` se interpretan como `ACTIVE` hasta que sean anuladas.
- La reversión conserva `entry_id`.
- Se usa el tipo existente `ajuste` con `reason = purchase_entry_reversal`.

## 3. Integridad de inventario

Para cada línea:

`new_existence = existence - quantity_to_reverse`

Debe cumplirse:

`new_existence >= reserved`

Si una línea incumple la condición, se rechaza la operación completa.

## 4. Atomicidad — Appwrite Client SDK

B3 se ejecuta desde el **Client SDK TypeScript del back-office**, sin Function serverless adicional.

La implementación usa `Databases.createTransaction()`, propaga `transactionId` a las lecturas/escrituras y finaliza con `updateTransaction({ commit: true })` o rollback ante error.

Orden:

1. Leer entrada dentro de la transacción.
2. Verificar `ACTIVE`.
3. Leer líneas y productos.
4. Validar todas las reversas.
5. Decrementar `existence`.
6. Crear movements compensatorios.
7. Marcar `CANCELLED`.
8. Commit.

Si falla cualquier paso, ningún cambio parcial debe quedar persistido.

El mismo runner se reutiliza para el registro de entradas para evitar el anterior `soft-fail` de movements.

## 5. Idempotencia y concurrencia

Una entrada `CANCELLED` no puede generar otra reversión. Las operaciones se ejecutan dentro de una misma transacción para permitir que Appwrite detecte conflictos concurrentes al commit.

## 6. Alcance B3

**B3.1:** anulación completa. El caso de uso transaccional está implementado en el back-office.

**B3.2:** corrección parcial queda separada; no se editará retrospectivamente la entrada original.

## 7. `last_unit_cost`

La anulación no modifica `last_unit_cost`. No se establece a cero ni se inventa un costo anterior. El operador continúa usando el valor existente para COGS.

## 8. UX y autorización

La acción de anulación solo aparece para `owner/admin`, requiere confirmación, explica el impacto en stock y queda inactiva una vez cancelada.

## 9. Requisito de schema

Para habilitar la operación debe existir en Appwrite `purchase_entry.status` como atributo compatible con `ACTIVE` / `CANCELLED`. La creación/modificación del schema es una operación administrativa separada del Client SDK y de la transacción B3.

## 10. Frontera AlejoTaller

Este repositorio no registra compras ni implementa la UI/caso de uso de anulación. Solo debe preservar `existence`, `reserved`, COGS y la frontera de permisos definida por esta política.
