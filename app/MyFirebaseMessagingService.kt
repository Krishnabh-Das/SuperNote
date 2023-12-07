import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming FCM messages here and display notifications
        val notificationData = remoteMessage.data
        val title = notificationData["title"]
        val body = notificationData["body"]

        sendNotification(title, body)
    }

    private fun sendNotification(title: String?, message: String?) {
        // Implement code to display a notification here
        // You can use NotificationCompat or Notification.Builder as shown earlier
    }
}
