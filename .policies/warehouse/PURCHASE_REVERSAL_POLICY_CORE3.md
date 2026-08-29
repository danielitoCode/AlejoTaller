# Core 3 — Política de anulación y corrección de entradas

**Fecha:** 2026-08-29  
**Estado:** propuesta aceptada para diseño B3; no implica implementación todavía.

## 1. Alcance

Esta política define cómo se revertirán entradas de compras/abastecimiento que ya hayan afectado el inventario.

La anulación pertenece al back-office y queda restringida a **owner/admin**. El cliente B2C y el operador no anulan ni corrigen entradas.

## 2. Inmutabilidad y trazabilidad

- Una entrada confirmada **no se elimina físicamente**.
- Una entrada confirmada no se edita línea a línea para alterar retrospectivamente el movimiento original.
- La anulación cambia el estado de la entrada a `CANCELLED` (o equivalente definido por el contrato B3).
- La reversión de stock se registra mediante **movimientos compensatorios**, conservando la referencia `entry_id` de la entrada original.
- El historial debe permitir reconstruir la entrada original y su reversión.

## 3. Regla de integridad de inventario

Toda anulación debe preservar la invariante Core 1:

`existence >= reserved`

Para cada línea afectada, antes de modificar el stock debe comprobarse que:

`existence - quantity_to_reverse >= reserved`

Si una sola línea incumple la condición, **se rechaza la operación completa**. No se permite una anulación parcial accidental.

## 4. Atomicidad

La anulación debe ejecutarse como una única operación atómica:

1. Verificar que la entrada está activa/no anulada.
2. Obtener todas sus líneas y el stock actual.
3. Validar `existence >= reserved` después de la reversión para todas las líneas.
4. Aplicar los decrementos de stock.
5. Crear los movimientos compensatorios.
6. Marcar la entrada como anulada.
7. Confirmar la transacción.

Si cualquier paso falla, **ningún cambio de stock, movement o estado debe quedar persistido**.

## 5. Idempotencia

Una entrada ya anulada no puede volver a generar movimientos compensatorios ni volver a decrementar stock.

La primera transición válida es:

`ACTIVE → CANCELLED`

Una solicitud posterior sobre una entrada `CANCELLED` debe ser rechazada o tratada como operación ya completada, sin efectos adicionales.

## 6. Anulación completa vs corrección parcial

### B3.1 — Anulación completa

Es el primer alcance de implementación. Revierte todas las líneas de la entrada original.

### B3.2 — Corrección parcial

Queda separada de B3.1. No se debe editar retrospectivamente la entrada original; preferentemente se modelará como un ajuste/movimiento compensatorio auditable.

## 7. `last_unit_cost`

La anulación **no debe establecer `last_unit_cost` a cero ni inventar un costo nuevo**.

Antes de implementar cualquier reversión que afecte este campo se debe auditar el contrato Core 2 y determinar cómo recuperar, si corresponde, el último costo válido anterior.

La semántica existente de COGS (`last_unit_cost × qty` al `VERIFIED`) no debe romperse.

## 8. UX y autorización

La acción de anulación:

- solo aparece para roles `owner/admin`;
- requiere confirmación explícita;
- muestra que revertirá el stock;
- informa claramente cuando no puede ejecutarse por unidades reservadas;
- desaparece o queda inactiva una vez anulada la entrada.

## 9. Regla de diseño

B3 debe reutilizar el modelo de inventario existente. No se debe introducir un segundo almacén lógico ni una segunda fuente de verdad para `existence`, `reserved`, `available` o COGS.

**Antes de implementar:** auditar el contrato real de Core 2 y los casos de uso/repositorios de stock, movements, entradas y `last_unit_cost` en ambos repositorios.