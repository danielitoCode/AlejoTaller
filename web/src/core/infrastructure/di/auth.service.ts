import { account } from "./appwrite.config";

export const authService = {
    login: (email: string, password: string) =>
        account.createEmailPasswordSession(email, password),

    logout: () => account.deleteSession("current"),

    getCurrentUser: () => account.get()
}