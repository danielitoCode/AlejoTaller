/**
 * @file CreateSaleCaseUse.test.ts
 * @description Tests para creación de ventas
 * @critical Revenue-sensitive: Handles transactions
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { CreateSaleCaseUse } from '../../../../../core/feature/sale/domain/caseuse/CreateSaleCaseUse';
import type { SaleRepository } from '../../../../../core/feature/sale/domain/repository/sale.repository';

describe('CreateSaleCaseUse', () => {
    let mockRepository: jest.Mocked<SaleRepository>;
    let useCase: CreateSaleCaseUse;

    function createValidSale() {
        return {
            id: '',
            date: new Date().toISOString(),
            amount: 250,
            verified: 'UNVERIFIED',
            products: [
                { id: 'prod-1', name: 'Product 1', price: 100, quantity: 2 },
                { id: 'prod-2', name: 'Product 2', price: 50, quantity: 1 }
            ],
            userId: 'user-123',
            deliveryType: null
        };
    }

    beforeEach(() => {
        mockRepository = {
            create: vi.fn().mockResolvedValue(undefined),
            update: vi.fn(),
            delete: vi.fn(),
            getById: vi.fn(),
            getByUser: vi.fn(),
            getAll: vi.fn()
        } as any;

        useCase = new CreateSaleCaseUse(mockRepository);
        vi.clearAllMocks();
    });

    describe('when creating sale with valid data', () => {
        it('should call repository create method', async () => {
            const sale = createValidSale();

            await useCase.execute(sale);

            expect(mockRepository.create).toHaveBeenCalledWith(sale);
        });

        it('should complete successfully', async () => {
            const sale = createValidSale();

            await expect(useCase.execute(sale)).resolves.not.toThrow();
        });
    });

    describe('when validating sale data', () => {
        it('should reject sale with empty products array', async () => {
            const sale = { ...createValidSale(), products: [] };

            await expect(useCase.execute(sale))
                .rejects
                .toThrow();
        });

        it('should reject sale with negative total amount', async () => {
            const sale = { ...createValidSale(), amount: -100 };

            await expect(useCase.execute(sale))
                .rejects
                .toThrow();
        });

        it('should reject sale with zero amount', async () => {
            const sale = { ...createValidSale(), amount: 0 };

            await expect(useCase.execute(sale))
                .rejects
                .toThrow();
        });

        it('should reject sale without user ID', async () => {
            const sale = { ...createValidSale(), userId: '' };

            await expect(useCase.execute(sale))
                .rejects
                .toThrow();
        });

        it('should reject sale with invalid product quantities', async () => {
            const sale = {
                ...createValidSale(),
                products: [{ id: 'prod-1', name: 'Product', price: 100, quantity: 0 }]
            };

            await expect(useCase.execute(sale))
                .rejects
                .toThrow();
        });
    });

    describe('when calculating totals', () => {
        it('should accept sale with correctly calculated total', async () => {
            const sale = {
                ...createValidSale(),
                products: [
                    { id: 'p1', name: 'P1', price: 50, quantity: 2 },
                    { id: 'p2', name: 'P2', price: 30, quantity: 1 }
                ],
                amount: 130 // (50*2) + (30*1)
            };

            await expect(useCase.execute(sale)).resolves.not.toThrow();
        });
    });

    describe('when repository fails', () => {
        it('should propagate persistence errors', async () => {
            mockRepository.create.mockRejectedValue(new Error('DB constraint violation'));

            await expect(useCase.execute(createValidSale()))
                .rejects
                .toThrow();
        });

        it('should not leave partial sale on failure', async () => {
            mockRepository.create.mockRejectedValue(new Error('Fail'));

            try {
                await useCase.execute(createValidSale());
            } catch (error) {
                // Verificar rollback
                expect(mockRepository.update).not.toHaveBeenCalled();
            }
        });
    });
});
