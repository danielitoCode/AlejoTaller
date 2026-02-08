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