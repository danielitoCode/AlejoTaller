/**
 * @file SaveProductCaseUse.test.ts
 * @description Tests para guardado de productos
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { SaveProductCaseUse } from '../../../../../core/feature/product/domain/caseuse/SaveProductCaseUse';
import type { ProductRepository } from '../../../../../core/feature/product/domain/repository/product.repository';
import type { Product } from '../../../../../core/feature/product/domain/entity/Product';

describe('SaveProductCaseUse', () => {
    let mockRepository: jest.Mocked<ProductRepository>;
    let useCase: SaveProductCaseUse;

    function createValidProduct(): Product {
        return {
            id: '',
            name: 'Test Product',
            price: 150,
            categoryId: 'cat-123',
            stock: 10,
            description: 'Test description',
            imageUrl: ''
        };
    }

    beforeEach(() => {
        mockRepository = {
            create: vi.fn().mockResolvedValue(undefined),
            update: vi.fn(),
            delete: vi.fn(),
            getById: vi.fn(),
            getAll: vi.fn()
        } as any;

        useCase = new SaveProductCaseUse(mockRepository);
        vi.clearAllMocks();
    });

    describe('when saving product with valid data', () => {
        it('should call repository create method', async () => {
            const product = createValidProduct();

            await useCase.execute(product);

            expect(mockRepository.create).toHaveBeenCalledWith(product);
        });

        it('should complete without errors', async () => {
            const product = createValidProduct();

            await expect(useCase.execute(product)).resolves.not.toThrow();
        });
    });

    describe('when validating product data', () => {
        it('should reject product without category', async () => {
            const product = { ...createValidProduct(), categoryId: '' };

            await useCase.execute(product);
            
            // Comportamiento actual: retorna sin crear (línea 12 del case use)
            expect(mockRepository.create).not.toHaveBeenCalled();
        });

        it('should reject product with negative price', async () => {
            const product = { ...createValidProduct(), price: -50 };

            await expect(useCase.execute(product))
                .rejects
                .toThrow();
        });

        it('should reject product with zero price', async () => {
            const product = { ...createValidProduct(), price: 0 };

            await expect(useCase.execute(product))
                .rejects
                .toThrow();
        });

        it('should reject product with empty name', async () => {
            const product = { ...createValidProduct(), name: '' };

            await expect(useCase.execute(product))
                .rejects
                .toThrow();
        });

        it('should reject product with negative stock', async () => {
            const product = { ...createValidProduct(), stock: -5 };

            await expect(useCase.execute(product))
                .rejects
                .toThrow();
        });
    });

    describe('when repository fails', () => {
        it('should propagate database errors', async () => {
            mockRepository.create.mockRejectedValue(new Error('Database error'));

            await expect(useCase.execute(createValidProduct()))
                .rejects
                .toThrow();
        });
    });
});
