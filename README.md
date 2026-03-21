# Taller Alejo

## Descripción
Taller Alejo es una aplicación Android para la gestión de un taller. El objetivo es centralizar la administración de productos, categorías y ventas, con sincronización en la nube y soporte offline-first para operar sin conexión y luego sincronizar los datos.

## Objetivos del proyecto
- **Inventario y catálogo**: administrar productos y sus categorías.
- **Ventas**: registrar ventas, consultar historial y sincronizar con la nube.
- **Offline-first**: permitir operar con base local y sincronizar cuando haya conexión.
- **Arquitectura modular**: separar claramente las capas de datos, dominio y presentación.

## Qué estamos haciendo (estado actual)
### Arquitectura
- Capas **data**, **domain** y **presentation** con inyección de dependencias usando **Koin**.
- Base de datos local con **Room**.
- Sincronización remota usando **Appwrite**.

### Módulos funcionales
- **Productos**: lectura remota de productos y mapeo DTO ↔ dominio.
- **Categorías**: infraestructura para gestionar categorías.
- **Ventas**: flujo completo con DAO, mapeadores, repositorio offline-first, casos de uso y ViewModel.

## Qué nos proponemos (roadmap)
1. **Consolidar flujo de ventas**: alta, edición, eliminación y consulta detallada.
2. **Sincronización bidireccional**: subir cambios locales pendientes y resolver conflictos.
3. **Mejorar UX**: estados de carga/errores, mensajes al usuario y pantalla de detalle.
4. **Reportes básicos**: métricas simples como ventas por fecha y por producto.
5. **Hardening**: validaciones, manejo de errores y pruebas automatizadas.

## Qué nos falta
- **CRUD completo de productos y categorías** (actualmente enfocado en lectura/remotos).
- **Sincronización bidireccional de ventas** (hoy es pull y reemplazo local).
- **Manejo robusto de errores** en ViewModels y UI.
- **Pruebas** unitarias/integración para casos de uso y repositorios.
- **Documentación técnica** (diagramas, convenciones y guía de contribución).


## Balance MVP (actualizado)
### ✅ Lo que ya está sólido
- **Base arquitectónica**: separación por capas (`data`, `domain`, `presentation`) e inyección de dependencias con Koin.
- **Persistencia local**: almacenamiento local con Room para soportar operación sin conexión.
- **Sincronización base**: integración con Appwrite y flujo de sincronización para ventas.
- **Feature de ventas avanzada**: existen casos de uso, repositorio offline-first y ViewModel para observación/sincronización.

### 🟡 Lo que está parcialmente cubierto
- **Productos y categorías**: hay infraestructura y lectura/mapeo, pero no está consolidado el CRUD completo en UX y dominio.
- **UX de flujos críticos**: existen pantallas y navegación, pero falta robustecer feedback de carga/error y consistencia de estados.
- **Pruebas automatizadas**: ya hay pruebas unitarias en piezas críticas (realtime, serializers, reconciliación), pero la cobertura aún no es de MVP “cerrado”.

### 🔴 Lo crítico que falta para declarar MVP cumplido
1. **CRUD completo de productos y categorías** en capa de dominio + UI.
2. **Sincronización bidireccional real** (subida de cambios locales + política de resolución de conflictos).
3. **Manejo de errores de punta a punta** (casos de uso, repositorios y ViewModels con mensajes accionables).
4. **Cobertura mínima de calidad**: pruebas de integración/repositorios y criterios de aceptación por feature.
5. **Definición explícita de “Done” del MVP**: checklist funcional y técnico para cierre.

### Propuesta de cierre MVP (orden recomendado)
1. **Vertical 1: catálogo (productos/categorías)**
   - Crear/editar/eliminar + validaciones + persistencia local.
2. **Vertical 2: ventas offline-first completas**
   - Cola de pendientes locales + reconciliación + estrategia de conflictos.
3. **Vertical 3: hardening UX**
   - Estados de carga, errores recuperables y confirmaciones de acciones sensibles.
4. **Vertical 4: calidad y release**
   - Suite de pruebas mínima para rutas críticas + documentación técnica corta.

### Criterio sugerido para “MVP listo”
- Un usuario puede **crear/editar/eliminar productos**, **registrar ventas**, operar **sin conexión** y sincronizar luego sin pérdida de datos.
- Existen **mensajes claros de error/estado** en los flujos principales.
- Las rutas críticas están cubiertas por pruebas automatizadas básicas.

## Estructura del proyecto (alto nivel)
```
app/
  src/main/java/com/elitec/alejotaller/
    feature/
      category/
      product/
      sale/
    infraestructure/
```

## Cómo ejecutar
1. Configura las credenciales de Appwrite en las variables de entorno o `BuildConfig`.
2. Compila con Gradle:
   ```
   ./gradlew assembleDebug
   ```

## Contribución
Si deseas contribuir, crea una rama, realiza tus cambios y abre un pull request.

## Flujo de autenticación con Google (actual)

### Objetivo
- Mantener un acceso de “un toque” para usuarios Google en Appwrite usando credenciales de email/password por detrás.
- Permitir que cualquier usuario cambie su contraseña sin romper su cuenta.

### Reglas del flujo
1. **Default Google**:
   - `email` de la cuenta Appwrite = email de Google.
   - `password` inicial = `sub` de Google.
   - En `prefs` se guarda `sub` (`prefs.sub = sub`) junto al resto de metadatos.
2. **Cambio de contraseña**:
   - El usuario puede cambiar su contraseña libremente.
   - El `sub` se mantiene en `prefs`; no se elimina.
3. **Google one-tap**:
   - Funciona directamente mientras la contraseña actual siga siendo el `sub`.
4. **Fallback (Opción A)**:
   - Si el login one-tap falla porque el usuario ya cambió su contraseña, se muestra un mensaje guiando a iniciar una vez con correo+contraseña para continuar usando Google.

### Detalle técnico del fallback
- En error de autenticación Google (`401`) se intenta alta automática.
- Si la alta responde `409` (cuenta ya existe), se interpreta como posible contraseña cambiada y se devuelve el mensaje de recuperación con login tradicional una vez.
