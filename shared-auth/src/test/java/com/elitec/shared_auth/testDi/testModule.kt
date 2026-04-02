package com.elitec.shared_auth.testDi

import com.elitec.shared.auth.feature.auth.domain.ports.SessionManager
import com.elitec.shared_auth.fakes.feature.auth.data.SessionManagerImplFake
import org.koin.dsl.module

val testModule = module {
    single<SessionManager> { SessionManagerImplFake() }
}