package com.elitec.alejotallerscan.infraestructure.di

import androidx.room.Room
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import com.elitec.alejotallerscan.feature.confirmation.domain.caseuse.NotifyOperatorSaleDecisionCaseUse
import com.elitec.alejotallerscan.feature.confirmation.domain.repository.OperatorSaleRealtimeNotifier
import com.elitec.alejotallerscan.feature.history.data.repository.RoomOperatorSaleRecordRepository
import com.elitec.alejotallerscan.feature.history.domain.caseuse.ObserveOperatorSaleRecordsCaseUse
import com.elitec.alejotallerscan.feature.history.domain.caseuse.RegisterOperatorSaleRecordCaseUse
import com.elitec.alejotallerscan.feature.history.domain.repository.OperatorSaleRecordRepository
import com.elitec.alejotallerscan.feature.history.presentation.viewmodel.OperatorSaleRecordsViewModel
import com.elitec.alejotallerscan.feature.product.data.repository.AppwriteOperatorProductNameRepository
import com.elitec.alejotallerscan.feature.product.data.repository.OperatorProductNameRepository
import com.elitec.alejotallerscan.feature.product.domain.caseuse.EnrichSaleProductsCaseUse
import com.elitec.alejotallerscan.feature.reservation.domain.caseuse.SearchReservationsCaseUse
import com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel.OperatorReservationSearchViewModel
import com.elitec.alejotallerscan.feature.scan.domain.caseuse.ParseSaleScanPayloadCaseUse
import com.elitec.alejotallerscan.feature.scan.presentation.viewmodel.OperatorScanViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.feature.sync.domain.caseuse.SyncPendingOperatorSalesCaseUse
import com.elitec.alejotallerscan.feature.sync.domain.repository.OperatorSyncNotificationService
import com.elitec.alejotallerscan.infraestructure.core.data.bd.OperatorAppDatabase
import com.elitec.alejotallerscan.infraestructure.core.data.bd.OperatorAppDatabaseMigrations
import com.elitec.alejotallerscan.infraestructure.core.data.realtime.OperatorPusherConfig
import com.elitec.alejotallerscan.infraestructure.core.data.realtime.PusherSaleRealtimeNotifier
import com.elitec.alejotallerscan.infraestructure.core.presentation.services.OperatorSaleSyncNotificationService
import com.elitec.shared.auth.feature.auth.domain.caseuse.AuthOperatorUserCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.AuthUserCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.shared.auth.feature.auth.domain.ports.SessionManager
import com.elitec.shared.auth.feature.auth.domain.repositories.AccountRepository
import com.elitec.shared.data.feature.auth.data.repository.AccountRepositoryImpl
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepositoryImpl
import com.elitec.shared.data.feature.sale.data.repository.SaleOfflineFirstRepository
import com.elitec.shared.data.infraestructure.core.data.config.SaleRemoteConfig
import com.elitec.shared.data.infraestructure.core.data.repository.AppwriteSessionManager
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val operatorScanDiModule = module {
    single {
        Client(context = get())
            .setEndpoint(BuildConfig.APPWRITE_PROJECT_ENDPOINT)
            .setProject(BuildConfig.APPWRITE_PROJECT_ID)
            .setSelfSigned(false)
    }
    single { Account(get()) }
    single { Databases(get()) }
    single {
        SaleRemoteConfig(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            saleCollectionId = BuildConfig.SALE_TABLE_ID
        )
    }

    single {
        Room.databaseBuilder(
            get(),
            OperatorAppDatabase::class.java,
            "operator_app_database"
        )
            .addMigrations(OperatorAppDatabaseMigrations.MIGRATION_1_2)
            .build()
    }
    single { get<OperatorAppDatabase>().saleDao() }
    single { get<OperatorAppDatabase>().operatorSaleRecordDao() }
    single { OkHttpClient() }
    single {
        OperatorPusherConfig(
            appId = BuildConfig.PUSHER_APP_ID,
            apiKey = BuildConfig.PUSHER_API_KEY,
            apiSecret = BuildConfig.PUSHER_API_SECRETS,
            cluster = BuildConfig.PUSHER_CLUSTER,
            saleChannelPrefix = BuildConfig.PUSHER_SALE_CHANNEL.ifBlank { "sale-verification" }
        )
    }

    single<SessionManager> { AppwriteSessionManager(get()) }
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<SaleNetRepository> { SaleNetRepositoryImpl(get(), get()) }
    single<SaleRepository> { SaleOfflineFirstRepository(get(), get()) }
    single<OperatorSaleRealtimeNotifier> { PusherSaleRealtimeNotifier(get(), get()) }
    single<OperatorSaleRecordRepository> { RoomOperatorSaleRecordRepository(get()) }
    single<OperatorProductNameRepository> { AppwriteOperatorProductNameRepository(get()) }
    single<OperatorSyncNotificationService> { OperatorSaleSyncNotificationService(get()) }

    factory { AuthUserCaseUse(get()) }
    factory { GetCurrentUserInfoCaseUse(get()) }
    factory { CloseSessionCaseUse(get()) }
    factory { AuthOperatorUserCaseUse(get(), get(), get()) }
    factory { ParseSaleScanPayloadCaseUse() }
    factory { SearchReservationsCaseUse(get()) }
    factory { ObserveAllSalesCaseUse(get()) }
    factory { UpdateSaleVerificationFromRealtimeCaseUse(get()) }
    factory { NotifyOperatorSaleDecisionCaseUse(get()) }
    factory { RegisterOperatorSaleRecordCaseUse(get()) }
    factory { ObserveOperatorSaleRecordsCaseUse(get()) }
    factory { EnrichSaleProductsCaseUse(get()) }
    factory { SyncPendingOperatorSalesCaseUse(get(), get(), get()) }

    viewModel { OperatorAuthViewModel(get(), get(), get(), get()) }
    viewModel { OperatorReservationSearchViewModel(get()) }
    viewModel { OperatorScanViewModel(get(), get(), get(), get()) }
    viewModel { OperatorSalesViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { OperatorSaleRecordsViewModel(get(), get(), get()) }
}
