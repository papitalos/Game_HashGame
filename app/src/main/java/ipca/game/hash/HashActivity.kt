package ipca.game.hash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.firestore.FirebaseFirestore

class HashActivity : AppCompatActivity(), HashView.OnGameEndListener {
    private lateinit var hashView: HashView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hash)

        val gameId = intent.getStringExtra("gameId") ?: return

        // Inicializa hashView corretamente
        hashView = findViewById<HashView>(R.id.hashViewGame)

        // Configura as informações do jogo com base no que foi passado pelo Intent
        hashView.setGameInfo(gameId)

        val buttonHome = findViewById<Button>(R.id.idButtonHome)
        buttonHome.setOnClickListener {
            finish()
        }

        // Definir o listener para o evento de finalização do jogo
        hashView.setOnGameEndListener(this)
    }
    override fun onGameEnd() {
        val gameId = intent.getStringExtra("gameId") ?: return
        deleteJogadasThenGame(gameId)
    }

    private fun deleteJogadasThenGame(gameId: String) {
        val jogadasCollection = FirebaseFirestore.getInstance().collection("games").document(gameId).collection("jogadas")

        // Primeiro, delete todas as jogadas
        jogadasCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                jogadasCollection.document(document.id).delete()
            }

            // Depois, delete o jogo
            FirebaseFirestore.getInstance().collection("games").document(gameId)
                .delete()
                .addOnSuccessListener {
                    Log.d("HashActivity", "Jogo e jogadas deletadas com sucesso")
                    finish() // Fecha a HashActivity
                }
                .addOnFailureListener { e ->
                    Log.e("HashActivity", "Erro ao deletar jogo", e)
                }
        }
    }
}

