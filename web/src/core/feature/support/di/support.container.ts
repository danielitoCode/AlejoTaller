import { databases } from "../../../infrastructure/di/appwrite.config";
import { SupportAppwriteRepository } from "../data/repository/support.appwrite.repository";
import { CreateSupportThreadCaseUse } from "../domain/caseuse/CreateSupportThreadCaseUse";
import { ListMySupportThreadsCaseUse } from "../domain/caseuse/ListMySupportThreadsCaseUse";
import { ListSupportMessagesCaseUse } from "../domain/caseuse/ListSupportMessagesCaseUse";
import { PostSupportMessageCaseUse } from "../domain/caseuse/PostSupportMessageCaseUse";
import { SubscribeSupportInboxCaseUse } from "../domain/caseuse/SubscribeSupportInboxCaseUse";

const repo = new SupportAppwriteRepository(databases);

const listMine = new ListMySupportThreadsCaseUse(repo);
const listMessages = new ListSupportMessagesCaseUse(repo);
const createThread = new CreateSupportThreadCaseUse(repo);
const postMessage = new PostSupportMessageCaseUse(repo);
const subscribe = new SubscribeSupportInboxCaseUse(repo);

export const supportContainer = {
    repositories: { net: repo },
    useCases: {
        listMine: listMine.execute.bind(listMine),
        listMessages: listMessages.execute.bind(listMessages),
        create: createThread.execute.bind(createThread),
        postMessage: postMessage.execute.bind(postMessage),
        subscribe: subscribe.execute.bind(subscribe)
    }
};
