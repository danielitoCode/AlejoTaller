# 🧪 Estrategia de Testing - Proyecto Web TallerAlejo

## 📋 Resumen de Implementación

Este documento describe la cobertura de pruebas implementada en el proyecto, organizada por features según la arquitectura Clean Architecture.

---

## ✅ Configuración Completada

### Archivos de Configuración
- ✅ `src/test/setup.ts` - Configuración global con mocks de localStorage, matchMedia, factories
- ✅ `vitest.config.ts` - Config actualizada con coverage thresholds (60-65%)
- ✅ `package.json` - Scripts añadidos: `test:coverage`, `test:ui`

### Dependencias Requeridas
```bash
pnpm add -D @testing-library/svelte @testing-library/jest-dom @testing-library/user-event
```

---

## 📊 Tests Implementados por Feature

### **Feature: AUTH** 🔴 (Nivel 1 - Crítico)

#### Case Uses
- ✅ `CreateAccountCaseUse.test.ts` - 8 tests
  - Validación de email, password, nombre
  - Manejo de errores del repositorio
  - Prevención de estado parcial
  
- ✅ `UpdateNameCaseUse.test.ts` - 7 tests
  - Validación de longitud (2-50 caracteres)
  - Trim de whitespace
  - Rechazo de nombres vacíos

- ✅ `UpdatePhoneCaseUse.test.ts` - 7 tests
  - Validación de formato internacional
  - Longitud mínima/máxima
  - Caracteres válidos

#### Stores (Existentes)
- ✅ `profile.store.test.ts` - 3 tests (ya existía)
- ✅ `session.store.test.ts` - 2 tests (ya existía)

**Total Auth: ~27 tests**

---

### **Feature: PRODUCT** 🟡 (Nivel 2 - Alto)

#### Case Uses
- ✅ `SaveProductCaseUse.test.ts` - 9 tests
  - Validación de categoría requerida
  - Precio positivo (>0)
  - Stock no negativo
  - Nombre requerido

**Total Product: 9 tests**

---

### **Feature: SALE** 🔴 (Nivel 1 - Crítico)

#### Case Uses
- ✅ `CreateSaleCaseUse.test.ts` - 11 tests
  - Productos no vacíos
  - Monto positivo
  - User ID requerido
  - Cantidades válidas
  - Cálculo correcto de totales

#### Stores (Existente)
- ✅ `sale.store.test.ts` - 5 tests (ya existía)

**Total Sale: 16 tests**

---

### **Feature: EXCHANGE** 🟢 (Nivel 3 - Medio)

#### Mappers (Existente)
- ✅ `Mapper.test.ts` - 2 tests (ya existía)

**Total Exchange: 2 tests**

---

## 📈 Métricas Actuales

| Feature | Tests Escritos | Cobertura Estimada | Estado |
|---------|---------------|-------------------|--------|
| Auth | 27 | ~45% | 🟡 En progreso |
| Product | 9 | ~30% | 🟡 Iniciado |
| Sale | 16 | ~40% | 🟡 En progreso |
| Exchange | 2 | ~20% | 🔵 Básico |
| Category | 0 | 0% | ⚪ Pendiente |
| Notification | 0 | 0% | ⚪ Pendiente |
| Support | 0 | 0% | ⚪ Pendiente |
| **TOTAL** | **54 tests** | **~25%** | **🟡 25% completo** |

---

## 🎯 Próximos Pasos (Priorizados)

### Semana 1: Completar Nivel 1 (Crítico)
- [ ] `OpenSessionCaseUse.test.ts`
- [ ] `CloseSessionsCaseUSe.test.ts`
- [ ] `UpdatePasswordCaseUse.test.ts`
- [ ] `UpdatePhotoCaseUse.test.ts`
- [ ] `LinkGoogleAccountCaseUse.test.ts`
- [ ] `RegisterNewSaleCaseUse.test.ts`
- [ ] `GetSalesCaseUse.test.ts`
- [ ] `user.net.repository.test.ts` (expandir existente)

**Meta:** 40 tests adicionales → ~45% cobertura

---

### Semana 2: Nivel 2 (Alto)
- [ ] `GetAllProductCaseUse.test.ts`
- [ ] `GetProductByIdCaseUse.test.ts`
- [ ] `DeleteProductCaseUse.test.ts`
- [ ] `UpdateProductPriceCaseUse.test.ts`
- [ ] `product.net.repository.test.ts`
- [ ] `category.net.repository.test.ts`
- [ ] Componentes Svelte: `Login.svelte.test.ts`, `InternalProfileScreen.svelte.test.ts`

**Meta:** 30 tests adicionales → ~60% cobertura

---

### Semana 3: Nivel 3 (Medio)
- [ ] Mappers restantes
- [ ] Utilidades (validation, formatting)
- [ ] Navegación (router tests)
- [ ] Stores adicionales

**Meta:** 20 tests adicionales → ~70% cobertura

---

## 🚀 Cómo Ejecutar Tests

### Todos los tests
```bash
pnpm test
```

### Con cobertura
```bash
pnpm test:coverage
```

### Modo watch (desarrollo)
```bash
pnpm test:unit:watch
```

### UI interactiva
```bash
pnpm test:ui
```

---

## 📝 Patrones de Testing Utilizados

### 1. AAA Pattern (Arrange-Act-Assert)
```typescript
it('should do something', async () => {
    // Arrange
    const input = createValidInput();
    
    // Act
    await useCase.execute(input);
    
    // Assert
    expect(mockRepository.method).toHaveBeenCalledWith(input);
});
```

### 2. Factory Functions
```typescript
function createValidUser() {
    return {
        email: 'test@example.com',
        password: 'SecurePass123!',
        name: 'Test User',
        // ... defaults
    };
}
```

### 3. Describe Blocks Anidados
```typescript
describe('UseCaseName', () => {
    describe('when condition is met', () => {
        it('should behave correctly', () => {});
    });
    
    describe('when validation fails', () => {
        it('should throw error', () => {});
    });
    
    describe('when repository fails', () => {
        it('should propagate error', () => {});
    });
});
```

---

## ⚠️ Consideraciones Importantes

### Mocks vs Realidad
- Los repositories están mockeados para aislamiento
- Para integration tests, considerar MSW (Mock Service Worker)
- Mantener mocks cercanos a la implementación real

### Coverage Thresholds
- **Branches:** 60% mínimo
- **Functions:** 65% mínimo
- **Lines:** 65% mínimo
- **Statements:** 65% mínimo

Estos valores son alcanzables en 4-6 semanas de trabajo enfocado.

### Qué NO Testear
- ❌ Containers de DI (`**/di/**/*.ts`)
- ❌ Fixtures de datos de prueba
- ❌ Configuración (`.config.ts`)
- ❌ Types y interfaces (`.d.ts`)

---

## 📚 Referencias

- [Vitest Documentation](https://vitest.dev/)
- [Testing Library](https://testing-library.com/)
- [Svelte Testing](https://testing-library.com/docs/svelte-testing-library/intro/)

---

## 👥 Contributing

Al añadir nuevos tests:
1. Seguir estructura de directorios existente
2. Usar factories para datos de prueba
3. Incluir al menos 3 escenarios por case use (happy path, validación, error)
4. Documentar casos edge en comentarios
5. Actualizar este README con métricas

---

**Última actualización:** $(date +%Y-%m-%d)  
**Tests totales:** 54  
**Cobertura estimada:** ~25%  
**Próximo objetivo:** 70% en 6 semanas
