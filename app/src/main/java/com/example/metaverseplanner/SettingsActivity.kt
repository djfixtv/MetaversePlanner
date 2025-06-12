package com.example.metaverseplanner

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat // Import SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var characterSpinner: Spinner
    private lateinit var themeSpinner: Spinner
    private lateinit var notificationSwitch: SwitchCompat // Change to SwitchCompat
    private lateinit var sharedPrefs: SharedPreferences

    private val characters = arrayOf(
        "Joker", "Ryuji", "Ann", "Yusuke", "Makoto",
        "Futaba", "Haru", "Akechi", "Morgana"
    )

    private val themes = arrayOf(
        "Dark Red (Classic)", "Blue (Calm)", "Purple (Mystery)", "Green (Nature)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        initializeViews()
        setupSharedPreferences()
        applyTheme() // Apply theme to this activity
        setupSpinners()
        loadPreferences()
        setupButtons()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        characterSpinner = findViewById(R.id.characterSpinner)
        themeSpinner = findViewById(R.id.themeSpinner)
        notificationSwitch = findViewById(R.id.notificationSwitch) // This R.id.notificationSwitch should correspond to a SwitchCompat in your settings_layout.xml
    }

    private fun setupSharedPreferences() {
        sharedPrefs = getSharedPreferences("MetaversePlannerPrefs", MODE_PRIVATE)
    }

    private fun applyTheme() {
        val contentView = findViewById<View>(android.R.id.content)
        ThemeHelper.applyThemeToActivity(this, contentView)
    }

    private fun setupSpinners() {
        val colors = ThemeHelper.getThemeColors(this)

        // Character spinner
        val characterAdapter = ArrayAdapter(this, R.layout.spinner_item, characters)
        characterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        characterSpinner.adapter = characterAdapter

        // Theme spinner
        val themeAdapter = ArrayAdapter(this, R.layout.spinner_item, themes)
        themeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter

        // Theme change listener will be handled by ThemeHelper
    }

    private fun loadPreferences() {
        // Load saved preferences
        nameInput.setText(sharedPrefs.getString("user_name", "Phantom Thief"))

        val favoriteCharacter = sharedPrefs.getString("favorite_character", "Joker")
        val characterIndex = characters.indexOf(favoriteCharacter)
        if (characterIndex != -1) {
            characterSpinner.setSelection(characterIndex)
        }

        val theme = sharedPrefs.getString("app_theme", "Dark Red (Classic)")
        val themeIndex = themes.indexOf(theme)
        if (themeIndex != -1) {
            themeSpinner.setSelection(themeIndex)
        }

        notificationSwitch.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            savePreferences()
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetPreferences()
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun savePreferences() {
        val editor = sharedPrefs.edit()

        editor.putString("user_name", nameInput.text.toString().trim().ifEmpty { "Phantom Thief" })
        editor.putString("favorite_character", characters[characterSpinner.selectedItemPosition])
        editor.putString("app_theme", themes[themeSpinner.selectedItemPosition])
        editor.putBoolean("notifications_enabled", notificationSwitch.isChecked)

        editor.apply()

        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()

        // Reapply theme after saving
        applyTheme()
    }

    private fun resetPreferences() {
        // Reset to defaults
        nameInput.setText("Phantom Thief")
        characterSpinner.setSelection(0) // Joker
        themeSpinner.setSelection(0) // Dark Red
        notificationSwitch.isChecked = true

        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        applyTheme() // Reapply theme when returning to this activity
    }
}