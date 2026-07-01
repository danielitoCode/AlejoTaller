/**
 * @file setup.ts
 * @description Configuración global para tests con Vitest y Testing Library
 * @setup Ejecutado antes de cada suite de tests
 */

import '@testing-library/jest-dom/vitest';
import { vi, beforeEach, afterEach } from 'vitest';

// ============================================
// Mock de localStorage
// ============================================
const localStorageMock = (() => {
    let store: Record<string, string> = {};
    return {
        getItem: vi.fn((key: string) => store[key] || null),
        setItem: vi.fn((key: string, value: string) => {
            store[key] = value.toString();
        }),
        removeItem: vi.fn((key: string) => {
            delete store[key];
        }),
        clear: vi.fn(() => {
            store = {};
        })
    };
})();

Object.defineProperty(window, 'localStorage', {
    value: localStorageMock
});

// ============================================
// Mock de matchMedia (para componentes responsive)
// ============================================
Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
    })),
});

// ============================================
// Mock de scrollTo (para navegación)
// ============================================
Object.defineProperty(window, 'scrollTo', {
    writable: true,
    value: vi.fn()
});

// ============================================
// Suppress console output durante tests
// ============================================
beforeEach(() => {
    // Spy en console pero permitir ver logs cuando sea necesario
    vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.spyOn(console, 'warn').mockImplementation(() => {});
    // Mantener console.log activo para debugging si es necesario
    // vi.spyOn(console, 'log').mockImplementation(() => {});
});

afterEach(() => {
    vi.clearAllMocks();
    vi.restoreAllMocks();
});

// ============================================
// Helpers globales para factories
// ============================================
export function createMockUser(overrides: any = {}) {
    return {
        $id: 'user-test-123',
        email: 'test@example.com',
        name: 'Test User',
        phone: '+5355123456',
        prefs: {
            photo_url: '',
            role: 'customer',
            bio: ''
        },
        emailVerification: true,
        ...overrides
    };
}

export function createMockProduct(overrides: any = {}) {
    return {
        id: 'product-test-123',
        name: 'Test Product',
        price: 100,
        categoryId: 'category-test',
        stock: 10,
        description: 'Test product description',
        imageUrl: '',
        ...overrides
    };
}

export function createMockSale(overrides: any = {}) {
    return {
        id: 'sale-test-123',
        date: new Date().toISOString(),
        amount: 250,
        verified: 'UNVERIFIED',
        products: [],
        userId: 'user-test-123',
        deliveryType: null,
        ...overrides
    };
}
