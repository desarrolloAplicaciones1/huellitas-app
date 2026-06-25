package com.uade.huellitas.data.mapper

import com.uade.huellitas.data.local.entity.AlertEntity
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.PetType

fun AlertEntity.toDomain(): Alert = Alert(
    id = id,
    ownerId = ownerId,
    ownerName = ownerName,
    petId = petId,
    type = AlertType.valueOf(type),
    status = AlertStatus.valueOf(status),
    petType = PetType.valueOf(petType),
    petName = petName,
    breed = breed,
    color = color,
    size = size,
    hasCollar = hasCollar,
    isCastrated = isCastrated,
    description = description,
    photoUrls = if (photoUrlsJson.isEmpty()) emptyList() else photoUrlsJson.split("|"),
    location = Location(latitude, longitude, address),
    contactPhone = contactPhone,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Alert.toEntity(pendingSync: Boolean = false): AlertEntity = AlertEntity(
    id = id,
    ownerId = ownerId,
    ownerName = ownerName,
    petId = petId,
    type = type.name,
    status = status.name,
    petType = petType.name,
    petName = petName,
    breed = breed,
    color = color,
    size = size,
    hasCollar = hasCollar,
    isCastrated = isCastrated,
    description = description,
    photoUrlsJson = photoUrls.joinToString("|"),
    latitude = location.latitude,
    longitude = location.longitude,
    address = location.address,
    contactPhone = contactPhone,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pendingSync = pendingSync
)
