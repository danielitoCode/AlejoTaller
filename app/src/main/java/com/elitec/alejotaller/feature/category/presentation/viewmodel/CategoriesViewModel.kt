package com.elitec.alejotaller.feature.category.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.category.domain.caseUse.ObserveCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.caseUse.SyncCategoriesCaseUse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    observeCategories: ObserveCategoriesCaseUse,
    private val syncCategories: SyncCategoriesCaseUse
): ViewModel() {
    val categoriesFlow = observeCategories()
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )


    init {
        viewModelScope.launch {
            syncCategories()
        }
    }
}