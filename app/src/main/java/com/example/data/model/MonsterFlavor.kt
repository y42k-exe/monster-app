package com.example.data.model

data class MonsterFlavor(
    val id: String,
    val name: String,
    val brandColorHex: String,
    val accentColorHex: String,
    val caffeineMg: Int = 160,
    val volumeMl: Int = 500,
    val description: String
) {
    companion object {
        val PRESETS = listOf(
            MonsterFlavor(
                id = "original",
                name = "Original Green",
                brandColorHex = "#111111", // Dark black can background
                accentColorHex = "#39FF14", // Vibrant neon green claw
                description = "The classic, bold energy blend with that smooth, sweet kick."
            ),
            MonsterFlavor(
                id = "ultra_white",
                name = "Zero Ultra (White)",
                brandColorHex = "#EFEFEF", // Textured crisp silver/white
                accentColorHex = "#9CA3AF", // Silver grey
                description = "Zero sugar, light refreshing citrus flavor. Crisp and clean."
            ),
            MonsterFlavor(
                id = "mango_loco",
                name = "Mango Loco",
                brandColorHex = "#0C729E", // Day of the Dead vibrant blue
                accentColorHex = "#FFA500", // Vibrant Orange
                description = "Exotic juice blend with a crazy mango kick and party vibe."
            ),
            MonsterFlavor(
                id = "pipeline_punch",
                name = "Pipeline Punch",
                brandColorHex = "#FF5D73", // Coral Pink
                accentColorHex = "#FFE600", // Coral sun yellow
                description = "A perfect tropical mix of passion fruit, orange, and guava."
            ),
            MonsterFlavor(
                id = "aussie_lemonade",
                name = "Aussie Lemonade",
                brandColorHex = "#006C7A", // Deep Ocean Blue
                accentColorHex = "#F4D03F", // Golden Lemon Yellow
                description = "Classic tart style lemonade with an organic citrus twist."
            ),
            MonsterFlavor(
                id = "ultra_violet",
                name = "Ultra Violet",
                brandColorHex = "#4A154B", // Grape Purple
                accentColorHex = "#C39BD3", // Violet lilac
                description = "Zero sugar, light grape-citrus twist. Sweet and bubbly."
            ),
            MonsterFlavor(
                id = "ultra_paradise",
                name = "Ultra Paradise",
                brandColorHex = "#1B4F3E", // Dark Emerald Green
                accentColorHex = "#4AF2A1", // Tropical lime green
                description = "Zero sugar, crisp tropical blend with kiwi, lime, and cucumber."
            ),
            MonsterFlavor(
                id = "khaotic",
                name = "Khaotic Orange",
                brandColorHex = "#D35400", // Sunset Orange
                accentColorHex = "#F39C12", // Graffiti Yellow
                description = "Rebirth of the legendary juice blend with bold citrus flavor."
            ),
            MonsterFlavor(
                id = "pacific_punch",
                name = "Pacific Punch",
                brandColorHex = "#1C2833", // Deep Navy can background
                accentColorHex = "#E74C3C", // Retro tattoo red
                description = "Classic red fruit punch style flavor with a heavy kick."
            )
        )

        fun getById(id: String): MonsterFlavor {
            return PRESETS.firstOrNull { it.id == id } ?: PRESETS[0]
        }
    }
}
