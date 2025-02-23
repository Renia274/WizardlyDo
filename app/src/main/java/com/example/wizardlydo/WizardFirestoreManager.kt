package com.example.wizardlydo

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class WizardFirestoreManager(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val wizardsCollection = db.collection("wizards")

    suspend fun createWizardProfile(profile: WizardProfile): Result<Unit> {
        return try {
            wizardsCollection.document(profile.userId)
                .set(profile.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWizardProfile(userId: String): Result<WizardProfile?> {
        return try {
            val document = wizardsCollection.document(userId).get().await()
            Result.success(document.data?.let { WizardProfile.fromFirestore(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWizardLastLogin(userId: String): Result<Unit> {
        return try {
            wizardsCollection.document(userId)
                .update("lastLogin", FieldValue.serverTimestamp())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWizardExperience(userId: String, experience: Int): Result<Unit> {
        return try {
            wizardsCollection.document(userId)
                .update("experience", experience)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWizardLevel(userId: String, level: Int): Result<Unit> {
        return try {
            wizardsCollection.document(userId)
                .update(mapOf(
                    "level" to level,
                    "lastLogin" to FieldValue.serverTimestamp()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}