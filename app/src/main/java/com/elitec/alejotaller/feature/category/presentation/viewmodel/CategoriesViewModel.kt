package com.elitec.alejotaller.feature.category.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.category.domain.caseUse.ObserveCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.caseUse.SyncCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.entity.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val observeCategories: ObserveCategoriesCaseUse,
    private val syncCategories: SyncCategoriesCaseUse
): ViewModel() {
    val _categoriesFlow = observeCategories()
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )
    val categoriesFlow get() = _categoriesFlow

    init {
        viewModelScope.launch {
            syncCategories()
        }
    }
}