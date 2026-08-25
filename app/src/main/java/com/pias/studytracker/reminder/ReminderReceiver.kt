package com.pias.studytracker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.pias.studytracker.data.StudyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Fires daily via ReminderScheduler. Skips the notification if today's hours are already logged. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // API 33+ requires POST_NOTIFICATIONS; if it was never granted, silently do nothing.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = StudyDatabase.getInstance(context.applicationContext).studyDao()
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entry = dao.getForDate(today)
                if (entry == null || entry.hours <= 0f) {
                    showReminderNotification(context.applicationContext)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
