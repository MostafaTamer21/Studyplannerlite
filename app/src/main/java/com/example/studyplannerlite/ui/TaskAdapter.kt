package com.example.studyplannerlite.ui

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.example.studyplannerlite.R
import com.example.studyplannerlite.data.Task
import com.example.studyplannerlite.util.DateUtils

class TaskAdapter(
    private val context: Context,
    private val onDeleteClick: (Task) -> Unit,
    private val onShareClick: (Task) -> Unit,
    private val onDoneChange: (Task, Boolean) -> Unit
) : BaseAdapter() {

    private var tasks: List<Task> = emptyList()

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun getCount(): Int = tasks.size

    override fun getItem(position: Int): Task = tasks[position]

    override fun getItemId(position: Int): Long = tasks[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)
        val task = tasks[position]

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val deadlineText = view.findViewById<TextView>(R.id.deadlineText)
        val doneCheckBox = view.findViewById<CheckBox>(R.id.doneCheckBox)
        val shareButton = view.findViewById<Button>(R.id.shareButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)

        titleText.text = task.title
        deadlineText.text = context.getString(
            R.string.deadline_format,
            DateUtils.formatDate(task.deadline)
        )
        doneCheckBox.contentDescription = context.getString(
            R.string.done_checkbox_description,
            task.title
        )
        titleText.paintFlags = if (task.isDone) {
            titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        doneCheckBox.setOnCheckedChangeListener(null)
        doneCheckBox.isChecked = task.isDone
        doneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onDoneChange(task, isChecked)
        }
        shareButton.setOnClickListener { onShareClick(task) }
        deleteButton.setOnClickListener { onDeleteClick(task) }

        return view
    }
}
