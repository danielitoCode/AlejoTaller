/**
 * @file CreateAccountCaseUse.test.ts
 * @description Tests para el caso de uso de creación de cuenta
 * @critical Security-sensitive: Handles user registration
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { CreateAccountCaseUse } from '../../../../../core/feature/auth/domain/caseuse/CreateAccountCaseUse';
import type { UserNetRepository } from '../../../../../core/feature/auth/domain/repository/user.net.repository';

describe('CreateAccountCaseUse', () => {
    let mockRepository: jest.Mocked<UserNetRepository>;
    let useCase: CreateAccountCaseUse;

    function createValidUser() {
        return {
            email: 'usuario@ejemplo.com',
            password: 'ContraseñaSegura123!',
            name: 'Usuario Test',
            phone: '+5355123456',
            photo_url: '',
            role: 'customer'
        };
    }

    beforeEach(() => {
        mockRepository = {
            createAccount: vi.fn().mockResolvedValue(undefined),
            updatePrefs: vi.fn().mockResolvedValue(undefined),
            getCurrentUser: vi.fn(),
            updateName: vi.fn(),
            updatePassword: vi.fn(),
            updatePhotoUrl: vi.fn(),
            updatePhone: vi.fn(),
            updateRole: vi.fn(),
            deleteUser: vi.fn()
        } as any;

        useCase = new CreateAccountCaseUse(mockRepository);
        vi.clearAllMocks();
    });

    describe('when creating account with valid data', () => {
        it('should call repository with correct parameters', async () => {
            const user = createValidUser();

            await useCase.execute(user);

            expect(mockRepository.createAccount).toHaveBeenCalledWith(
                expect.objectContaining({
                    email: user.email,
                    name: user.name,
                    password: user.password
                })
            );
        });

        it('should complete without errors', async () => {
            const user = createValidUser();

            await expect(useCase.execute(user)).resolves.not.toThrow();
        });
    });

    describe('when validating input', () => {
        it('should reject empty email', async () => {
            const invalidUser = { ...createValidUser(), email: '' };

            await expect(useCase.execute(invalidUser))
                .rejects
                .toThrow();
        });

        it('should reject invalid email format', async () => {
            const invalidUser = { ...createValidUser(), email: 'not-an-email' };

            await expect(useCase.execute(invalidUser))
                .rejects
                .toThrow();
        });

        it('should reject short password', async () => {
            const weakUser = { ...createValidUser(), password: '123' };

            await expect(useCase.execute(weakUser))
                .rejects
                .toThrow();
        });

        it('should reject empty name', async () => {
            const invalidUser = { ...createValidUser(), name: '' };

            await expect(useCase.execute(invalidUser))
                .rejects
                .toThrow();
        });
    });

    describe('when repository fails', () => {
        it('should propagate error with context', async () => {
            const networkError = new Error('Network timeout');
            mockRepository.createAccount.mockRejectedValue(networkError);

            await expect(useCase.execute(createValidUser()))
                .rejects
                .toThrow();
        });

        it('should not leave partial state on failure', async () => {
            mockRepository.createAccount.mockRejectedValue(new Error('Fail'));

            try {
                await useCase.execute(createValidUser());
            } catch (error) {
                // Verificar que no se llamó a otros métodos (rollback)
                expect(mockRepository.updatePrefs).not.toHaveBeenCalled();
            }
        });
    });
});
