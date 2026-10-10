package com.eta.assistant.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_ID = "eta_business_alerts"
    private const val CHANNEL_NAME = "Eta Business Alerts"
    private const val CHANNEL_DESC = "Notifications for low stock alerts, sales updates, and payment reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled when notification permission is not yet granted
        }
    }

    fun sendTestNotification(context: Context, languageCode: String) {
        val (title, message) = when (languageCode) {
            "hi" -> "सूचना परीक्षण - Eta" to "ईटा की व्यावसायिक सूचनाएं सफलतापूर्वक सक्रिय हैं!"
            "ta" -> "அறிவிப்பு சோதனை - Eta" to "Eta வணிக அறிவிப்புகள் வெற்றிகரமாக செயல்படுகின்றன!"
            "bn" -> "বিজ্ঞপ্তি পরীক্ষা - Eta" to "Eta ব্যবসায়িক বিজ্ঞপ্তি সফলভাবে সক্রিয় করা হয়েছে!"
            else -> "Notification Test - Eta" to "Eta business notifications are active and working smoothly!"
        }
        sendNotification(context, 1001, title, message)
    }

    fun sendLowStockNotification(context: Context, productName: String, remainingStock: Int, languageCode: String) {
        val (title, message) = when (languageCode) {
            "hi" -> "कम स्टॉक चेतावनी!" to "$productName के केवल $remainingStock बचे हैं। कृपया पुनः स्टॉक करें।"
            "ta" -> "குறைந்த இருப்பு எச்சரிக்கை!" to "$productName இல் $remainingStock மட்டுமே மீதமுள்ளது. தயவுசெய்து புதுப்பிக்கவும்."
            "bn" -> "কম স্টক সতর্কতা!" to "$productName এর মাত্র $remainingStock টি বাকি আছে। দয়া করে পুনরায় স্টক করুন।"
            else -> "Low Stock Alert!" to "$productName has only $remainingStock left. Please restock soon."
        }
        sendNotification(context, 2001, title, message)
    }
}

