package ipca.game.hash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import ipca.game.hash.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.buttonLogin.setOnClickListener {

            val email = binding.editTextEmailAddress.text.toString()
            val password = binding.editTextPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let { uid ->
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    val nickname = uid.take(6)
                                    val user = hashMapOf(
                                        "email" to email,
                                        "nickname" to nickname,
                                        "uid" to uid,
                                        "isOnline" to true
                                    )
                                    db.collection("users").document(uid).set(user)
                                        .addOnSuccessListener {
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Erro ao adicionar usuário ao Firestore", e)
                                            Toast.makeText(this, "Erro ao registrar usuário no banco de dados", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Usuário já existe, continue com o processo de login
                                    db.collection("users").document(uid).update("isOnline", true)
                                        .addOnSuccessListener {
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Erro ao verificar usuário no Firestore", e)
                                Toast.makeText(this, "Erro ao verificar usuário no banco de dados", Toast.LENGTH_SHORT).show()
                            }
                    } ?: run {
                        Toast.makeText(this, "Erro ao obter informações do usuário", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Se o login falhar, mostrar uma mensagem ao usuário
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Falha na autenticação.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}