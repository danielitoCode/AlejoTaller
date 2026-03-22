import { logStore} from "../viewmodel/log.store";

class LoggerService {

    log(message: any) {
        logStore.add(message, "log");
    }

    info(message: any) {
        logStore.add(message, "info");
    }

    warn(message: any) {
        logStore.add(message, "warn");
    }

    error(message: any, stack?: string) {
        logStore.add(message, "error", stack);
    }
}

export const logger = new LoggerService();