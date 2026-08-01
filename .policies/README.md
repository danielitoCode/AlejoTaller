# Políticas de la aplicación (AlejoTaller)

Este directorio documenta las **políticas de negocio y de sesión** por feature.
Sirven para validar los cores (web, Android, operador) y mantener paridad.

## Estructura

```
.policies/
  README.md
  auth/
    AUTH_POLICY.md          # Clasificación de perfil, visitante vs autenticado
  product/                  # (pendiente) stock, existence, lectura cliente
  sale/                     # (pendiente) UNVERIFIED → VERIFIED, deducción stock en operador
  warehouse/                # (pendiente) stock_movements, alineación contable
```

## Regla de oro

Al añadir o cambiar comportamiento en un core, actualizar la política correspondiente
**antes o junto** con el código, y marcar el checklist de validación.
