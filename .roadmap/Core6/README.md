# Core 6 — Taller y Reservas · **AlejoTaller**

**Rama:** `Core6` · **Apertura:** 2026-09-13  
**Regla:** `Appointment ≠ Sale`

## Rol de este monorepo

Core 6 **producto de agenda/staff** vive en **dash**. Aquí:

1. **Frontera** — cliente/operador no confunden reserva con venta ni escriben finance/stock por citas.
2. **Solicitud B2C (opcional en release)** — web (y luego Android) pueden **pedir** cita `REQUESTED` si se decide incluirlo.
3. **Operador scan** — sin gestionar agenda completa (eso es panel).

## Plataforma

Migración desacoplada: [../PLATFORM_MIGRATION.md](../PLATFORM_MIGRATION.md) (Auth0 → R2 → Turso → cortar Appwrite).

## Canónico

[dash Core6](https://github.com/danielitoCode/dash_alejo_taller/blob/Core6/.roadmap/Core6/README.md)

## Checklists

- [AT_IMPLEMENTATION_CHECKLIST.md](./AT_IMPLEMENTATION_CHECKLIST.md)
- [MVP_CORE6_STATUS.md](./MVP_CORE6_STATUS.md)
- [CORE6_UNIFIED_CHECKLIST.md](./CORE6_UNIFIED_CHECKLIST.md)
