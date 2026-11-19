package com.example.studyplannerlite.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.studyplannerlite.R
import com.example.studyplannerlite.settings.SettingsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsManager = SettingsManager(
            getSharedPreferences(SettingsManager.PREFS_NAME, Context.MODE_PRIVATE)
        )
        val sortGroup = findViewById<RadioGroup>(R.id.sortGroup)
        val deadlineRadio = findViewById<RadioButton>(R.id.deadlineRadio)
        val titleRadio = findViewById<RadioButton>(R.id.titleRadio)
        val backButton = findViewById<Button>(R.id.backButton)

        if (settingsManager.getSortOrder() == SettingsManager.SORT_TITLE) {
            titleRadio.isChecked = true
        } else {
            deadlineRadio.isChecked = true
        }

        sortGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedOrder = if (checkedId == R.id.titleRadio) {
                SettingsManager.SORT_TITLE
            } else {
                SettingsManager.SORT_DEADLINE
            }
            settingsManager.setSortOrder(selectedOrder)
        }

        backButton.setOnClickListener { finish() }
    }
}
