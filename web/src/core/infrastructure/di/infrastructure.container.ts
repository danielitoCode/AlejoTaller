import {account, client, databases, functions, storage} from "./appwrite.config";
import {db} from "./dexie.db";
import {authService} from "./auth.service";

export const infrastructureContainer = {
    appwrite: {
        client,
        databases,
        storage,
        account,
        functions,
    },
    auth: authService,
    database: db
}