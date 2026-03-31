package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.shared.sale.feature.sale.domain.repository.SaleIdProvider
import io.appwrite.ID

class AppwriteSaleIdProvider : SaleIdProvider {
    override fun nextId(): String = ID.unique()
}
