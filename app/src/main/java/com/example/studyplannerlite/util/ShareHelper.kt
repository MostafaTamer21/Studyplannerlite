package com.example.studyplannerlite.util

import android.content.Context
import android.content.Intent
import com.example.studyplannerlite.R
import com.example.studyplannerlite.data.Task

object ShareHelper {

    fun shareTask(context: Context, task: Task) {
        val status = context.getString(
            if (task.isDone) R.string.status_done else R.string.status_pending
        )
        val description = task.description.ifBlank {
            context.getString(R.string.no_description)
        }
        val message = context.getString(
            R.string.share_task_text,
            task.title,
            DateUtils.formatDate(task.deadline),
            status,
            description
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.share_task_subject, task.title)
            )
            putExtra(Intent.EXTRA_TEXT, message)
        }

        context.startActivity(
            Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
        )
    }
}
