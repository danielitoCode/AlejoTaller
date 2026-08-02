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

## Estado (balance Core 1 + soft-hold + SaleType)

| Política | Estado |
|----------|--------|
| Auth | Definida + implementada + tests |
| Sale | Definida + soft-hold + **SaleType en UI operador** |
| Warehouse | Definida + soft-hold + ajuste existence/reserved en operador |
| Product | Pendiente UI "disponible" en catálogo |

### Cobertura por app

| App | UNVERIFIED hold | Check available | VERIFIED stock | DELETED release | SaleType |
|-----|-----------------|-----------------|----------------|-----------------|----------|
| Web | sí | sí | n/a | n/a | n/a |
| Android cliente | sí | sí | n/a | n/a | n/a |
| alejotallerscan | n/a | n/a | sí | sí | **UI chips** |

### Pendiente próximo

1. Editor de importe para DISCOUNT en operador (hoy conserva amount del pedido)
2. Colección `stock_movements` + escritura en confirmación
3. UI catálogo: mostrar `available` (web + Android)
4. (Core 2) Appwrite Function atómica confirm+stock

## Regla de oro

Al añadir o cambiar comportamiento en un core, actualizar la política correspondiente
**antes o junto** con el código, y marcar el checklist de validación.
