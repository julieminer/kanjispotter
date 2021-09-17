package com.melonheadstudios.kanjispotter.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.activities.KanjiBubbleActivity
import com.melonheadstudios.kanjispotter.activities.MainActivity

class NotificationManager(private val context: Context) {
    companion object {
        private const val CHANNEL = "Kanji Spotter"
        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
        private const val SHORTCUT = "Show Kanji Spotter"
    }

    private val notificationManager: NotificationManager
    private val shortcutManager: ShortcutManager

    init {
        notificationManager = context.getSystemService() ?: throw IllegalStateException()
        shortcutManager = context.getSystemService() ?: throw IllegalStateException()
        setUpNotificationChannels()
        updateShortcuts()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setUpNotificationChannels() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL,
                "Kanji Spotter",
                // The importance must be IMPORTANCE_HIGH to show Bubbles.
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel used for notifications used to show Kanji Spotter panel"
                setAllowBubbles(true)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @WorkerThread
    fun updateShortcuts() {
        var shortcuts = listOf(1).map {
            val icon = Icon.createWithResource(context, R.drawable.image1b)
            // Create a dynamic shortcut for each of the contacts.
            // The same shortcut ID will be used when we show a bubble notification.
            ShortcutInfo.Builder(context, SHORTCUT)
                .setLocusId(LocusId(SHORTCUT))
                .setActivity(ComponentName(context, MainActivity::class.java))
                .setShortLabel("Kanji Spotter")
                .setIcon(icon)
                .setLongLived(true)
                .setCategories(setOf("com.melonheadstudios.kanjispotter.bubbles.category.TEXT_SHARE_TARGET"))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(
                            Uri.parse("https://com.melonheadstudios.kanjispotter/kanji/")
                        )
                )
                .setPerson(
                    Person.Builder()
                        .setName("Kanji Spotter")
                        .setIcon(icon)
                        .setImportant(true)
                        .build()
                )
                .build()
        }
        // Truncate the list if we can't show all of our contacts.
        val maxCount = shortcutManager.maxShortcutCountPerActivity
        if (shortcuts.size > maxCount) {
            shortcuts = shortcuts.take(maxCount)
        }
        shortcutManager.addDynamicShortcuts(shortcuts)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @WorkerThread
    fun showNotification() {
        dismissNotification()

        val icon = Icon.createWithResource(context, R.drawable.app_icon)
        val user = Person.Builder().setName("User").setImportant(true).build()
        val person = Person.Builder().setName("Kanji Spotter").setIcon(icon).setImportant(true).build()
        val contentUri = "https://com.melonheadstudios.kanjispotter/kanji/".toUri()

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            Intent(context, KanjiBubbleActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
                    .setData(contentUri),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = Notification.Builder(context, CHANNEL)
            .setBubbleMetadata(
                Notification.BubbleMetadata.Builder(pendingIntent, icon)
                    .setDesiredHeight(600)
                    .apply {
                        setSuppressNotification(true)
                    }
                    .build()
            )
            .setContentTitle("Kanji Spotter")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(icon)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setShortcutId(SHORTCUT)
            .setLocusId(LocusId(SHORTCUT))
            .addPerson(person)
            .setShowWhen(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, KanjiBubbleActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
            // Let's add some more content to the notification in case it falls back to a normal
            // notification.
            .setStyle(
                Notification.MessagingStyle(user)
                    .apply {
                        for (message in listOf(1)) {
                            val m = Notification.MessagingStyle.Message(
                                "Kanji Spotter",
                                System.currentTimeMillis(),
                                person,
                            )
                            addMessage(m)
                        }
                    }
                    .setGroupConversation(false)
            )
            .setWhen(System.currentTimeMillis())

        builder.setOnlyAlertOnce(true)
        notificationManager.notify(4, builder.build())
    }

    private fun dismissNotification() {
        notificationManager.cancelAll()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun canBubble(): Boolean {
        val channel = notificationManager.getNotificationChannel(
            CHANNEL,
            SHORTCUT
        )
        return notificationManager.areBubblesAllowed() || channel?.canBubble() == true
    }
}