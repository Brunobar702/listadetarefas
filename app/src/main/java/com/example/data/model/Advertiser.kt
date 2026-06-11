package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advertisers")
data class Advertiser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val slogan: String, // Slogan or title of the promotion
    val bannerText: String, // Description of the promotion
    val targetUrl: String, // Mock target website
    val colorHex: String, // Premium background color of the banner card in hexadecimal
    val iconName: String, // Brand layout symbol (e.g., "fastfood", "school", "fitness", "computer")
    val isActive: Boolean = true,
    val clickCount: Int = 0 // Track interest in full-fidelity simulation
)
