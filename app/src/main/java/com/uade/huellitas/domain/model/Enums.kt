package com.uade.huellitas.domain.model

enum class AlertType   { LOST, FOUND }
enum class AlertStatus { ACTIVE, RESOLVED }
enum class PetType     { DOG, CAT, OTHER }

fun AlertType.toLabel(): String = when (this) {
    AlertType.LOST  -> "Perdido"
    AlertType.FOUND -> "Encontrado"
}

fun AlertStatus.toLabel(): String = when (this) {
    AlertStatus.ACTIVE   -> "Activo"
    AlertStatus.RESOLVED -> "Resuelto"
}

fun PetType.toLabel(): String = when (this) {
    PetType.DOG   -> "Perro"
    PetType.CAT   -> "Gato"
    PetType.OTHER -> "Otro"
}
