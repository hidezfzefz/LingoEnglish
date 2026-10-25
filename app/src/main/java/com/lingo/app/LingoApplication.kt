package com.lingo.app

import android.app.Application
import com.lingo.app.data.CallStore
import com.lingo.app.data.ProgressStore

/**
 * Holds the stores that must outlive a single activity, so rotating the phone
 * cannot abandon an exam or drop a live call.
 */
class LingoApplication : Application() {
    val progressStore: ProgressStore by lazy { ProgressStore(this) }
    val callStore: CallStore by lazy { CallStore(this, progressStore) }
}
