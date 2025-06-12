package com.example.metaverseplanner

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var welcomeText: TextView
    private lateinit var dateText: TextView
    private lateinit var taskInput: EditText
    private lateinit var btnAddTask: Button
    private lateinit var taskList: ListView
    private lateinit var btnSettings: Button
    private lateinit var btnStats: Button
    private lateinit var btnInfo: Button

    private lateinit var taskAdapter: ArrayAdapter<String>
    private val tasks = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSharedPreferences()
        initializeViews()
        setupTaskList()
        loadTasksFromPrefs()
        updateWelcomeText()
        updateDateText()
        setupClickListeners()
        applyTheme() // Move applyTheme to the end after all views are initialized
    }

    private fun setupSharedPreferences() {
        sharedPrefs = getSharedPreferences("MetaversePlannerPrefs", MODE_PRIVATE)
    }

    private fun initializeViews() {
        welcomeText = findViewById(R.id.welcomeText)
        dateText = findViewById(R.id.dateText)
        taskInput = findViewById(R.id.taskInput)
        btnAddTask = findViewById(R.id.btnAddTask)
        taskList = findViewById(R.id.taskList)
        btnSettings = findViewById(R.id.btnSettings)
        btnStats = findViewById(R.id.btnStats)
        btnInfo = findViewById(R.id.btnInfo)
    }

    private fun applyTheme() {
        val contentView = findViewById<View>(android.R.id.content)
        ThemeHelper.applyThemeToActivity(this, contentView)

        // Apply theme to dividers if any
        ThemeHelper.applyToDividers(contentView, ThemeHelper.getThemeColors(this))

        // Update the ListView adapter to use themed color
        updateListViewTheme()
    }

    private fun updateListViewTheme() {
        val colors = ThemeHelper.getThemeColors(this)
        taskList.setBackgroundColor(colors.surface)

        // If you want to customize the list item appearance further,
        // you might need to create a custom adapter with themed layouts
    }

    private fun setupTaskList() {
        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
        taskList.adapter = taskAdapter

        // Long press to complete task
        taskList.setOnItemLongClickListener { _, _, position, _ ->
            showCompleteTaskDialog(position)
            true
        }
    }

    private fun loadTasksFromPrefs() {
        val tasksString = sharedPrefs.getString("saved_tasks", "")
        if (!tasksString.isNullOrEmpty()) {
            val savedTasks = tasksString.split("|||").filter { it.isNotBlank() }
            tasks.clear()
            tasks.addAll(savedTasks)
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun saveTasksToPrefs() {
        val tasksString = tasks.joinToString("|||")
        sharedPrefs.edit().putString("saved_tasks", tasksString).apply()
    }

    private fun updateWelcomeText() {
        val userName = sharedPrefs.getString("user_name", "Phantom Thief") ?: "Phantom Thief"
        val favoriteCharacter = sharedPrefs.getString("favorite_character", "Joker") ?: "Joker"
        welcomeText.text = "Welcome back, $userName!\n$favoriteCharacter believes in you!"
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateText.text = "Today: ${dateFormat.format(Date())}"
    }

    private fun setupClickListeners() {
        btnAddTask.setOnClickListener {
            val taskText = taskInput.text.toString().trim()
            if (taskText.isNotEmpty()) {
                addTask(taskText)
                taskInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        btnInfo.setOnClickListener {
            showInfoDialog()
        }
    }

    private fun addTask(taskText: String) {
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val taskWithTime = "[$timestamp] $taskText"

        tasks.add(taskWithTime)
        taskAdapter.notifyDataSetChanged()
        saveTasksToPrefs()

        // Update total tasks attempted counter
        val totalTasksAttempted = sharedPrefs.getInt("total_tasks_attempted", 0)
        sharedPrefs.edit().putInt("total_tasks_attempted", totalTasksAttempted + 1).apply()

        // Update weekly tasks attempted
        updateWeeklyTasksAttempted()

        // Update last active date for streak tracking
        updateLastActiveDate()

        Toast.makeText(this, "Task added to your agenda!", Toast.LENGTH_SHORT).show()
    }

    private fun showCompleteTaskDialog(position: Int) {
        val colors = ThemeHelper.getThemeColors(this)

        AlertDialog.Builder(this)
            .setTitle("Complete Task")
            .setMessage("Mark this task as completed?")
            .setPositiveButton("Complete") { _, _ ->
                completeTask(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun completeTask(position: Int) {
        if (position < tasks.size) {
            tasks.removeAt(position)
            taskAdapter.notifyDataSetChanged()
            saveTasksToPrefs()

            // Update completed tasks count
            val completedTasks = sharedPrefs.getInt("completed_tasks", 0)
            sharedPrefs.edit().putInt("completed_tasks", completedTasks + 1).apply()

            // Update weekly completed tasks
            updateWeeklyTasksCompleted()

            // Update last active date and streak
            updateLastActiveDate()
            updateStreak()

            Toast.makeText(this, "Task completed! Well done, Phantom Thief!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWeeklyTasksAttempted() {
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val savedWeek = sharedPrefs.getInt("current_week", -1)

        if (currentWeek != savedWeek) {
            // New week started, reset weekly stats
            sharedPrefs.edit()
                .putInt("current_week", currentWeek)
                .putInt("weekly_tasks_attempted", 1)
                .putInt("weekly_tasks_completed", 0)
                .apply()
        } else {
            // Same week, increment counter
            val weeklyAttempted = sharedPrefs.getInt("weekly_tasks_attempted", 0)
            sharedPrefs.edit().putInt("weekly_tasks_attempted", weeklyAttempted + 1).apply()
        }
    }

    private fun updateWeeklyTasksCompleted() {
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val savedWeek = sharedPrefs.getInt("current_week", -1)

        if (currentWeek != savedWeek) {
            // New week started, reset weekly stats
            sharedPrefs.edit()
                .putInt("current_week", currentWeek)
                .putInt("weekly_tasks_attempted", 0)
                .putInt("weekly_tasks_completed", 1)
                .apply()
        } else {
            // Same week, increment counter
            val weeklyCompleted = sharedPrefs.getInt("weekly_tasks_completed", 0)
            sharedPrefs.edit().putInt("weekly_tasks_completed", weeklyCompleted + 1).apply()
        }
    }

    private fun updateLastActiveDate() {
        sharedPrefs.edit().putLong("last_active_date", System.currentTimeMillis()).apply()
    }

    private fun updateStreak() {
        val lastActiveDate = sharedPrefs.getLong("last_active_date", 0)
        val currentStreak = sharedPrefs.getInt("streak_days", 0)

        val today = Calendar.getInstance()
        val lastActive = Calendar.getInstance().apply { timeInMillis = lastActiveDate }

        val newStreak = when {
            isSameDay(today, lastActive) -> currentStreak
            isConsecutiveDay(today, lastActive) -> currentStreak + 1
            else -> 1 // Start new streak
        }

        sharedPrefs.edit().putInt("streak_days", newStreak).apply()
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

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Metaverse Planner")
            .setMessage("""Welcome to the Metaverse Planner!
                
Inspired by Persona 5's time management system, this app helps you plan your daily activities like a true Phantom Thief!

Features:
• Add daily tasks and goals
• Track your progress
• Customize your experience in Settings
• View your stats and achievements

How to use:
1. Type your task in the input field
2. Tap 'ADD TO AGENDA' to add it to your list
3. Long press any task to mark it as complete
4. Visit Settings to personalize your experience
5. Check Stats to see your progress

Time to change the world, one task at a time!""")
            .setPositiveButton("Let's go!", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateWelcomeText() // Refresh welcome text in case settings changed
        applyTheme() // Reapply theme in case it changed in settings
    }
}