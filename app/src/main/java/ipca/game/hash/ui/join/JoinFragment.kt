package ipca.game.hash.ui.join

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import ipca.game.hash.HashActivity
import ipca.game.hash.Invite
import ipca.game.hash.OnlineUsers
import ipca.game.hash.R
import ipca.game.hash.TAG
import ipca.game.hash.databinding.FragmentJoinBinding
import java.util.UUID
import kotlin.random.Random

class JoinFragment : Fragment() {

    val invites_recived = arrayListOf<Invite>()

    private  lateinit var db: FirebaseFirestore
    private lateinit var adapter: InvitesRecivedAdapter

    private var _binding: FragmentJoinBinding? = null

    private val binding get() = _binding!!


    fun fetchRecivedInvites() {
        db = Firebase.firestore
        val myUid = FirebaseAuth.getInstance().currentUser?.uid

        myUid?.let { uid ->
            db.collection("invites").whereEqualTo("invitedId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    invites_recived.clear()
                    for (document in documents) {
                        val invite = document.toObject(Invite::class.java)
                        invite.inviteId = document.id // Armazena o ID do documento
                        invites_recived.add(invite)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Erro ao buscar convites do usuário", exception)
                }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(JoinViewModel::class.java)

        _binding = FragmentJoinBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = InvitesRecivedAdapter()
        val recivedInvitesList = binding.recivedInvitesList
        recivedInvitesList.adapter = adapter

        fetchRecivedInvites()

    }
    override fun onResume() {
        super.onResume()
        fetchRecivedInvites()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class InvitesRecivedAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return invites_recived.size
        }

        override fun getItem(position: Int): Any {
            return invites_recived[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rootView = convertView ?: layoutInflater.inflate(R.layout.invites_row, parent, false)
            val invite = getItem(position) as Invite

            val textView = rootView.findViewById<TextView>(R.id.textViewNickname)
            val buttonAccept = rootView.findViewById<Button>(R.id.buttonAccept)
            val buttonDecline = rootView.findViewById<Button>(R.id.buttonDecline)

            textView.text = invite.inviterName

            buttonAccept.setOnClickListener {
                FirebaseFirestore.getInstance().collection("invites")
                    .document(invite.inviteId)
                    .update("status", "accepted")
                    .addOnSuccessListener {

                        // Crie uma nova coleção "games" e adicione um documento com os dados dos usuários
                        val gamesCollection = FirebaseFirestore.getInstance().collection("games")

                        // Gera uma string aleatória de 4 caracteres alfanuméricos
                        val gameId = generateRandomId()

                        val gameData = hashMapOf(
                            "gameId" to gameId,
                            "players" to listOf(invite.inviterId, invite.invitedId),
                            "turno" to invite.inviterId
                        )

                        // Use o próprio ID gerado como nome do documento
                        gamesCollection.document(gameId)
                            .set(gameData)
                            .addOnSuccessListener {
                                Log.d(TAG, "Jogo criado com ID: $gameId")

                                // Após aceitar o convite, remova-o
                                removeInvite(invite, position)
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Erro ao criar jogo", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao aceitar convite", e)
                    }
            }



            buttonDecline.setOnClickListener {
                removeInvite(invite, position)
            }

            return rootView
        }


        private fun generateRandomId(): String {
            val characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            return buildString {
                repeat(4) {
                    append(characters[Random.nextInt(characters.length)])
                }
            }
        }

        fun removeInvite(invite: Invite, position: Int) {
            // Remova o convite do Firestore
            FirebaseFirestore.getInstance().collection("invites")
                .document(invite.inviteId)
                .delete()
                .addOnSuccessListener {
                    // Remova o convite da lista e notifique o adapter
                    invites_recived.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erro ao recusar convite", e)
                }
        }
    }
}

