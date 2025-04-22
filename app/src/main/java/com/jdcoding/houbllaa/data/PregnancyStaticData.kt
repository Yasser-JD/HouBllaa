package com.jdcoding.houbllaa.data

/**
 * Static data for pregnancy information by week
 * Contains size comparisons, height, weight measurements for each week
 */
data class WeeklyBabyMetrics(
    val week: Int,
    val sizeComparison: String,
    val heightCm: Double,
    val weightGrams: Double,
    val daysLeftToTermFull: Int // Days left to full term (40 weeks)
)

object PregnancyStaticData {
    
    private val weeklyBabyMetrics = listOf(
        WeeklyBabyMetrics(1, "smaller than a poppy seed", 0.0, 0.0, 273),
        WeeklyBabyMetrics(2, "a poppy seed", 0.0, 0.0, 266),
        WeeklyBabyMetrics(3, "a sesame seed", 0.0, 0.0, 259),
        WeeklyBabyMetrics(4, "a poppyseed", 0.1, 0.4, 252),
        WeeklyBabyMetrics(5, "an apple seed", 0.4, 1.3, 245),
        WeeklyBabyMetrics(6, "a sweet pea", 0.6, 3.0, 238),
        WeeklyBabyMetrics(7, "a blueberry", 1.3, 5.0, 231),
        WeeklyBabyMetrics(8, "a kidney bean", 1.6, 7.0, 224),
        WeeklyBabyMetrics(9, "a grape", 2.3, 10.0, 217),
        WeeklyBabyMetrics(10, "a kumquat", 3.1, 14.0, 210),
        WeeklyBabyMetrics(11, "a fig", 4.1, 18.0, 203),
        WeeklyBabyMetrics(12, "a lime", 5.4, 25.0, 196),
        WeeklyBabyMetrics(13, "a peapod", 7.4, 33.0, 189),
        WeeklyBabyMetrics(14, "a lemon", 8.7, 45.0, 182),
        WeeklyBabyMetrics(15, "an apple", 10.1, 78.0, 175),
        WeeklyBabyMetrics(16, "a pear", 11.6, 110.0, 168),
        WeeklyBabyMetrics(17, "a pomegranate", 13.0, 150.0, 161),
        WeeklyBabyMetrics(18, "a bell pepper", 14.2, 190.0, 154),
        WeeklyBabyMetrics(19, "a mango", 15.3, 240.0, 147),
        WeeklyBabyMetrics(20, "a banana", 16.5, 300.0, 140),
        WeeklyBabyMetrics(21, "a carrot", 26.7, 360.0, 133),
        WeeklyBabyMetrics(22, "a spaghetti squash", 27.8, 430.0, 126),
        WeeklyBabyMetrics(23, "a large mango", 28.9, 500.0, 119),
        WeeklyBabyMetrics(24, "an ear of corn", 30.0, 600.0, 112),
        WeeklyBabyMetrics(25, "a rutabaga", 34.6, 660.0, 105),
        WeeklyBabyMetrics(26, "a scallion", 35.6, 760.0, 98),
        WeeklyBabyMetrics(27, "a cauliflower", 36.6, 875.0, 91),
        WeeklyBabyMetrics(28, "an eggplant", 37.6, 1000.0, 84),
        WeeklyBabyMetrics(29, "a butternut squash", 38.6, 1150.0, 77),
        WeeklyBabyMetrics(30, "a large cabbage", 39.9, 1320.0, 70),
        WeeklyBabyMetrics(31, "a coconut", 41.1, 1500.0, 63),
        WeeklyBabyMetrics(32, "a jicama", 42.4, 1700.0, 56),
        WeeklyBabyMetrics(33, "a pineapple", 43.7, 1900.0, 49),
        WeeklyBabyMetrics(34, "a cantaloupe", 45.0, 2150.0, 42),
        WeeklyBabyMetrics(35, "a honeydew melon", 46.2, 2400.0, 35),
        WeeklyBabyMetrics(36, "a head of romaine lettuce", 47.4, 2650.0, 28),
        WeeklyBabyMetrics(37, "a bunch of Swiss chard", 48.6, 2900.0, 21),
        WeeklyBabyMetrics(38, "a leek", 49.8, 3100.0, 14),
        WeeklyBabyMetrics(39, "a mini watermelon", 50.7, 3300.0, 7),
        WeeklyBabyMetrics(40, "a small pumpkin", 51.2, 3400.0, 0),
        WeeklyBabyMetrics(41, "a watermelon", 51.5, 3500.0, 0),
        WeeklyBabyMetrics(42, "a watermelon", 51.7, 3600.0, 0)
    )
    
    /**
     * Get metrics for a specific week of pregnancy
     * @param week The pregnancy week (1-42)
     * @return WeeklyBabyMetrics for the specified week, or null if the week is invalid
     */
    fun getMetricsForWeek(week: Int): WeeklyBabyMetrics? {
        return if (week in 1..42) {
            weeklyBabyMetrics.find { it.week == week }
        } else {
            null
        }
    }
}
