import { SupportPulseRepository } from "../data/repository/support.pulse.repository";
import { GetAllSupportMessagesCaseUse } from "../domain/caseuse/GetAllSupportMessagesCaseUse";
import { SubscribeSupportInboxCaseUse } from "../domain/caseuse/SubscribeSupportInboxCaseUse";
import { UpdateSupportStatusCaseUse } from "../domain/caseuse/UpdateSupportStatusCaseUse";

const repo = new SupportPulseRepository();

const getAll = new GetAllSupportMessagesCaseUse(repo);
const subscribeInbox = new SubscribeSupportInboxCaseUse(repo);
const updateStatus = new UpdateSupportStatusCaseUse(repo);

export const supportContainer = {
    repositories: { net: repo },
    useCases: {
        inbox: {
            getAll: getAll.execute.bind(getAll),
            subscribe: subscribeInbox.execute.bind(subscribeInbox),
            updateStatus: updateStatus.execute.bind(updateStatus)
        }
    }
};
