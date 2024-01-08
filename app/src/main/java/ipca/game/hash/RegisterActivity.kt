package ipca.game.hash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import ipca.game.hash.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegisterDone.setOnClickListener {

            val email = binding.editTextRegisterEmail.text.toString()
            val password = binding.editTextRegisterPassword.text.toString()
            val password2 = binding.editTextRegisterPasswordVerify.text.toString()

            if (password != password2){
                Toast.makeText(
                    baseContext,
                    "Passwords do not match.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            if (!password.isPasswordValid()){
                Toast.makeText(
                    baseContext,
                    "Password must have at least 6 chars.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            if (!email.isValidEmail()){
                Toast.makeText(
                    baseContext,
                    "Email is not valid.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }



            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            Log.i("RegisterActivity","${email}, ${password}," )
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.${task.exception}", Toast.LENGTH_SHORT,).show()
                }
            }
        }
    }
}