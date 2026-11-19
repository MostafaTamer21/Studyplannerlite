package com.example.studyplannerlite.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studyplannerlite.R
import com.example.studyplannerlite.data.AppDatabase
import com.example.studyplannerlite.data.Task
import com.example.studyplannerlite.data.TaskDao
import com.example.studyplannerlite.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var taskDao: TaskDao
    private lateinit var screenTitle: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var deadlineButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private val editingTaskId: Int by lazy {
        intent.getIntExtra(EXTRA_TASK_ID, NEW_TASK_ID)
    }
    private val isNewTask: Boolean
        get() = editingTaskId == NEW_TASK_ID

    private var existingTask: Task? = null
    private var selectedDeadline: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        taskDao = AppDatabase.getInstance(this).taskDao()
        screenTitle = findViewById(R.id.screenTitle)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        deadlineButton = findViewById(R.id.deadlineButton)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        selectedDeadline = savedInstanceState?.getLong(KEY_DEADLINE) ?: startOfToday()
        screenTitle.setText(
            if (isNewTask) R.string.add_task_title else R.string.edit_task_title
        )
        updateDeadlineText()

        cancelButton.setOnClickListener { finish() }
        deadlineButton.setOnClickListener { showDatePicker() }
        saveButton.setOnClickListener { saveTask() }
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                titleEditText.error = null
            }
        })

        restoreOrLoadTask(savedInstanceState)
    }

    private fun restoreOrLoadTask(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_TASK_ID)) {
            existingTask = Task(
                id = savedInstanceState.getInt(KEY_TASK_ID),
                title = savedInstanceState.getString(KEY_TASK_TITLE).orEmpty(),
                description = savedInstanceState.getString(KEY_TASK_DESCRIPTION).orEmpty(),
                deadline = savedInstanceState.getLong(KEY_TASK_DEADLINE),
                isDone = savedInstanceState.getBoolean(KEY_TASK_DONE)
            )
        } else if (!isNewTask) {
            saveButton.isEnabled = false
            loadTask()
        }
    }

    private fun loadTask() {
        lifecycleScope.launch {
            val task = withContext(Dispatchers.IO) {
                taskDao.getTaskById(editingTaskId)
            }
            if (task == null) {
                Toast.makeText(
                    this@AddEditTaskActivity,
                    R.string.task_not_found,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            existingTask = task
            selectedDeadline = task.deadline
            titleEditText.setText(task.title)
            descriptionEditText.setText(task.description)
            updateDeadlineText()
            saveButton.isEnabled = true
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDeadline
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val chosenDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDeadline = chosenDate.timeInMillis
                updateDeadlineText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDeadlineText() {
        deadlineButton.text = DateUtils.formatDate(selectedDeadline)
    }

    private fun saveTask() {
        val title = titleEditText.text.toString().trim()
        if (title.isEmpty()) {
            titleEditText.error = getString(R.string.error_title_required)
            titleEditText.requestFocus()
            return
        }

        val description = descriptionEditText.text.toString().trim()
        val baseTask = existingTask ?: Task(
            title = title,
            description = description,
            deadline = selectedDeadline
        )
        val taskToSave = baseTask.copy(
            title = title,
            description = description,
            deadline = selectedDeadline
        )

        saveButton.isEnabled = false
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (isNewTask) {
                    taskDao.insert(taskToSave)
                } else {
                    taskDao.update(taskToSave)
                }
            }
            Toast.makeText(
                this@AddEditTaskActivity,
                R.string.task_saved,
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_DEADLINE, selectedDeadline)

        val task = existingTask
        if (task != null) {
            outState.putInt(KEY_TASK_ID, task.id)
            outState.putString(KEY_TASK_TITLE, task.title)
            outState.putString(KEY_TASK_DESCRIPTION, task.description)
            outState.putLong(KEY_TASK_DEADLINE, task.deadline)
            outState.putBoolean(KEY_TASK_DONE, task.isDone)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
        const val NEW_TASK_ID = -1

        private const val KEY_DEADLINE = "deadline"
        private const val KEY_TASK_ID = "task_id"
        private const val KEY_TASK_TITLE = "task_title"
        private const val KEY_TASK_DESCRIPTION = "task_description"
        private const val KEY_TASK_DEADLINE = "task_deadline"
        private const val KEY_TASK_DONE = "task_done"
    }
}
