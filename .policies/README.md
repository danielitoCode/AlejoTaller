# Políticas de la aplicación (AlejoTaller)

Este directorio documenta las **políticas de negocio y de sesión** por feature.
Sirven para validar los cores (web, Android, operador) y mantener paridad.

## Estructura

```
.policies/
  README.md
  auth/
    AUTH_POLICY.md          # Clasificación de perfil, visitante vs autenticado
  sale/
    SALE_POLICY.md          # Ciclo UNVERIFIED→VERIFIED/DELETED, tipos NORMAL|DISCOUNT|GIFT
  warehouse/
    WAREHOUSE_POLICY.md     # Baja de stock solo en VERIFIED (cantidad de la línea)
  product/                  # (pendiente) catálogo, existence visible, visitor vs auth
```

## Estado

| Política | Estado Core 1 |
|----------|---------------|
| Auth | Definida + implementada + tests |
| Sale | Definida (modelos a alinear / implementar tipo + confirmación) |
| Warehouse | Definida (movimientos en confirmación pendiente de cablear) |
| Product | Pendiente |

## Regla de oro

Al añadir o cambiar comportamiento en un core, actualizar la política correspondiente
**antes o junto** con el código, y marcar el checklist de validación.
