package ipca.game.hash

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Verifique se é um convite aceito
            val messageType = remoteMessage.data["messageType"]
            if (messageType == "inviteAccepted") {
                val inviterId = remoteMessage.data["inviterId"]
                Log.d(TAG, "Convite aceito do usuário: $inviterId")

                // Intent para abrir HashActivity
                val intent = Intent(this@MyFirebaseMessagingService, HashActivity::class.java)
                startActivity(intent)
            }
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
