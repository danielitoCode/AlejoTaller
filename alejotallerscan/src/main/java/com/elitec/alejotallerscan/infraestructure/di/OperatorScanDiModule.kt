package com.elitec.alejotallerscan.infraestructure.di

import androidx.room.Room
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.data.bd.OperatorAppDatabase
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
        ).fallbackToDestructiveMigration(false)
            .build()
    }
    single { get<OperatorAppDatabase>().saleDao() }

    single<SessionManager> { AppwriteSessionManager(get()) }
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<SaleNetRepository> { SaleNetRepositoryImpl(get(), get()) }
    single<SaleRepository> { SaleOfflineFirstRepository(get(), get()) }

    factory { AuthUserCaseUse(get()) }
    factory { GetCurrentUserInfoCaseUse(get()) }
    factory { CloseSessionCaseUse(get()) }
    factory { AuthOperatorUserCaseUse(get(), get(), get()) }
    factory { ObserveAllSalesCaseUse(get()) }
    factory { UpdateSaleVerificationFromRealtimeCaseUse(get()) }

    viewModel { OperatorAuthViewModel(get(), get(), get()) }
    viewModel { OperatorSalesViewModel(get(), get(), get(), get()) }
}
