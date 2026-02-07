package com.elitec.alejotaller.feature.category.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.category.domain.caseUse.GetAllCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.entity.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getAllCategoriesCaseUse: GetAllCategoriesCaseUse
): ViewModel() {
    private var _categoriesFlow = MutableStateFlow(listOf<Category>())
    val categoriesFlow get() = _categoriesFlow.asStateFlow()

    init {
        syncCategories()
    }

    private fun syncCategories() {
        viewModelScope.launch {
            getAllCategoriesCaseUse()
                .onSuccess { categories ->
                    _categoriesFlow.value = categories
                }
        }
    }
}