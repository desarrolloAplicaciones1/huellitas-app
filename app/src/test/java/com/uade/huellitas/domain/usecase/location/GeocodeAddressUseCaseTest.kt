package com.uade.huellitas.domain.usecase.location

import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.repository.GeocodingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GeocodeAddressUseCaseTest {

    private lateinit var geocodingRepository: GeocodingRepository
    private lateinit var geocodeAddressUseCase: GeocodeAddressUseCase

    @Before
    fun setUp() {
        geocodingRepository = mockk()
        geocodeAddressUseCase = GeocodeAddressUseCase(geocodingRepository)
    }

    @Test
    fun `returns location when address is found`() = runTest {
        val location = Location(-34.6037, -58.3816, "Buenos Aires")
        coEvery { geocodingRepository.geocodeAddress("Buenos Aires") } returns location

        val result = geocodeAddressUseCase("Buenos Aires")

        assertEquals(location, result)
        coVerify(exactly = 1) { geocodingRepository.geocodeAddress("Buenos Aires") }
    }

    @Test
    fun `returns null when address is not found`() = runTest {
        coEvery { geocodingRepository.geocodeAddress(any()) } returns null

        val result = geocodeAddressUseCase("Dirección inexistente")

        assertNull(result)
    }

    @Test(expected = RuntimeException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { geocodingRepository.geocodeAddress(any()) } throws RuntimeException("Error de geocodificación")

        geocodeAddressUseCase("Buenos Aires")
    }
}
