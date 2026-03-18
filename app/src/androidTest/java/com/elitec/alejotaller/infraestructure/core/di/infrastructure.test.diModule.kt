package com.elitec.alejotaller.infraestructure.core.di

import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val infrastructureTestDiModule = module {
    // Presentation: ViewModels
    viewModel { ToasterViewModel() }
}

