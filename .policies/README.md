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
  product/                  # (pendiente) catálogo UI available
```

## Estado (balance Core 1)

| Política | Estado |
|----------|--------|
| Auth | Definida + implementada + tests |
| Sale | Definida + soft-hold + SaleType + importe DISCOUNT + **tests parciales** |
| Warehouse | Definida + soft-hold + existence/reserved + **tests parciales** |
| Product | Pendiente UI "disponible" en catálogo |

### Cobertura por app

| App | Hold UNVERIFIED | Check available | Stock VERIFIED/DELETED | SaleType + amount |
|-----|-----------------|-----------------|------------------------|-------------------|
| Web | sí + test | sí + test | n/a | n/a |
| Android cliente | sí + test | sí + test | n/a | n/a |
| alejotallerscan | n/a | n/a | sí | **UI completa** |

### Tests parciales Core 1 (sale / warehouse)

| Superficie | Suite | Qué cubre |
|------------|-------|-----------|
| Web | `check-a-product-existence.case.use.test.ts` | available = existence − reserved |
| Web | `RegisterNewSaleCaseUse.soft-hold.test.ts` | reserved += qty, insuficiencia, telegram best-effort |
| Android | `SoftHoldCaseUseTest` | availableStock, CheckAProductExistence, ApplySoftHold + idempotencia |

### Pendiente (fuera Core 1 estricto / siguiente)

1. Colección `stock_movements` + escritura en confirmación (Core 2)
2. UI catálogo: mostrar `available` (web + Android)
3. (Core 2) Appwrite Function atómica confirm+stock
4. Tests automatizados del lado operador (confirm/reject stock)

## Regla de oro

Al añadir o cambiar comportamiento en un core, actualizar la política correspondiente
**antes o junto** con el código, y marcar el checklist de validación.
