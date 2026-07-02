package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MonsterDrink
import kotlinx.coroutines.flow.Flow

@Dao
interface MonsterDao {
    @Query("SELECT * FROM monster_drinks ORDER BY timestamp DESC")
    fun getAllDrinks(): Flow<List<MonsterDrink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: MonsterDrink): Long

    @Delete
    suspend fun deleteDrink(drink: MonsterDrink)

    @Query("DELETE FROM monster_drinks WHERE id = :id")
    suspend fun deleteDrinkById(id: Int)

    @Query("DELETE FROM monster_drinks")
    suspend fun clearAllDrinks()
}
