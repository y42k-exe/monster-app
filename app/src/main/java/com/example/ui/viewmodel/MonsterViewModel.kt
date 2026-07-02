package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MonsterDatabase
import com.example.data.model.MonsterDrink
import com.example.data.model.MonsterFlavor
import com.example.data.repository.MonsterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String, // name of icon representing achievement
    val isUnlocked: Boolean,
    val progress: Float, // 0.0 to 1.0
    val progressText: String // e.g. "3/10"
)

data class MonsterUiState(
    val drinks: List<MonsterDrink> = emptyList(),
    val totalCans: Int = 0,
    val totalCaffeineMg: Int = 0,
    val caffeineTodayMg: Int = 0,
    val cansToday: Int = 0,
    val streakDays: Int = 0,
    val isCaffeineOverLimit: Boolean = false,
    val mostPopularFlavor: String = "None",
    val flavorCounts: Map<String, Int> = emptyMap(),
    val weeklyHistory: List<WeeklyBarData> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

data class WeeklyBarData(
    val dayName: String,
    val count: Int,
    val dateLabel: String
)

class MonsterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MonsterRepository
    
    // UI selection state for custom additions
    private val _selectedFlavor = MutableStateFlow(MonsterFlavor.PRESETS[0])
    val selectedFlavor = _selectedFlavor.asStateFlow()

    init {
        val database = MonsterDatabase.getDatabase(application)
        repository = MonsterRepository(database.monsterDao())
    }

    // Main raw state from Room DB
    val allDrinks: StateFlow<List<MonsterDrink>> = repository.allDrinks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined state computing all analytics reactively in the background
    val uiState: StateFlow<MonsterUiState> = allDrinks
        .map { drinksList -> computeUiState(drinksList) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonsterUiState()
        )

    fun selectFlavor(flavor: MonsterFlavor) {
        _selectedFlavor.value = flavor
    }

    fun logDrink(
        flavor: MonsterFlavor,
        notes: String = "",
        location: String = "",
        imagePath: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val drink = MonsterDrink(
                flavorId = flavor.id,
                flavorName = flavor.name,
                caffeineMg = flavor.caffeineMg,
                volumeMl = flavor.volumeMl,
                notes = notes,
                location = location,
                imagePath = imagePath,
                timestamp = timestamp
            )
            repository.insertDrink(drink)
        }
    }

    fun deleteDrink(drink: MonsterDrink) {
        viewModelScope.launch {
            repository.deleteDrink(drink)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun computeUiState(drinks: List<MonsterDrink>): MonsterUiState {
        val totalCans = drinks.size
        val totalCaffeine = drinks.sumOf { it.caffeineMg }

        // Filter and calculate for today
        val todayStart = getStartOfToday()
        val todayDrinks = drinks.filter { it.timestamp >= todayStart }
        val cansToday = todayDrinks.size
        val caffeineToday = todayDrinks.sumOf { it.caffeineMg }
        val isCaffeineOverLimit = caffeineToday > 400 // Safe daily limit is 400mg

        // Most popular flavor
        val flavorCounts = drinks.groupingBy { it.flavorId }.eachCount()
        val mostPopularFlavorId = flavorCounts.maxByOrNull { it.value }?.key
        val mostPopularName = mostPopularFlavorId?.let { id ->
            MonsterFlavor.getById(id).name
        } ?: "None"

        // Weekly history (last 7 days)
        val weeklyHistory = calculateWeeklyHistory(drinks)

        // Streak Days
        val streak = calculateStreak(drinks)

        // Calculate Achievements
        val uniqueFlavors = drinks.map { it.flavorId }.distinct()
        val legendaryFlavors = listOf("original", "mango_loco", "pipeline_punch")
        val hasLegendary = drinks.any { it.flavorId in legendaryFlavors }

        val achievements = listOf(
            Achievement(
                id = "first_monster",
                title = "First Monster",
                description = "Log your first energy boost of the series.",
                iconName = "star",
                isUnlocked = totalCans >= 1,
                progress = if (totalCans >= 1) 1.0f else 0.0f,
                progressText = if (totalCans >= 1) "1/1" else "0/1"
            ),
            Achievement(
                id = "ten_monsters",
                title = "10 Monsters Consumed",
                description = "Build the core energy collection by logging 10 cans.",
                iconName = "sports_score",
                isUnlocked = totalCans >= 10,
                progress = (totalCans.coerceAtMost(10) / 10.0f),
                progressText = "${totalCans.coerceAtMost(10)}/10"
            ),
            Achievement(
                id = "legendary_monster",
                title = "Legendary Flavor",
                description = "Log an Original, Mango Loco, or Pipeline Punch classic can.",
                iconName = "workspace_premium",
                isUnlocked = hasLegendary,
                progress = if (hasLegendary) 1.0f else 0.0f,
                progressText = if (hasLegendary) "1/1" else "0/1"
            ),
            Achievement(
                id = "five_different",
                title = "Monster Sommelier",
                description = "Log at least 5 unique varieties of Monster Energy.",
                iconName = "palette",
                isUnlocked = uniqueFlavors.size >= 5,
                progress = (uniqueFlavors.size.coerceAtMost(5) / 5.0f),
                progressText = "${uniqueFlavors.size.coerceAtMost(5)}/5"
            )
        )

        return MonsterUiState(
            drinks = drinks,
            totalCans = totalCans,
            totalCaffeineMg = totalCaffeine,
            caffeineTodayMg = caffeineToday,
            cansToday = cansToday,
            streakDays = streak,
            isCaffeineOverLimit = isCaffeineOverLimit,
            mostPopularFlavor = mostPopularName,
            flavorCounts = flavorCounts,
            weeklyHistory = weeklyHistory,
            achievements = achievements
        )
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun calculateStreak(drinks: List<MonsterDrink>): Int {
        if (drinks.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uniqueDays = drinks.map { dateFormat.format(Date(it.timestamp)) }.toSet()

        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(calendar.time)

        // Reset calendar back to today
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        // Streak can only be active if user drank today or yesterday
        if (!uniqueDays.contains(todayStr) && !uniqueDays.contains(yesterdayStr)) {
            return 0
        }

        var currentStreak = 0
        // Start counting back from today or yesterday
        val startingDate = if (uniqueDays.contains(todayStr)) todayStr else yesterdayStr
        
        if (startingDate == yesterdayStr) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val checkDateStr = dateFormat.format(calendar.time)
            if (uniqueDays.contains(checkDateStr)) {
                currentStreak++
                calendar.add(Calendar.DAY_OF_YEAR, -1) // Go to previous day
            } else {
                break
            }
        }

        return currentStreak
    }

    private fun calculateWeeklyHistory(drinks: List<MonsterDrink>): List<WeeklyBarData> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault()) // e.g. "Mon"
        val labelFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        val calendar = Calendar.getInstance()
        val datesList = mutableListOf<Calendar>()
        
        // Populate the last 7 calendar days starting from 6 days ago to today
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val calCopy = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
            }
            datesList.add(calCopy)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Group drinks by day formatted string
        val drinksByDayString = drinks.groupBy { dateFormat.format(Date(it.timestamp)) }

        return datesList.map { cal ->
            val dayStr = dateFormat.format(cal.time)
            val dayName = dayNameFormat.format(cal.time)
            val dateLabel = labelFormat.format(cal.time)
            val dayDrinks = drinksByDayString[dayStr] ?: emptyList()
            WeeklyBarData(
                dayName = dayName,
                count = dayDrinks.size,
                dateLabel = dateLabel
            )
        }
    }
}
