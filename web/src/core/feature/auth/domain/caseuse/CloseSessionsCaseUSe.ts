import type {SessionNetManager} from "../repository/session.net.manager";

export class CloseSessionsCaseUSe {
    constructor(private readonly sessionNetManager: SessionNetManager) {}
    async execute(): Promise<void> {
        await this.sessionNetManager.closeSessions()
    }
}