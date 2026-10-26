package com.lingo.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lingo.app.LingoApplication

/**
 * Re-arms the next reminder after a reboot, an app update, or a clock or
 * timezone change. Android drops alarms across those events, so this receiver
 * exists purely to restore the learner's choice.
 */
class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action !in RESCHEDULE_ACTIONS) return
        val app = context.applicationContext as? LingoApplication ?: return
        val store = app.progressStore
        if (store.remindersEnabled) {
            PracticeReminder.scheduleNext(context, store)
        } else {
            PracticeReminder.cancel(context)
        }
    }

    private companion object {
        val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
