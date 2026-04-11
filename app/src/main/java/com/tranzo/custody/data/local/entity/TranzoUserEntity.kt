package com.tranzo.custody.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class TranzoUserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val dob: String?,
    val kycStatus: String? = "PENDING",
    val onboardingComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
