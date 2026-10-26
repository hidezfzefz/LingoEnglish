package com.lingo.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lingo.app.LingoApplication

/**
 * Shows at most one practice notification, then schedules the next day. A
 * learner who has already practised, or already met the daily goal, is not
 * interrupted.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != PracticeReminder.ACTION_REMINDER) return
        val app = context.applicationContext as? LingoApplication ?: return
        val store = app.progressStore
        if (!store.remindersEnabled) {
            PracticeReminder.cancel(context)
            return
        }
        if (!store.practicedToday && store.todayXp < store.dailyGoal) {
            PracticeReminder.show(context, store)
        }
        PracticeReminder.scheduleNext(context, store, fromTomorrow = true)
    }
}
