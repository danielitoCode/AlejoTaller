package com.elitec.alejotaller.feature.auth.di

import com.elitec.alejotaller.feature.auth.data.repository.AccountRepositoryImpl
import com.elitec.alejotaller.feature.auth.data.repository.FileUploadRepoImpl
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthUserCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthWithGoogleCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.CreateAccountUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.CustomRegisterCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.IsUserVerifiedUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.RegisterWithGoogleUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserNameUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPassCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhoneCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhotoUrlCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.VerifyUserUseCase
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.AuthViewModel
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.RegistrationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authFeatureDiModule = module {
    // Data layer
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<FileRepository> { FileUploadRepoImpl(get()) }

    // Domain layer
    factory { CreateAccountUseCase(get(), get()) }
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
    factory { UpdateUserPhoneCaseUse(get()) }
    factory { UpdateUserPhotoUrlCaseUse(get(), get()) }
    factory { VerifyUserUseCase(get()) }
    factory { AuthUserCaseUse(get()) }
    factory { CloseSessionCaseUse(get()) }
    factory { AuthWithGoogleCaseUse(get(), get(), get(), get()) }
    factory { CustomRegisterCaseUse(get(), get()) }

    // Presentation layer
    viewModel {
        AuthViewModel(
            get(),
            get(),
            get())
    }
    viewModel { RegistrationViewModel(get(), get()) }
    viewModel {
        ProfileViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get())
    }
}