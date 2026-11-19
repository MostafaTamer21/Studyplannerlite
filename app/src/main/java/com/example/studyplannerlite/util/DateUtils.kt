package com.example.studyplannerlite.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DATE_PATTERN = "dd MMM yyyy"

    fun formatDate(millis: Long): String =
        SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date(millis))
}
