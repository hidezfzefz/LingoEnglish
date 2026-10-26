package com.lingo.app

import android.app.Application
import com.lingo.app.data.CallStore
import com.lingo.app.data.ProgressStore
import com.lingo.app.reminders.PracticeReminder

/**
 * Holds the stores that must outlive a single activity, so rotating the phone
 * cannot abandon an exam or drop a live call.
 */
class LingoApplication : Application() {
    val progressStore: ProgressStore by lazy { ProgressStore(this) }
    val callStore: CallStore by lazy { CallStore(this, progressStore) }

    override fun onCreate() {
        super.onCreate()
        PracticeReminder.createChannel(this)
        if (progressStore.remindersEnabled) {
            // A force-stop or a missed boot broadcast can leave the alarm
            // absent. Opening the app is a safe moment to restore it.
            PracticeReminder.scheduleNext(this, progressStore)
        }
    }
}
