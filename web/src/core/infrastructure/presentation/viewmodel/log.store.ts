import {writable} from "svelte/store";

export type LogType = "log" | "info" | "warn" | "error";

export interface LogEntry {
    id: number;
    message: string;
    type: LogType;
    timestamp: Date;
    stack?: string;
}

function createLogStore() {
    const { subscribe, update, set } = writable<LogEntry[]>([]);
    let counter = 0;

    function getAllLogs(): LogEntry[] {
        let result: LogEntry[] = [];
        const unsubscribe = subscribe((logs) => { result = logs; });
        unsubscribe();
        return result;
    }

    return {
        subscribe,
        add: (message: any, type: LogType = "log", stack?: string) => {
            update(logs => [
                ...logs,
                {
                    id: counter++,
                    message: typeof message === "string"
                        ? message
                        : JSON.stringify(message, null, 2),
                    type,
                    timestamp: new Date(),
                    stack
                }
            ]);
        },
        clear: () => set([]),
        getAll: getAllLogs
    };
}

export const logStore = createLogStore();