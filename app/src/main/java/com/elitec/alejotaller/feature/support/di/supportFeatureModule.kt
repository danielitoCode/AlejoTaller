package com.elitec.alejotaller.feature.support.di

import com.elitec.alejotaller.feature.support.data.repository.SupportAppwriteRepository
import com.elitec.alejotaller.feature.support.domain.caseuse.CreateSupportThreadCaseUse
import com.elitec.alejotaller.feature.support.domain.caseuse.ListMySupportThreadsCaseUse
import com.elitec.alejotaller.feature.support.domain.caseuse.ListSupportMessagesCaseUse
import com.elitec.alejotaller.feature.support.domain.caseuse.MarkThreadReadCaseUse
import com.elitec.alejotaller.feature.support.domain.caseuse.PostSupportMessageCaseUse
import com.elitec.alejotaller.feature.support.domain.caseuse.SubscribeSupportInboxCaseUse
import com.elitec.alejotaller.feature.support.domain.repository.SupportRepository
import com.elitec.alejotaller.feature.support.presentation.viewmodel.SupportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val supportFeatureModule = module {
    single<SupportRepository> { SupportAppwriteRepository(get(), get()) }

    factory { ListMySupportThreadsCaseUse(get()) }
    factory { ListSupportMessagesCaseUse(get()) }
    factory { CreateSupportThreadCaseUse(get()) }
    factory { PostSupportMessageCaseUse(get()) }
    factory { MarkThreadReadCaseUse(get()) }
    factory { SubscribeSupportInboxCaseUse(get()) }

    viewModel {
        SupportViewModel(
            listMine = get(),
            listMessages = get(),
            createThread = get(),
            postMessage = get(),
            markRead = get(),
            subscribeInbox = get(),
        )
    }
}
