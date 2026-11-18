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
        settingsManager = SettingsManager(
            getSharedPreferences(SettingsManager.PREFS_NAME, Context.MODE_PRIVATE)
        )

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
            val intent = Intent(this, AddEditTaskActivity::class.java)
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.id)
            startActivity(intent)
        }

        addTaskButton.setOnClickListener {
            openTaskEditor(AddEditTaskActivity.NEW_TASK_ID)
        }
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun openTaskEditor(taskId: Int) {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }

    private fun loadTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tasks = taskDao.getAllTasks()
            val sortedTasks = if (settingsManager.getSortOrder() == SettingsManager.SORT_TITLE) {
                tasks.sortedBy {task -> task.title.lowercase()}
            }
            else {
                tasks.sortedBy {task -> task.deadline}
            }
            withContext(Dispatchers.Main) {
                adapter.updateTasks(sortedTasks)
                if (sortedTasks.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                }
                else {
                    emptyText.visibility = View.GONE
                }
            }
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            taskDao.delete(task)
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, R.string.task_deleted, Toast.LENGTH_SHORT).show()
                loadTasks()
            }
        }
    }

    private fun updateDone(task: Task, isDone: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updatedTask = Task(
                id = task.id,
                title = task.title,
                description = task.description,
                deadline = task.deadline,
                isDone = isDone
            )
            taskDao.update(updatedTask)
            withContext(Dispatchers.Main) {
                loadTasks()
            }
        }
    }
}
