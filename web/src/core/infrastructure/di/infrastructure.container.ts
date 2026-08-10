import {account, client, databases, functions, storage, tablesDB} from "./appwrite.config";
import {db} from "./dexie.db";
import {authService} from "./auth.service";

export const infrastructureContainer = {
    appwrite: {
        client,
        databases,
        tablesDB,
        storage,
        account,
        functions,
    },
    auth: authService,
    database: db
}