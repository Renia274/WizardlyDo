package com.example.wizardlydo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

data class WizardProfile(
    val userId: String = "",
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val wizardName: String = "",
    val email: String = "",
    val signInProvider: SignInProvider = SignInProvider.EMAIL,
    val level: Int = 1,
    val experience: Int = 0,
    val spells: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    @ServerTimestamp val joinDate: Timestamp? = null,
    @ServerTimestamp val lastLogin: Timestamp? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "wizardClass" to wizardClass.name,
        "wizardName" to wizardName,
        "email" to email,
        "signInProvider" to signInProvider.name,
        "level" to level,
        "experience" to experience,
        "spells" to spells,
        "achievements" to achievements,
        "joinDate" to joinDate,
        "lastLogin" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromFirestore(map: Map<String, Any>): WizardProfile {
            return WizardProfile(
                userId = map["userId"] as? String ?: "",
                wizardClass = WizardClass.valueOf(map["wizardClass"] as? String ?: WizardClass.MYSTWEAVER.name),
                wizardName = map["wizardName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                signInProvider = SignInProvider.valueOf(map["signInProvider"] as? String ?: SignInProvider.EMAIL.name),
                level = (map["level"] as? Long)?.toInt() ?: 1,
                experience = (map["experience"] as? Long)?.toInt() ?: 0,
                spells = map["spells"] as? List<String> ?: emptyList(),
                achievements = map["achievements"] as? List<String> ?: emptyList(),
                joinDate = map["joinDate"] as? Timestamp,
                lastLogin = map["lastLogin"] as? Timestamp
            )
        }
    }
}