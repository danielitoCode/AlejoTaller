export interface PasswordResetNetRepository {
    requestCode(): Promise<void>;
    confirmCode(params: { code: string; newPassword: string }): Promise<void>;
}

