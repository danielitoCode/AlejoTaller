import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {SaleNetRepository} from "../data/repository/sale.net.repository";
import {SaleTursoRepository} from "../data/repository/sale.turso.repository";
import {SaleOfflineFirstRepository} from "../data/repository/sale.offline-first.repository";
import {GetSalesCaseUse} from "../domain/caseuse/GetSalesCaseUse";
import { UpdateSaleVerifiedCaseUse } from "../domain/caseuse/UpdateSaleVerifiedCaseUse";
import { UpdateSaleDeliveryTypeCaseUse } from "../domain/caseuse/UpdateSaleDeliveryTypeCaseUse";
import { RegisterNewSaleCaseUse } from "../domain/caseuse/RegisterNewSaleCaseUse";
import { CancelUnverifiedSaleCaseUse } from "../domain/caseuse/CancelUnverifiedSaleCaseUse";
import { ApplySaleRealtimeSnapshotCaseUse } from "../domain/caseuse/ApplySaleRealtimeSnapshotCaseUse";
import { SessionSaleNotificationUserProvider } from "../data/repository/SessionSaleNotificationUserProvider";
import { TelegramNotificatorImpl } from "../data/repository/TelegramNotificatorImpl";
import {productContainer} from "../../product/di/product.container";
import {isTursoDataProvider} from "../../../infrastructure/turso/turso.client";
import {sessionStore} from "../../auth/presentation/viewmodel/session.store";

const netDatabases = infrastructureContainer.appwrite.databases;

const saleNetRepository = isTursoDataProvider()
    ? (new SaleTursoRepository() as unknown as SaleNetRepository)
    : new SaleNetRepository(netDatabases);

const saleOfflineFirstRepository = new SaleOfflineFirstRepository(saleNetRepository);

const saleNotificationUserProvider = new SessionSaleNotificationUserProvider(async () => {
    const u = await sessionStore.getCurrentUser();
    const anyU = u as any;
    return {
        $id: String(anyU?.$id ?? anyU?.id ?? ""),
        name: String(anyU?.name ?? anyU?.email ?? "Cliente"),
        email: String(anyU?.email ?? ""),
        phone: anyU?.phone ? String(anyU.phone) : "",
    } as any;
});

const telegramNotificator = new TelegramNotificatorImpl();

const getSalesCaseUse = new GetSalesCaseUse(saleOfflineFirstRepository);
const createSaleCaseUse = new RegisterNewSaleCaseUse(
    saleOfflineFirstRepository,
    saleNotificationUserProvider,
    telegramNotificator,
    productContainer.repositories.offlineFirst
);
const updateSaleVerifiedCaseUse = new UpdateSaleVerifiedCaseUse(saleOfflineFirstRepository);
const updateSaleDeliveryTypeCaseUse = new UpdateSaleDeliveryTypeCaseUse(saleOfflineFirstRepository);
const cancelUnverifiedSaleCaseUse = new CancelUnverifiedSaleCaseUse(
    saleOfflineFirstRepository,
    productContainer.repositories.offlineFirst
);
const applySaleRealtimeSnapshotCaseUse = new ApplySaleRealtimeSnapshotCaseUse(
    saleOfflineFirstRepository
);

export const saleContainer = {
    repositories: {
        net: saleNetRepository,
        offlineFirst: saleOfflineFirstRepository,
        telegramNotificator
    },
    useCases: {
        getAll: getSalesCaseUse,
        create: createSaleCaseUse,
        updateVerified: updateSaleVerifiedCaseUse,
        updateDeliveryType: updateSaleDeliveryTypeCaseUse,
        cancelUnverified: cancelUnverifiedSaleCaseUse,
        applyRealtimeSnapshot: applySaleRealtimeSnapshotCaseUse
    }
};
