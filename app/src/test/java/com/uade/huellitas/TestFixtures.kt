package com.uade.huellitas

import com.uade.huellitas.data.local.entity.AlertEntity
import com.uade.huellitas.data.local.entity.PetEntity
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.Pet
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.domain.model.User

fun makeAlert(
    id: String = "alert-1",
    ownerId: String = "user-1",
    type: AlertType = AlertType.LOST,
    status: AlertStatus = AlertStatus.ACTIVE,
    petType: PetType = PetType.DOG,
    petName: String = "Luna",
    photoUrls: List<String> = listOf("url1", "url2")
) = Alert(
    id = id,
    ownerId = ownerId,
    petId = null,
    type = type,
    status = status,
    petName = petName,
    petType = petType,
    description = "Se perdió cerca del parque",
    photoUrls = photoUrls,
    location = Location(-34.6037, -58.3816, "Buenos Aires"),
    contactPhone = "1122334455",
    createdAt = 1_000L,
    updatedAt = 2_000L
)

fun makeAlertEntity(
    id: String = "alert-1",
    ownerId: String = "user-1",
    type: String = "LOST",
    status: String = "ACTIVE",
    petType: String = "DOG",
    petName: String = "Luna",
    photoUrlsJson: String = "url1|url2"
) = AlertEntity(
    id = id,
    ownerId = ownerId,
    petId = null,
    type = type,
    status = status,
    petName = petName,
    petType = petType,
    breed = null,
    color = null,
    size = null,
    hasCollar = null,
    isCastrated = null,
    description = "Se perdió cerca del parque",
    photoUrlsJson = photoUrlsJson,
    latitude = -34.6037,
    longitude = -58.3816,
    address = "Buenos Aires",
    contactPhone = "1122334455",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    pendingSync = false
)

fun makePet(
    id: String = "pet-1",
    ownerId: String = "user-1",
    name: String = "Mochi",
    petType: PetType = PetType.CAT,
    photoUrls: List<String> = listOf("photo1")
) = Pet(
    id = id,
    ownerId = ownerId,
    name = name,
    petType = petType,
    breed = "Siamés",
    color = "Blanco",
    description = "Muy cariñoso",
    photoUrls = photoUrls,
    microchipId = null,
    createdAt = 1_000L
)

fun makePetEntity(
    id: String = "pet-1",
    ownerId: String = "user-1",
    name: String = "Mochi",
    petType: String = "CAT",
    photoUrlsJson: String = "photo1"
) = PetEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    petType = petType,
    breed = "Siamés",
    color = "Blanco",
    description = "Muy cariñoso",
    photoUrlsJson = photoUrlsJson,
    microchipId = null,
    createdAt = 1_000L
)

fun makeUser(
    uid: String = "user-1",
    name: String = "Ana García",
    email: String = "ana@example.com"
) = User(uid = uid, name = name, email = email)

fun makeUserEntity(
    uid: String = "user-1",
    name: String = "Ana García",
    email: String = "ana@example.com"
) = com.uade.huellitas.data.local.entity.UserEntity(
    uid = uid,
    name = name,
    email = email,
    phone = null,
    avatarUrl = null,
    location = null,
    createdAt = 1_000L
)
