import type { UserNetRepository } from "../repository/user.net.repository";
import type { SessionNetManager } from "../repository/session.net.manager";

export type LinkGoogleAccountInput = {
    email: string;
    currentPassword: string;
    googleSub: string;
    name: string;
    photoUrl: string;
};

export class LinkGoogleAccountCaseUse {
    constructor(
        private readonly userRepo: UserNetRepository,
        private readonly sessionNetManager: SessionNetManager
    ) {}

    async execute(input: LinkGoogleAccountInput): Promise<string> {
        const userId = await this.sessionNetManager.createEmailSession(input.email, input.currentPassword);
        await this.userRepo.linkGoogle(input.googleSub, input.photoUrl, input.name);
        return userId;
    }
}
