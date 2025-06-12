package com.example.metaverseplanner

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var statsText: TextView
    private lateinit var achievementsText: TextView
    private lateinit var levelProgressBar: ProgressBar
    private lateinit var weeklyProgressBar: ProgressBar
    private lateinit var btnBackToMain: Button
    private lateinit var levelProgressText: TextView
    private lateinit var todaysTasksText: TextView
    private lateinit var streakDaysText: TextView
    private lateinit var efficiencyText: TextView
    private lateinit var weeklyProgressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_layout)

        setupSharedPreferences()
        initializeViews()
        applyTheme() // Apply theme to this activity
        loadAndDisplayStats()
        setupClickListeners()
    }

    private fun setupSharedPreferences() {
        sharedPrefs = getSharedPreferences("MetaversePlannerPrefs", MODE_PRIVATE)
    }

    private fun initializeViews() {
        statsText = findViewById(R.id.statsText)
        achievementsText = findViewById(R.id.achievementsText)
        levelProgressBar = findViewById(R.id.levelProgressBar)
        weeklyProgressBar = findViewById(R.id.weeklyProgressBar)
        btnBackToMain = findViewById(R.id.btnBackToMain)
        levelProgressText = findViewById(R.id.levelProgressText)
        todaysTasksText = findViewById(R.id.todaysTasksText)
        streakDaysText = findViewById(R.id.streakDaysText)
        efficiencyText = findViewById(R.id.efficiencyText)
        weeklyProgressText = findViewById(R.id.weeklyProgressText)
    }

    private fun applyTheme() {
        val contentView = findViewById<View>(android.R.id.content)
        ThemeHelper.applyThemeToActivity(this, contentView)
    }

    private fun loadAndDisplayStats() {
        val statsData = getStatsData()
        displayStats(statsData)
        displayAchievements(statsData)
        updateProgressBars(statsData)
        updateQuickStats(statsData)
    }

    private fun updateQuickStats(statsData: StatsData) {
        // Update Today's Tasks
        todaysTasksText.text = statsData.todaysTasks.toString()

        // Update Streak Days
        streakDaysText.text = statsData.streakDays.toString()

        // Update Efficiency
        val efficiencyPercentage = (statsData.efficiency * 100).toInt()
        efficiencyText.text = "${efficiencyPercentage}%"

        // Update Weekly Progress text based on efficiency
        val weeklyProgressMessage = when {
            efficiencyPercentage >= 90 -> "You're on fire this week! 🔥"
            efficiencyPercentage >= 70 -> "Great progress! 💫"
            efficiencyPercentage >= 50 -> "Keeping steady! 👍"
            efficiencyPercentage >= 30 -> "Keep pushing forward! 💪"
            else -> "Every step counts! 🌱"
        }
        weeklyProgressText.text = weeklyProgressMessage
    }

    private fun getStatsData(): StatsData {
        val userName = sharedPrefs.getString("user_name", "Phantom Thief") ?: "Phantom Thief"
        val favoriteCharacter = sharedPrefs.getString("favorite_character", "Joker") ?: "Joker"
        val completedTasks = sharedPrefs.getInt("completed_tasks", 0)
        val appTheme = sharedPrefs.getString("app_theme", "Dark Red (Classic)") ?: "Dark Red (Classic)"
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)

        val level = calculateLevel(completedTasks)
        val nextLevelTasks = calculateTasksForNextLevel(level)
        val todayDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        val achievements = getAchievements(completedTasks, level)

        // Calculate today's tasks (active tasks in the list)
        val todaysTasks = getTodaysTasks()

        // Calculate streak days
        val streakDays = calculateStreakDays()

        // Calculate efficiency (completed tasks / total tasks attempted)
        val efficiency = calculateEfficiency()

        // Calculate level progress percentage
        val levelProgress = calculateLevelProgress(completedTasks, level)

        return StatsData(
            userName = userName,
            favoriteCharacter = favoriteCharacter,
            completedTasks = completedTasks,
            appTheme = appTheme,
            notificationsEnabled = notificationsEnabled,
            level = level,
            nextLevelTasks = nextLevelTasks,
            todayDate = todayDate,
            achievements = achievements,
            todaysTasks = todaysTasks,
            streakDays = streakDays,
            efficiency = efficiency,
            levelProgress = levelProgress
        )
    }

    private fun displayStats(statsData: StatsData) {
        val statsContent = buildString {
            appendLine("📊 PHANTOM THIEF STATS 📊")
            appendLine()
            appendLine("Codename: ${statsData.userName}")
            appendLine("Partner: ${statsData.favoriteCharacter}")
            appendLine("Current Level: ${statsData.level}")
            appendLine()
            appendLine("Tasks Completed: ${statsData.completedTasks}")
            appendLine("Tasks until next level: ${statsData.nextLevelTasks}")
            appendLine()
            appendLine("Profile Settings:")
            appendLine("• Theme: ${statsData.appTheme}")
            appendLine("• Notifications: ${if (statsData.notificationsEnabled) "Enabled" else "Disabled"}")
            appendLine()
            appendLine("Last Updated: ${statsData.todayDate}")
        }

        statsText.text = statsContent
    }

    private fun displayAchievements(statsData: StatsData) {
        if (statsData.achievements.isEmpty()) {
            achievementsText.text = "🎯 Keep completing tasks to unlock achievements!"
        } else {
            val achievementsContent = buildString {
                statsData.achievements.forEach { achievement ->
                    appendLine(achievement)
                }
            }
            achievementsText.text = achievementsContent
        }
    }

    private fun updateProgressBars(statsData: StatsData) {
        // Level Progress
        levelProgressBar.progress = statsData.levelProgress
        levelProgressText.text = "${statsData.levelProgress}% to next level"

        // Weekly Progress (based on efficiency)
        val weeklyProgress = (statsData.efficiency * 100).toInt().coerceIn(0, 100)
        weeklyProgressBar.progress = weeklyProgress
    }

    private fun setupClickListeners() {
        btnBackToMain.setOnClickListener {
            finish()
        }
    }

    // Helper functions
    private fun calculateLevel(completedTasks: Int): Int {
        return when {
            completedTasks >= 100 -> 5
            completedTasks >= 50 -> 4
            completedTasks >= 25 -> 3
            completedTasks >= 10 -> 2
            completedTasks >= 1 -> 1
            else -> 0
        }
    }

    private fun calculateTasksForNextLevel(currentLevel: Int): Int {
        return when (currentLevel) {
            0 -> 1
            1 -> 10 - (sharedPrefs.getInt("completed_tasks", 0) - 1)
            2 -> 25 - (sharedPrefs.getInt("completed_tasks", 0) - 10)
            3 -> 50 - (sharedPrefs.getInt("completed_tasks", 0) - 25)
            4 -> 100 - (sharedPrefs.getInt("completed_tasks", 0) - 50)
            else -> 0 // Max level reached
        }.coerceAtLeast(0)
    }

    private fun getAchievements(completedTasks: Int, level: Int): List<String> {
        val achievements = mutableListOf<String>()

        // Task-based achievements
        when {
            completedTasks >= 100 -> achievements.add("🏆 Master Phantom Thief (100+ tasks)")
            completedTasks >= 50 -> achievements.add("⭐ Veteran Thief (50+ tasks)")
            completedTasks >= 25 -> achievements.add("🎯 Experienced Thief (25+ tasks)")
            completedTasks >= 10 -> achievements.add("✨ Rising Star (10+ tasks)")
            completedTasks >= 1 -> achievements.add("🌱 Beginner's Luck (1+ tasks)")
        }

        // Level-based achievements
        when {
            level >= 5 -> achievements.add("👑 Persona Master (Level 5)")
            level >= 3 -> achievements.add("🛡️ Confidant Ranker (Level 3)")
            level >= 1 -> achievements.add("🐾 Rookie Explorer (Level 1)")
        }

        return achievements
    }

    private fun getTodaysTasks(): Int {
        val tasksString = sharedPrefs.getString("saved_tasks", "") ?: ""
        return if (tasksString.isEmpty()) 0 else tasksString.split("|||").size
    }

    private fun calculateStreakDays(): Int {
        val lastActiveDate = sharedPrefs.getLong("last_active_date", 0)
        val streakCount = sharedPrefs.getInt("streak_days", 0)

        val today = Calendar.getInstance()
        val lastActive = Calendar.getInstance().apply { timeInMillis = lastActiveDate }

        // If last active was yesterday or today, maintain/increment streak
        return when {
            isSameDay(today, lastActive) -> streakCount
            isConsecutiveDay(today, lastActive) -> streakCount
            else -> 0 // Break the streak if more than a day has passed
        }
    }

    private fun calculateEfficiency(): Double {
        val completedTasks = sharedPrefs.getInt("completed_tasks", 0)
        val totalTasksAttempted = sharedPrefs.getInt("total_tasks_attempted", 0)
        return if (totalTasksAttempted == 0) 0.0
        else (completedTasks.toDouble() / totalTasksAttempted.toDouble())
    }

    private fun calculateLevelProgress(completedTasks: Int, level: Int): Int {
        return when {
            level >= 5 -> 100 // Max level
            level == 4 -> ((completedTasks - 50) * 100) / 50 // Level 4 to 5: 50 more tasks
            level == 3 -> ((completedTasks - 25) * 100) / 25 // Level 3 to 4: 25 more tasks
            level == 2 -> ((completedTasks - 10) * 100) / 15 // Level 2 to 3: 15 more tasks
            level == 1 -> ((completedTasks - 1) * 100) / 9   // Level 1 to 2: 9 more tasks
            else -> (completedTasks * 100) / 1 // Level 0 to 1: 1 task
        }.coerceIn(0, 100)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isConsecutiveDay(today: Calendar, lastActive: Calendar): Boolean {
        val yesterdayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterdayCal, lastActive)
    }

    override fun onResume() {
        super.onResume()
        applyTheme() // Reapply theme when returning to this activity
    }

    // Data class to hold stats information
    data class StatsData(
        val userName: String,
        val favoriteCharacter: String,
        val completedTasks: Int,
        val appTheme: String,
        val notificationsEnabled: Boolean,
        val level: Int,
        val nextLevelTasks: Int,
        val todayDate: String,
        val achievements: List<String>,
        val todaysTasks: Int,
        val streakDays: Int,
        val efficiency: Double,
        val levelProgress: Int
    )
}