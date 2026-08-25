package com.pias.studytracker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pias.studytracker.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** AlarmManager alarms are cleared on reboot — this puts the reminder back if one was set. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferences(context.applicationContext)
                val time = prefs.reminderTime.first()
                if (time != null) {
                    ReminderScheduler.schedule(context.applicationContext, time.first, time.second)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
