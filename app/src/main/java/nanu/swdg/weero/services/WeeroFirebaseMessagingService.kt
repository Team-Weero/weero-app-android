package nanu.swdg.weero.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nanu.swdg.weero.MainActivity
import nanu.swdg.weero.R
import nanu.swdg.weero.utils.SecureStorage

class WeeroFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "WeeroFCMService"
        private const val CHANNEL_ID = "weero_notification_channel"
        private const val CHANNEL_NAME = "Weero Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")

        // 토큰을 영구 저장
        val secureStorage = SecureStorage(applicationContext)
        secureStorage.saveFcmToken(token)

        // TODO: 필요시 웹앱에 토큰 갱신 이벤트 전달
        // 또는 서버에 토큰 업데이트 요청
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // 알림 데이터 처리
        message.notification?.let { notification ->
            val title = notification.title ?: "Weero"
            val body = notification.body ?: ""
            showNotification(title, body, message.data)
        }

        // 데이터 메시지 처리 (알림 없이 데이터만 올 경우)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataMessage(message.data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O 이상에서는 알림 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Weero app notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 앱 열기
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 데이터 전달 (필요시)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 알림 생성
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘 사용
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // 데이터 메시지 처리 로직
        // 필요시 웹앱으로 데이터 전달하거나 로컬 처리
        Log.d(TAG, "Handling data message: $data")
    }
}
