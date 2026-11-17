package com.example.studyplannerlite.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studyplannerlite.R
import com.example.studyplannerlite.data.AppDatabase
import com.example.studyplannerlite.data.Task
import com.example.studyplannerlite.data.TaskDao
import com.example.studyplannerlite.settings.SettingsManager
import com.example.studyplannerlite.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskListActivity : AppCompatActivity() {
    private lateinit var taskDao: TaskDao
    private lateinit var settingsManager: SettingsManager
    private lateinit var adapter: TaskAdapter
    private lateinit var taskListView: ListView
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        taskDao = AppDatabase.getInstance(this).taskDao()
        settingsManager = SettingsManager(getSharedPreferences(SettingsManager.PREFS_NAME, Context.MODE_PRIVATE))

        taskListView = findViewById(R.id.taskListView)
        emptyText = findViewById(R.id.emptyText)
        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        adapter = TaskAdapter(
            context = this,
            onDeleteClick = { task -> deleteTask(task) },
            onShareClick = { task -> ShareHelper.shareTask(this, task) },
            onDoneChange = { task, isDone -> updateDone(task, isDone) }
        )
        taskListView.adapter = adapter
        taskListView.setOnItemClickListener { _, _, position, _ ->
            val task = adapter.getItem(position)
            startActivity(Intent(this, AddEditTaskActivity::class.java)
                .putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.id))
        }

        addTaskButton.setOnClickListener {
            openTaskEditor(AddEditTaskActivity.NEW_TASK_ID)
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}

private fun openTaskEditor(taskId: Int) {
    startActivity(Intent(this, AddEditTaskActivity::class.java).putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId))
}