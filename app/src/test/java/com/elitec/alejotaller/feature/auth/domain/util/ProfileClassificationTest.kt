package com.elitec.alejotaller.feature.auth.domain.util

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileClassificationTest {

    private fun user(
        id: String = "u1",
        name: String = "Daniel",
        email: String = "d@test.com"
    ) = User(
        id = id,
        name = name,
        email = email,
        pass = "",
        userProfile = UserProfile(sub = "", phone = "", photoUrl = "", verification = false)
    )

    @Test
    fun clearEmailProfile_isAuthenticated() {
        assertTrue(hasClearAuthenticatedProfile(user()))
        assertEquals(SessionMode.AUTHENTICATED, classifySessionMode(user()))
    }

    @Test
    fun emptyEmail_isVisitor() {
        val guest = user(name = "", email = "")
        assertFalse(hasClearAuthenticatedProfile(guest))
        assertEquals(SessionMode.VISITOR, classifySessionMode(guest))
    }

    @Test
    fun blankId_isVisitor() {
        assertFalse(hasClearAuthenticatedProfile(id = "", email = "a@b.com"))
        assertEquals(SessionMode.VISITOR, classifySessionMode(id = null, email = "a@b.com"))
    }

    @Test
    fun nullUser_isVisitor() {
        assertFalse(hasClearAuthenticatedProfile(null as User?))
        assertEquals(SessionMode.VISITOR, classifySessionMode(null as User?))
    }

    @Test
    fun guestProvider_isVisitor() {
        assertTrue(isGuestProvider("anonymous"))
        assertTrue(isGuestProvider("GUEST"))
        assertTrue(isAnonymousProfile(email = "x@y.com", provider = "guest"))
        assertFalse(hasClearAuthenticatedProfile(id = "1", email = "x@y.com", provider = "visitor"))
    }

    @Test
    fun displayName_forAnonymous() {
        val guest = user(name = "", email = "")
        assertEquals("Visitante", guest.displayName)
    }
}
