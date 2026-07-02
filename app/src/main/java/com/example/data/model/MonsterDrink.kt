package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monster_drinks")
data class MonsterDrink(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val flavorId: String,
    val flavorName: String,
    val caffeineMg: Int,
    val volumeMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val location: String = "",
    val imagePath: String? = null
)

