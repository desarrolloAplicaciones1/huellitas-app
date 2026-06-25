package com.uade.huellitas.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.Location
import com.uade.huellitas.domain.model.PetType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreAlertDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val alertsRef = db.collection("alerts")

    // One-shot fetch used by Room sync.
    suspend fun getActiveAlerts(): List<Alert> {
        val activeDocuments = alertsRef
            .whereEqualTo(FIELD_STATUS, AlertStatus.ACTIVE.name)
            .get()
            .await()
            .documents

        val allDocuments = alertsRef
            .get()
            .await()
            .documents

        return if (allDocuments.any { it.hasMissingStatus() }) {
            mapVisibleAlerts(allDocuments)
        } else {
            mapVisibleAlerts(activeDocuments)
        }
    }

    // Reactive listener for future realtime usage.
    fun observeActiveAlerts(): Flow<List<Alert>> = callbackFlow {
        val listener = alertsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            trySend(mapVisibleAlerts(snapshot?.documents.orEmpty()))
        }

        awaitClose { listener.remove() }
    }

    suspend fun saveAlert(alert: Alert) {
        alertsRef.document(alert.id).set(alert.toMap()).await()
    }

    suspend fun updateAlert(alert: Alert) {
        alertsRef.document(alert.id).set(alert.toMap()).await()
    }

    suspend fun deleteAlert(alertId: String) {
        alertsRef.document(alertId).delete().await()
    }

    private fun mapVisibleAlerts(documents: List<DocumentSnapshot>): List<Alert> {
        val useLegacyFallback = documents.any { it.hasMissingStatus() }

        return documents
            .filter { document ->
                if (useLegacyFallback) {
                    document.hasMissingStatus() || document.hasActiveStatus()
                } else {
                    document.hasActiveStatus()
                }
            }
            .mapNotNull { it.toAlert() }
    }

    private fun DocumentSnapshot.toAlert(): Alert? = runCatching {
        Alert(
            id = id,
            ownerId = getString("ownerId")!!,
            ownerName = getString("ownerName"),
            petId = getString("petId"),
            type = AlertType.valueOf(getString("type")!!),
            status = getAlertStatus(),
            petName = getString("petName")!!,
            petType = PetType.valueOf(getString("petType")!!),
            breed = getString("breed"),
            color = getString("color"),
            size = getString("size"),
            hasCollar = getBoolean("hasCollar"),
            isCastrated = getBoolean("isCastrated"),
            description = getString("description")!!,
            photoUrls = (get("photoUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            location = Location(
                latitude = getDouble("latitude")!!,
                longitude = getDouble("longitude")!!,
                address = getString("address")
            ),
            contactPhone = getString("contactPhone"),
            createdAt = getLong("createdAt")!!,
            updatedAt = getLong("updatedAt")!!
        )
    }.getOrNull()

    private fun DocumentSnapshot.getAlertStatus(): AlertStatus {
        val rawStatus = getString(FIELD_STATUS)
            ?.trim()
            ?.ifBlank { null }
            ?: AlertStatus.ACTIVE.name

        return AlertStatus.valueOf(rawStatus.uppercase())
    }

    private fun DocumentSnapshot.hasMissingStatus(): Boolean =
        getString(FIELD_STATUS).isNullOrBlank()

    private fun DocumentSnapshot.hasActiveStatus(): Boolean =
        getString(FIELD_STATUS)?.equals(AlertStatus.ACTIVE.name, ignoreCase = true) == true

    private fun Alert.toMap(): Map<String, Any?> = mapOf(
        "ownerId" to ownerId,
        "ownerName" to ownerName,
        "petId" to petId,
        "type" to type.name,
        FIELD_STATUS to status.name,
        "petName" to petName,
        "petType" to petType.name,
        "breed" to breed,
        "color" to color,
        "size" to size,
        "hasCollar" to hasCollar,
        "isCastrated" to isCastrated,
        "description" to description,
        "photoUrls" to photoUrls,
        "latitude" to location.latitude,
        "longitude" to location.longitude,
        "address" to location.address,
        "contactPhone" to contactPhone,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        private const val FIELD_STATUS = "status"
    }
}
