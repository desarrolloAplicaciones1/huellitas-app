package com.uade.huellitas.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.uade.huellitas.domain.model.Pet
import com.uade.huellitas.domain.model.PetType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePetDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val petsRef = db.collection("pets")

    fun getPetsByOwner(ownerId: String): Flow<List<Pet>> = callbackFlow {
        val listener = petsRef
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { it.toPet() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun savePet(pet: Pet) {
        petsRef.document(pet.id).set(pet.toMap()).await()
    }

    suspend fun deletePet(petId: String) {
        petsRef.document(petId).delete().await()
    }

    private fun Pet.toMap(): Map<String, Any?> = mapOf(
        "ownerId" to ownerId,
        "name" to name,
        "petType" to petType.name,
        "breed" to breed,
        "color" to color,
        "description" to description,
        "photoUrls" to photoUrls,
        "microchipId" to microchipId,
        "createdAt" to createdAt
    )

    private fun DocumentSnapshot.toPet(): Pet? = runCatching {
        Pet(
            id = id,
            ownerId = getString("ownerId")!!,
            name = getString("name")!!,
            petType = PetType.valueOf(getString("petType")!!),
            breed = getString("breed"),
            color = getString("color"),
            description = getString("description"),
            photoUrls = (get("photoUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            microchipId = getString("microchipId"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    }.getOrNull()
}
