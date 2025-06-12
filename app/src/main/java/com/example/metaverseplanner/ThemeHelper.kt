package com.example.metaverseplanner

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat

object ThemeHelper {

    data class ThemeColors(
        val primary: Int,
        val background: Int,
        val surface: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int,
        val divider: Int,
        val cardBackground: Int,
        val warning: Int
    )

    fun getThemeColors(context: Context): ThemeColors {
        val sharedPrefs = context.getSharedPreferences("MetaversePlannerPrefs", Context.MODE_PRIVATE)
        val theme = sharedPrefs.getString("app_theme", "Dark Red (Classic)") ?: "Dark Red (Classic)"

        return when (theme) {
            "Dark Red (Classic)" -> ThemeColors(
                primary = android.graphics.Color.parseColor("#dc143c"),
                background = android.graphics.Color.parseColor("#1a0d0d"),
                surface = android.graphics.Color.parseColor("#2d1a1a"),
                textPrimary = android.graphics.Color.parseColor("#ffffff"),
                textSecondary = android.graphics.Color.parseColor("#b0b0b0"),
                accent = android.graphics.Color.parseColor("#ff4757"),
                divider = android.graphics.Color.parseColor("#dc143c"),
                cardBackground = android.graphics.Color.parseColor("#2d1a1a"),
                warning = android.graphics.Color.parseColor("#ff6b6b")
            )
            "Blue (Calm)" -> ThemeColors(
                primary = android.graphics.Color.parseColor("#4169e1"),
                background = android.graphics.Color.parseColor("#0d1421"),
                surface = android.graphics.Color.parseColor("#1a2332"),
                textPrimary = android.graphics.Color.parseColor("#ffffff"),
                textSecondary = android.graphics.Color.parseColor("#a0b4d6"),
                accent = android.graphics.Color.parseColor("#5865f2"),
                divider = android.graphics.Color.parseColor("#4169e1"),
                cardBackground = android.graphics.Color.parseColor("#1a2332"),
                warning = android.graphics.Color.parseColor("#74b9ff")
            )
            "Purple (Mystery)" -> ThemeColors(
                primary = android.graphics.Color.parseColor("#8a2be2"),
                background = android.graphics.Color.parseColor("#1a0d21"),
                surface = android.graphics.Color.parseColor("#2d1a32"),
                textPrimary = android.graphics.Color.parseColor("#ffffff"),
                textSecondary = android.graphics.Color.parseColor("#c5a3d6"),
                accent = android.graphics.Color.parseColor("#9b59b6"),
                divider = android.graphics.Color.parseColor("#8a2be2"),
                cardBackground = android.graphics.Color.parseColor("#2d1a32"),
                warning = android.graphics.Color.parseColor("#a29bfe")
            )
            "Green (Nature)" -> ThemeColors(
                primary = android.graphics.Color.parseColor("#228b22"),
                background = android.graphics.Color.parseColor("#0d1a0d"),
                surface = android.graphics.Color.parseColor("#1a2d1a"),
                textPrimary = android.graphics.Color.parseColor("#ffffff"),
                textSecondary = android.graphics.Color.parseColor("#a8d4a8"),
                accent = android.graphics.Color.parseColor("#32cd32"),
                divider = android.graphics.Color.parseColor("#228b22"),
                cardBackground = android.graphics.Color.parseColor("#1a2d1a"),
                warning = android.graphics.Color.parseColor("#55a3ff")
            )
            else -> ThemeColors(
                primary = android.graphics.Color.parseColor("#dc143c"),
                background = android.graphics.Color.parseColor("#1a0d0d"),
                surface = android.graphics.Color.parseColor("#2d1a1a"),
                textPrimary = android.graphics.Color.parseColor("#ffffff"),
                textSecondary = android.graphics.Color.parseColor("#b0b0b0"),
                accent = android.graphics.Color.parseColor("#ff4757"),
                divider = android.graphics.Color.parseColor("#dc143c"),
                cardBackground = android.graphics.Color.parseColor("#2d1a1a"),
                warning = android.graphics.Color.parseColor("#ff6b6b")
            )
        }
    }

    /**
     * Apply theme to any activity - call this in onCreate after setContentView
     */
    fun applyThemeToActivity(context: Context, rootView: View) {
        val colors = getThemeColors(context)

        // First, find the ScrollView which is our actual root container
        val scrollView = findScrollView(rootView)
        if (scrollView != null) {
            scrollView.setBackgroundColor(colors.background)
        }

        // Now apply theme recursively to all views
        applyThemeRecursively(rootView, colors)
    }

    private fun findScrollView(view: View): ScrollView? {
        if (view is ScrollView) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findScrollView(view.getChildAt(i))
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    /**
     * Recursively apply theme to all views in the hierarchy
     */
    private fun applyThemeRecursively(view: View, colors: ThemeColors) {
        when {
            view is ViewGroup -> {
                // Apply background only if it's a specific container or has a background
                if (view.background != null || view.tag == "card") {
                    if (view !is ScrollView) { // Don't set surface color on ScrollView
                        view.setBackgroundColor(colors.surface)
                    }
                }
                // Apply to all children
                for (i in 0 until view.childCount) {
                    applyThemeRecursively(view.getChildAt(i), colors)
                }
            }
            view is EditText -> applyToEditText(view, colors)
            view is Button -> applyToButton(view, colors)
            view is TextView -> applyToTextView(view, colors)
            view is SwitchCompat -> applyToSwitchCompat(view, colors)
            view is Spinner -> applyToSpinner(view, colors)
            view is ListView -> applyToListView(view, colors)
            view is ProgressBar -> applyToProgressBar(view, colors)
        }
    }

    private fun applyToTextView(textView: TextView, colors: ThemeColors) {
        when {
            // Section headers (tagged or by content pattern)
            textView.tag == "section_header" -> {
                textView.setTextColor(colors.primary)
            }
            // Content with emojis (section headers)
            textView.text.toString().let { text ->
                text.contains("📝") || text.contains("⚡") || text.contains("🎭") ||
                        text.contains("👥") || text.contains("🎨") || text.contains("📊") ||
                        text.contains("🏆") || text.contains("📈")
            } -> {
                textView.setTextColor(colors.primary)
            }
            // Main headers (larger text, titles)
            textView.textSize >= 18f -> {
                textView.setTextColor(colors.primary)
            }
            // Achievement/special text
            textView.text.toString().let { text ->
                text.contains("🏆") || text.contains("⭐")
            } -> {
                textView.setTextColor(android.graphics.Color.parseColor("#ffd700"))
            }
            // Secondary text (hints, descriptions, smaller text)
            textView.textSize <= 12f || textView.text.toString().let { text ->
                text.contains("💡") || text.contains("Today:")
            } -> {
                textView.setTextColor(colors.textSecondary)
            }
            // Regular text
            else -> {
                textView.setTextColor(colors.textPrimary)
            }
        }
    }

    private fun applyToEditText(editText: EditText, colors: ThemeColors) {
        editText.setTextColor(colors.textPrimary)
        editText.setHintTextColor(colors.textSecondary)
        val background = createInputBackground(colors)
        editText.background = background
    }

    private fun applyToButton(button: Button, colors: ThemeColors) {
        val buttonText = button.text.toString().uppercase()

        when {
            buttonText.contains("SAVE") || buttonText.contains("ADD") -> {
                button.background = createButtonBackground(colors.primary)
                button.setTextColor(colors.background) // Use background color for contrast
            }
            buttonText.contains("RESET") || buttonText.contains("WARNING") -> {
                button.background = createButtonBackground(colors.warning)
                button.setTextColor(colors.background)
            }
            buttonText.contains("INFO") || buttonText.contains("ABOUT") -> {
                button.background = createButtonBackground(colors.accent)
                button.setTextColor(colors.textPrimary)
            }
            buttonText.contains("BACK") || buttonText.contains("RETURN") || buttonText.contains("HIDEOUT") -> {
                button.background = createButtonBackground(colors.surface)
                button.setTextColor(colors.textPrimary)
            }
            else -> {
                button.background = createButtonBackground(colors.surface)
                button.setTextColor(colors.textPrimary)
            }
        }
    }

    private fun applyToSwitchCompat(switchCompat: SwitchCompat, colors: ThemeColors) {
        // Apply theme color to SwitchCompat
        switchCompat.thumbTintList = android.content.res.ColorStateList.valueOf(colors.primary)
        switchCompat.trackTintList = android.content.res.ColorStateList.valueOf(colors.surface)
    }

    private fun applyToSpinner(spinner: Spinner, colors: ThemeColors) {
        // Set the background
        spinner.background = createInputBackground(colors)

        // Change the text color for the selected item
        if (spinner.selectedView is TextView) {
            (spinner.selectedView as TextView).setTextColor(colors.textPrimary)
        }

        // Set up a listener to style newly selected items
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    if (it is TextView) {
                        it.setTextColor(colors.textPrimary)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Try to set the dropdown text color by getting the adapter
        (spinner.adapter as? ArrayAdapter<*>)?.let { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Force the adapter to refresh
            spinner.adapter = adapter
        }
    }

    private fun applyToListView(listView: ListView, colors: ThemeColors) {
        listView.setBackgroundColor(colors.surface)
        listView.divider = android.graphics.drawable.ColorDrawable(colors.divider)
        listView.dividerHeight = 1
    }

    private fun applyToProgressBar(progressBar: ProgressBar, colors: ThemeColors) {
        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(colors.primary)
        progressBar.progressBackgroundTintList = android.content.res.ColorStateList.valueOf(colors.surface)
    }

    /**
     * Create input field background drawable
     */
    private fun createInputBackground(colors: ThemeColors): GradientDrawable {
        return GradientDrawable().apply {
            setColor(colors.surface)
            cornerRadius = 8f
            setStroke(2, colors.primary) // Use primary color for the border
        }
    }

    /**
     * Create button background drawable
     */
    private fun createButtonBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 8f
            // Add a subtle stroke for better visibility on similar background colors
            setStroke(1, android.graphics.Color.parseColor("#33000000"))
        }
    }

    /**
     * Apply theme to dividers/separators
     */
    fun applyToDividers(rootView: View, colors: ThemeColors) {
        findAndApplyToDividers(rootView, colors)
    }

    private fun findAndApplyToDividers(view: View, colors: ThemeColors) {
        if (view.layoutParams?.height == 2) {
            // Likely a divider
            view.setBackgroundColor(colors.divider)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findAndApplyToDividers(view.getChildAt(i), colors)
            }
        }
    }
}