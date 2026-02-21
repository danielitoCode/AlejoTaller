package com.elitec.alejotaller.feature.sale.data.mappers

import com.elitec.alejotaller.feature.sale.data.mapper.toSaleItemList
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import io.appwrite.extensions.toJson
import org.junit.Assert
import org.junit.Test

class SaleItemListSerializerTest {
    @Test
    fun saleItemSerializerTest() {
        val saleItemTest = listOf(
            SaleItem("1", 32),
            SaleItem("43", 2),
            SaleItem("773", 2),
            SaleItem("12", 322)
        )
        val saleItemListTestSerialized = saleItemTest.toJson()
        println(saleItemListTestSerialized)

        val deserializedItemListTest = saleItemListTestSerialized.toSaleItemList()

        Assert.assertEquals(saleItemTest, deserializedItemListTest)

    }
}