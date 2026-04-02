package com.elitec.shared.auth.feature.auth.domain.entity

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OperatorAccessTest {

    @Test
    fun `acepta operator y roles administrativos relacionados`() {
        assertTrue("operator".hasOperatorAccess())
        assertTrue("admin".hasOperatorAccess())
        assertTrue("administrator".hasOperatorAccess())
        assertTrue("owner".hasOperatorAccess())
    }

    @Test
    fun `rechaza roles ajenos a operacion`() {
        assertFalse("viewer".hasOperatorAccess())
        assertFalse("customer".hasOperatorAccess())
        assertFalse(null.hasOperatorAccess())
    }
}
