package com.elitec.shared.data.feature.auth.data.mapper

import com.elitec.shared.data.feature.auth.data.dto.UserDto
import org.junit.Test
import org.junit.Assert.assertEquals

class UserMapperTest {

    @Test
    fun `toDomain conserva datos clave del perfil`() {
        val dto = UserDto(
            id = "user-1",
            name = "Admin",
            email = "admin@alejo.dev",
            pass = "secret",
            sub = "google-sub",
            phone = "+5350000000",
            photoUrl = "https://img.dev/admin.png",
            verification = true,
            role = "admin"
        )

        val domain = dto.toDomain()

        assertEquals("user-1", domain.id)
        assertEquals("Admin", domain.name)
        assertEquals("admin", domain.userProfile.role)
        assertEquals("https://img.dev/admin.png", domain.userProfile.photoUrl)
    }
}
