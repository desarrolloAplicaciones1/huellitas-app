package com.uade.huellitas.domain.usecase.alert

import com.uade.huellitas.domain.repository.AlertRepository

class PushPendingAlertsUseCase(private val alertRepository: AlertRepository) {
    suspend operator fun invoke() = alertRepository.syncPendingToFirestore()
}
