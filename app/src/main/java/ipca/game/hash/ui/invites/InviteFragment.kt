package ipca.game.hash.ui.invites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import ipca.game.hash.OnlineUsers
import ipca.game.hash.R
import ipca.game.hash.TAG
import ipca.game.hash.databinding.FragmentInviteBinding
import ipca.game.hash.ui.invites.InviteViewModel


class InviteFragment : Fragment() {

    val online_users = arrayListOf<OnlineUsers>()

    private  lateinit var db: FirebaseFirestore
    private lateinit var adapter: OnlineUsersAdapter

    private var _binding: FragmentInviteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    fun fetchOnlineUsers() {
        db = Firebase.firestore
        db.collection("users").whereEqualTo("isOnline", true)
            .get()
            .addOnSuccessListener { documents ->
                online_users.clear() // Limpa a lista antes de adicionar novos usuários online
                for (document in documents) {
                    val user = document.toObject(OnlineUsers::class.java)
                    online_users.add(user)
                }
                adapter.notifyDataSetChanged() // Notifica o adaptador que os dados mudaram
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Erro ao buscar usuários online", exception)
            }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(InviteViewModel::class.java)

        _binding = FragmentInviteBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OnlineUsersAdapter()
        val listViewOnlineUsers = binding.listViewOnlineUsers
        listViewOnlineUsers.adapter = adapter

        fetchOnlineUsers()

    }
    override fun onResume() {
        super.onResume()
        fetchOnlineUsers()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class OnlineUsersAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return online_users.size
        }

        override fun getItem(position: Int): Any {
            return online_users[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rootView = convertView ?: layoutInflater.inflate(R.layout.online_row, parent, false)
            val user = getItem(position) as OnlineUsers

            val textView = rootView.findViewById<TextView>(R.id.userID)
            val buttonInvite = rootView.findViewById<Button>(R.id.buttonJoin)

            textView.text = user.nickname

            buttonInvite.setOnClickListener {
                val invitedUserId = user.uid
                val myUserId = FirebaseAuth.getInstance().currentUser?.uid


                myUserId?.let { uid ->
                    checkForPendingInvites(uid) { hasPendingInvite ->
                        if (!hasPendingInvite) {
                            sendInvite(uid, user.nickname, invitedUserId)
                        } else {
                            Toast.makeText(context, "Aguarde a resposta do convite anterior", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }




            return rootView
        }
        private fun checkForPendingInvites(userId: String, callback: (Boolean) -> Unit) {
            FirebaseFirestore.getInstance().collection("invites")
                .whereEqualTo("inviterId", userId)
                .whereEqualTo("status", "sent")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        callback(false) // Não há convites pendentes
                    } else {
                        callback(true) // Existe um convite pendente
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erro ao verificar convites pendentes", e)
                    callback(false) // Em caso de falha, permita enviar um novo convite
                }
        }
        fun sendInvite(inviterId: String, inviterName: String, invitedId: String) {
            val inviteData = hashMapOf(
                "inviterId" to inviterId,
                "inviterName" to inviterName,
                "invitedId" to invitedId,
                "status" to "sent"
            )

            FirebaseFirestore.getInstance().collection("invites")
                .add(inviteData)
                .addOnSuccessListener { documentReference ->
                    // Aqui você pode obter o ID do convite se necessário
                    val inviteId = documentReference.id
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erro ao enviar convite", e)
                }
        }
    }
}