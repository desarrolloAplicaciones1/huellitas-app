package com.uade.huellitas.data.mapper

import com.uade.huellitas.makeUser
import com.uade.huellitas.makeUserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserMapperTest {

    // ── entity → domain ──────────────────────────────────────────────────────

    @Test
    fun `toDomain maps all base fields`() {
        val domain = makeUserEntity(uid = "uid-99", name = "Carlos", email = "carlos@test.com").toDomain()
        assertEquals("uid-99",          domain.uid)
        assertEquals("Carlos",          domain.name)
        assertEquals("carlos@test.com", domain.email)
    }

    @Test
    fun `toDomain maps null optional fields`() {
        val entity = makeUserEntity().copy(phone = null, avatarUrl = null, location = null)
        val domain = entity.toDomain()
        assertNull(domain.phone)
        assertNull(domain.avatarUrl)
        assertNull(domain.location)
    }

    @Test
    fun `toDomain maps non-null optional fields`() {
        val entity = makeUserEntity().copy(
            phone = "1122334455",
            avatarUrl = "https://example.com/avatar.png",
            location = "Buenos Aires"
        )
        val domain = entity.toDomain()
        assertEquals("1122334455",                        domain.phone)
        assertEquals("https://example.com/avatar.png",   domain.avatarUrl)
        assertEquals("Buenos Aires",                      domain.location)
    }

    @Test
    fun `toDomain preserves createdAt`() {
        val entity = makeUserEntity().copy(createdAt = 9_999L)
        assertEquals(9_999L, entity.toDomain().createdAt)
    }

    // ── domain → entity ──────────────────────────────────────────────────────

    @Test
    fun `toEntity maps all base fields`() {
        val entity = makeUser(uid = "uid-42", name = "Lucía", email = "lucia@test.com").toEntity()
        assertEquals("uid-42",          entity.uid)
        assertEquals("Lucía",           entity.name)
        assertEquals("lucia@test.com",  entity.email)
    }

    @Test
    fun `toEntity maps null optional fields`() {
        val entity = makeUser().copy(phone = null, avatarUrl = null, location = null).toEntity()
        assertNull(entity.phone)
        assertNull(entity.avatarUrl)
        assertNull(entity.location)
    }

    // ── round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `round-trip entity to domain and back preserves all fields`() {
        val original = makeUserEntity(uid = "rt-1", name = "Marcos", email = "marcos@test.com")
            .copy(phone = "5550001111", avatarUrl = "url", location = "Rosario", createdAt = 5_000L)
        val result = original.toDomain().toEntity()
        assertEquals(original.uid,       result.uid)
        assertEquals(original.name,      result.name)
        assertEquals(original.email,     result.email)
        assertEquals(original.phone,     result.phone)
        assertEquals(original.avatarUrl, result.avatarUrl)
        assertEquals(original.location,  result.location)
        assertEquals(original.createdAt, result.createdAt)
    }
}
