# Políticas de la aplicación (AlejoTaller)

Este directorio documenta las **políticas de negocio y de sesión** por feature.
Sirven para validar los cores (web, Android, operador) y mantener paridad.

## Estructura

```
.policies/
  README.md
  auth/
    AUTH_POLICY.md
  sale/
    SALE_POLICY.md
  warehouse/
    WAREHOUSE_POLICY.md
```

## Estado (balance Core 1) — 2026-08-07

| Política | Estado |
|----------|--------|
| Auth | Definida + implementada + tests |
| Sale | Definida + soft-hold + SaleType + importe DISCOUNT + tests parciales |
| Warehouse | Definida + soft-hold + existence/reserved + tests parciales |
| Product UI available | **Cerrada** — badges en catálogo/detalle (web + Android) |

### Cobertura por app

| App | Hold UNVERIFIED | Check available | Stock VERIFIED/DELETED | SaleType + amount | Badge available UI |
|-----|-----------------|-----------------|------------------------|-------------------|--------------------|
| Web | sí + test | sí + test | n/a | n/a | sí (ProductCard + Detail) |
| Android cliente | sí + test | sí + test | n/a | n/a | sí (ProductItem + Detail) |
| alejotallerscan | n/a | n/a | sí | **UI completa** | n/a (operador) |

### Tests parciales Core 1 (sale / warehouse)

| Superficie | Suite | Qué cubre |
|------------|-------|-----------|
| Web | `check-a-product-existence.case.use.test.ts` | available = existence − reserved |
| Web | `RegisterNewSaleCaseUse.soft-hold.test.ts` | reserved += qty, insuficiencia, telegram best-effort |
| Android | `SoftHoldCaseUseTest` | availableStock, CheckAProductExistence, ApplySoftHold + idempotencia |

### Pendiente (fuera Core 1 / Core 2)

1. Colección `stock_movements` + escritura en confirmación
2. Appwrite Function atómica confirm+stock
3. Tests automatizados del lado operador (confirm/reject stock)
4. Reportes / multi-almacén / devoluciones formales

## Regla de oro

Al añadir o cambiar comportamiento en un core, actualizar la política correspondiente
**antes o junto** con el código, y marcar el checklist de validación.
