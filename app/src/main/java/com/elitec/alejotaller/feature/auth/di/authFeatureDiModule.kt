package com.elitec.alejotaller.feature.auth.di

import com.elitec.alejotaller.feature.auth.data.AccountRepositoryImpl
import com.elitec.alejotaller.feature.auth.domain.caseuse.CreateAccountUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.IsUserVerifiedUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.RegisterWithGoogleUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserNameUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPassCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.VerifyUserUseCase
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import org.koin.dsl.module

val authFeatureDiModule = module {
    // Data layer
    single<AccountRepository> { AccountRepositoryImpl(get()) }

    // Domain layer
    factory { CreateAccountUseCase(get()) }
    factory { GetCurrentUserInfoCaseUse(get()) }
    factory { IsUserVerifiedUseCase(get()) }
    factory {
        RegisterWithGoogleUseCase(
            get(),
            get(),
            get())
    }
    factory { UpdateUserNameUseCase(get()) }
    factory { UpdateUserPassCaseUse(get()) }
    factory { VerifyUserUseCase(get()) }

    // Presentation layer
}