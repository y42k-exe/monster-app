package com.example.data.repository

import com.example.data.local.MonsterDao
import com.example.data.model.MonsterDrink
import kotlinx.coroutines.flow.Flow

class MonsterRepository(private val monsterDao: MonsterDao) {
    val allDrinks: Flow<List<MonsterDrink>> = monsterDao.getAllDrinks()

    suspend fun insertDrink(drink: MonsterDrink) {
        monsterDao.insertDrink(drink)
    }

    suspend fun deleteDrink(drink: MonsterDrink) {
        monsterDao.deleteDrink(drink)
    }

    suspend fun deleteDrinkById(id: Int) {
        monsterDao.deleteDrinkById(id)
    }

    suspend fun clearAll() {
        monsterDao.clearAllDrinks()
    }
}
