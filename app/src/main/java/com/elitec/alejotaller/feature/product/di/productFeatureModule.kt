package com.elitec.alejotaller.feature.product.di

import com.elitec.alejotaller.feature.product.data.LikedProductsPreferences
import com.elitec.alejotaller.feature.product.data.repository.ProductNetRepositoryImpl
import com.elitec.alejotaller.feature.product.data.repository.ProductOfflineFirstRepository
import com.elitec.alejotaller.feature.product.domain.caseUse.ApplyProductRealtimeSnapshotsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ApplySoftHoldCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.CheckAProductExistenceCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.RefreshProductsByIdsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ReleaseSoftHoldCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.ports.LikedProductsStore
import com.elitec.shared.core.feature.product.domain.realtime.StockUpdatesListener
import com.elitec.alejotaller.feature.product.domain.repository.ProductNetRepository
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ShopCartViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import com.elitec.alejotaller.infraestructure.core.data.realtime.AppwriteStockUpdatesListener
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productFeatureModule = module {
    single { get<AppBD>().productsDao() }

    single<ProductNetRepository> { ProductNetRepositoryImpl(get()) }
    single<ProductRepository> { ProductOfflineFirstRepository(get(), get()) }

    // Likes locales (paridad web localStorage) — sin backend
    single<LikedProductsStore> { LikedProductsPreferences(androidContext()) }

    // Appwrite Realtime (reemplaza Pusher stock-updates)
    single<StockUpdatesListener> { AppwriteStockUpdatesListener(get()) }

    factory { GetProductByIdCaseUse(get()) }
    factory { ObserveProductsCaseUse(get()) }
    factory { SyncProductCaseUse(get()) }
    factory { CheckAProductExistenceCaseUse(get()) }
    factory { ApplySoftHoldCaseUse(get()) }
    factory { ReleaseSoftHoldCaseUse(get()) }
    factory { RefreshProductsByIdsCaseUse(get()) }
    factory { ApplyProductRealtimeSnapshotsCaseUse(get()) }

    viewModel { ShopCartViewModel() }
    viewModel {
        ProductViewModel(
            observeProductsCaseUse = get(),
            syncProductCaseUse = get(),
            getProductByIdCaseUse = get(),
            refreshProductsByIdsCaseUse = get(),
            applyProductRealtimeSnapshotsCaseUse = get(),
            stockUpdatesListener = get()
        )
    }
}
