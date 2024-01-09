package ipca.game.hash.ui.games

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
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import ipca.game.hash.Game
import ipca.game.hash.HashActivity
import ipca.game.hash.R
import ipca.game.hash.TAG
import ipca.game.hash.databinding.FragmentGamesBinding
import ipca.game.hash.databinding.FragmentInviteBinding
import ipca.game.hash.ui.invites.InviteViewModel


class GameFragment : Fragment() {

    val existing_games = arrayListOf<Game>()

    private  lateinit var db: FirebaseFirestore
    private lateinit var adapter: GamesAdapter

    private var _binding: FragmentGamesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    fun fetchExistingGames() {
        db = Firebase.firestore
        db.collection("games")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Erro ao buscar jogos existentes", e)
                    return@addSnapshotListener
                }

                existing_games.clear() // Limpa a lista antes de adicionar novos jogos

                for (doc in snapshots!!) {
                    val gameId = doc.id
                    val id1 = doc.getString("id_1")
                    val id2 = doc.getString("id_2")
                    val turno = doc.getString("turno")

                    // Crie uma instância de Game e adicione à lista
                    val game = Game(gameId, id1.orEmpty(), id2.orEmpty(), turno.orEmpty())
                    existing_games.add(game)
                }

                adapter.notifyDataSetChanged() // Notifica o adaptador que os dados mudaram
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(GameViewModel::class.java)

        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GamesAdapter()
        val listViewGames = binding.listViewGames
        listViewGames.adapter = adapter

        fetchExistingGames()

    }
    override fun onResume() {
        super.onResume()
        fetchExistingGames()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class GamesAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return existing_games.size
        }

        override fun getItem(position: Int): Any {
            return existing_games[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rootView = convertView ?: layoutInflater.inflate(R.layout.games_row, parent, false)
            val games = getItem(position) as Game

            val textView = rootView.findViewById<TextView>(R.id.idGame)
            val buttonJoinGame = rootView.findViewById<Button>(R.id.buttonJoinGame)

            textView.text = games.gameId

            buttonJoinGame.setOnClickListener {
                // Iniciar a HashActivity ao clicar no botão
                val intent = Intent(context, HashActivity::class.java)
                // Você pode passar dados extras para a atividade, se necessário
                intent.putExtra("gameId", games.gameId)
                intent.putExtra("id1", games.id1)
                intent.putExtra("id2", games.id2)
                intent.putExtra("turno", games.turno)

                context?.startActivity(intent)
            }

            return rootView
        }

    }
}