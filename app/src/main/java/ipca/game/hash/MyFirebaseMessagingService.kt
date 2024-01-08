package ipca.game.hash

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Este método é chamado quando uma mensagem é recebida.
        // Você pode adicionar sua lógica para tratar a mensagem aqui.
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Exemplo de como acessar dados da mensagem
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // Exemplo de como acessar a notificação da mensagem
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Adicione sua lógica aqui para abrir a HashActivity ou realizar outras ações necessárias.
    }

    override fun onNewToken(token: String) {
        // Este método é chamado quando um novo token FCM é gerado.
        // Você pode usar este token para enviar mensagens diretamente para este dispositivo.
        Log.d(TAG, "Refreshed token: $token")
    }
}
