/**
 * @file UpdateNameCaseUse.test.ts
 * @description Tests para actualización de nombre de usuario
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { UpdateNameCaseUse } from '../../../../../core/feature/auth/domain/caseuse/UpdateNameCaseUse';

describe('UpdateNameCaseUse', () => {
    let mockRepository: any;
    let useCase: UpdateNameCaseUse;

    beforeEach(() => {
        mockRepository = {
            updateName: vi.fn().mockResolvedValue(undefined),
            createAccount: vi.fn(),
            getCurrentUser: vi.fn(),
            updatePassword: vi.fn(),
            updatePhotoUrl: vi.fn(),
            updatePhone: vi.fn(),
            updateRole: vi.fn(),
            deleteUser: vi.fn()
        };

        useCase = new UpdateNameCaseUse(mockRepository);
        vi.clearAllMocks();
    });

    describe('when updating name with valid input', () => {
        it('should call repository with trimmed name', async () => {
            await useCase.execute('  Nuevo Nombre  ');

            expect(mockRepository.updateName).toHaveBeenCalledWith('Nuevo Nombre');
        });

        it('should complete successfully', async () => {
            await expect(useCase.execute('Valid Name')).resolves.not.toThrow();
        });
    });

    describe('when validating input', () => {
        it('should reject empty name', async () => {
            await expect(useCase.execute(''))
                .rejects
                .toThrow();
        });

        it('should reject whitespace-only name', async () => {
            await expect(useCase.execute('   '))
                .rejects
                .toThrow();
        });

        it('should reject very long names', async () => {
            const longName = 'A'.repeat(100);
            
            await expect(useCase.execute(longName))
                .rejects
                .toThrow();
        });

        it('should reject names shorter than 2 characters', async () => {
            await expect(useCase.execute('A'))
                .rejects
                .toThrow();
        });
    });

    describe('when repository fails', () => {
        it('should propagate network errors', async () => {
            mockRepository.updateName.mockRejectedValue(new Error('Network error'));

            await expect(useCase.execute('Valid Name'))
                .rejects
                .toThrow();
        });
    });
});
