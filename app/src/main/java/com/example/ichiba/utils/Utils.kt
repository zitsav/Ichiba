package com.example.ichiba.utils

import android.content.Context
import android.widget.Toast
import com.example.ichiba.R
import com.example.ichiba.models.ModelCategory
import java.util.Calendar
import java.util.Locale

object Utils {
    object Utils {
        val categories = listOf(
            ModelCategory("ELECTRONICS", R.drawable.ic_phone),
            ModelCategory("COOLER", R.drawable.baseline_electrical_services_24),
            ModelCategory("BOOKS", R.drawable.ic_book),
            ModelCategory("MATTRESS", R.drawable.baseline_bed_24),
            ModelCategory("OTHERS", R.drawable.baseline_miscellaneous_services_24)
        )
    }

    fun getTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun formatTimestampDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp
        return android.text.format.DateFormat.format("dd/MM/yyyy", calendar).toString()
    }
}
