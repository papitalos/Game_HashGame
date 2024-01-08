package ipca.game.hash.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import ipca.game.hash.LoginActivity
import ipca.game.hash.MainActivity
import ipca.game.hash.OnlineUsers
import ipca.game.hash.TAG
import ipca.game.hash.databinding.FragmentUserBinding
import ipca.game.hash.ui.invites.InviteFragment


class UserFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var _binding: FragmentUserBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        db = Firebase.firestore

        fetchCurrentUser()

        binding.buttonConfirmChange.setOnClickListener {
            val newNickname = binding.editTextNickname.text.toString()
            updateNickname(newNickname)
        }

        binding.buttonLogout.setOnClickListener {
            val userId = auth.currentUser?.uid
            userId?.let { uid ->
                db.collection("users").document(uid).update("isOnline", false)
                    .addOnSuccessListener {
                        auth.signOut()
                        val intent = Intent(activity, LoginActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao atualizar status isOnline", e)
                        // Opcional: Mostrar mensagem de erro ou tentar logout mesmo assim
                    }
            }
        }

        return binding.root
    }

    private fun fetchCurrentUser() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(OnlineUsers::class.java)
                        binding.textShowEmail.text = user?.email
                        binding.textShowNickname.text = user?.nickname
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erro ao buscar informações do usuário", e)
                }
        }
    }

    private fun updateNickname(newNickname: String) {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            db.collection("users").document(uid).update("nickname", newNickname)
                .addOnSuccessListener {
                    Toast.makeText(context, "Nickname atualizado com sucesso", Toast.LENGTH_SHORT).show()
                    binding.textShowNickname.text = newNickname

                    binding.editTextNickname.setText("")

                    (parentFragment as? InviteFragment)?.fetchOnlineUsers()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erro ao atualizar nickname", e)
                }
        }
    }
}