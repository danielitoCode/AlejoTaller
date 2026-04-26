package com.elitec.shared.auth.feature.auth.domain.entity

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OperatorAccessTest {

    @Test
    fun `accept a operator and administrators relationship users roles`() {
        assertTrue("operator".hasOperatorAccess())
        assertTrue("admin".hasOperatorAccess())
        assertTrue("administrator".hasOperatorAccess())
        assertTrue("owner".hasOperatorAccess())
    }

    @Test
    fun `denied a not know users roles`() {
        assertFalse("viewer".hasOperatorAccess())
        assertFalse("customer".hasOperatorAccess())
        assertFalse(null.hasOperatorAccess())
    }
}
