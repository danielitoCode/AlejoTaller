/**
 * @file UpdatePhoneCaseUse.test.ts
 * @description Tests para actualización de teléfono
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { UpdatePhoneCaseUse } from '../../../../../core/feature/auth/domain/caseuse/UpdatePhoneCaseUse';

describe('UpdatePhoneCaseUse', () => {
    let mockRepository: any;
    let useCase: UpdatePhoneCaseUse;

    beforeEach(() => {
        mockRepository = {
            updatePhone: vi.fn().mockResolvedValue(undefined),
            createAccount: vi.fn(),
            getCurrentUser: vi.fn(),
            updateName: vi.fn(),
            updatePassword: vi.fn(),
            updatePhotoUrl: vi.fn(),
            updateRole: vi.fn(),
            deleteUser: vi.fn()
        };

        useCase = new UpdatePhoneCaseUse(mockRepository);
        vi.clearAllMocks();
    });

    describe('when updating phone with valid format', () => {
        it('should call repository with phone number', async () => {
            await useCase.execute('+5355123456');

            expect(mockRepository.updatePhone).toHaveBeenCalledWith('+5355123456');
        });

        it('should accept international format', async () => {
            await expect(useCase.execute('+1-555-123-4567'))
                .resolves
                .not.toThrow();
        });

        it('should accept local format', async () => {
            await expect(useCase.execute('55123456'))
                .resolves
                .not.toThrow();
        });
    });

    describe('when validating input', () => {
        it('should reject empty phone', async () => {
            await expect(useCase.execute(''))
                .rejects
                .toThrow();
        });

        it('should reject phone with invalid characters', async () => {
            await expect(useCase.execute('abc-def-ghij'))
                .rejects
                .toThrow();
        });

        it('should reject very short phone numbers', async () => {
            await expect(useCase.execute('123'))
                .rejects
                .toThrow();
        });

        it('should reject very long phone numbers', async () => {
            await expect(useCase.execute('+12345678901234567890'))
                .rejects
                .toThrow();
        });
    });

    describe('when repository fails', () => {
        it('should handle network errors', async () => {
            mockRepository.updatePhone.mockRejectedValue(new Error('Timeout'));

            await expect(useCase.execute('+5355123456'))
                .rejects
                .toThrow();
        });
    });
});
