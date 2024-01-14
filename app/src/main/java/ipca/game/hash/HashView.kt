package ipca.game.hash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class HashView : View {
    var points = arrayListOf<Point>()
    var crosses = arrayListOf<Point>()

    var isPoints = true
    var gameActive = true


    private var onTurnChanged : ((Boolean)->Unit)? = null

    private var gameId: String? = ""
    private var turno: String? = ""
    private var currentSymbol: String = "o" // Valor padrão
    private var currentPlayerTurn: String? = null

    fun setGameInfo(gameId: String?) {
        this.gameId = gameId
        Log.d("HashView", "My gameId is: $gameId")

        setupBoardListener()
        setupGameListeners()
    }
    fun setupGameListeners() {
        val db = FirebaseFirestore.getInstance()
        gameId?.let { gameId ->
            // Listener para mudanças no símbolo
            db.collection("games").document(gameId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Log.e("HashView", "Erro ao escutar mudanças no jogo", e)
                        return@addSnapshotListener
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentSymbol = documentSnapshot.getString("simbolo") ?: "o"
                        currentPlayerTurn = documentSnapshot.getString("turno")
                    }
                }
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val paint = Paint()

        // Configurações para as linhas da grade
        paint.color = Color.BLACK
        paint.strokeWidth = 10f
        val hSpacing = width / 3f
        val vSpacing = height / 3f

        // Desenhar a grade
        canvas.drawLine(hSpacing, 0f, hSpacing, height.toFloat(), paint)
        canvas.drawLine(2 * hSpacing, 0f, 2 * hSpacing, height.toFloat(), paint)
        canvas.drawLine(0f, vSpacing, width.toFloat(), vSpacing, paint)
        canvas.drawLine(0f, 2 * vSpacing, width.toFloat(), 2 * vSpacing, paint)

        // Configurações para desenhar os "O"s
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.STROKE // Apenas o contorno
        paint.strokeWidth = 15f // Espessura do contorno

        // Desenhar os "O"s
        for (p in points) {
            canvas.drawCircle(
                (hSpacing * p.x.toFloat()) - hSpacing / 2,
                (vSpacing * p.y.toFloat()) - vSpacing / 2,
                hSpacing / 3f, // Tamanho do círculo
                paint
            )
        }

        // Configurações para desenhar os "X"s
        paint.color = Color.GRAY
        paint.strokeWidth = 15f

        // Desenhar os "X"s
        for (p in crosses) {
            val xStart = hSpacing * (p.x.toFloat() - 1)
            val xEnd = hSpacing * p.x.toFloat()
            val yStart = vSpacing * (p.y.toFloat() - 1)
            val yEnd = vSpacing * p.y.toFloat()
            val padding = 60f // Espaçamento das bordas

            // Linha diagonal de cima para baixo
            canvas.drawLine(
                xStart + padding, yStart + padding,
                xEnd - padding, yEnd - padding,
                paint
            )

            // Linha diagonal de baixo para cima
            canvas.drawLine(
                xStart + padding, yEnd - padding,
                xEnd - padding, yStart + padding,
                paint
            )
        }
    }


    fun updateBoardFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        gameId?.let { gameId ->
            db.collection("games").document(gameId)
                .collection("jogadas")
                .get()
                .addOnSuccessListener { documents ->
                    points.clear()
                    crosses.clear()
                    for (document in documents) {
                        val symbol = document.getString("symbol")
                        val position = document.get("position") as List<Number>?
                        if (position != null) {
                            val point = Point(position[0].toInt(), position[1].toInt())
                            if (symbol == "o") {
                                points.add(point)
                            } else if (symbol == "x") {
                                crosses.add(point)
                            }
                        }
                    }
                    invalidate() // Redesenhar o tabuleiro
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateBoard", "Erro ao atualizar o tabuleiro", e)
                }
        }
    }

    fun setupBoardListener() {
        val db = FirebaseFirestore.getInstance()
        gameId?.let { gameId ->
            db.collection("games").document(gameId)
                .collection("jogadas")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("BoardListener", "Ouça falhou.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        updateBoardFromFirestore()
                    }
                }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!gameActive) {
            return false // Se o jogo terminou, não processa mais toques
        }

        super.onTouchEvent(event)
        val x = event?.x ?: 0
        val y = event?.y ?: 0

        val w3 = width / 3f
        val h3 = height / 3f

        var qx = 0
        var qy = 0

        if (x.toFloat() > w3 * 2) {
            qx = 3
        } else if (x.toFloat() > w3) {
            qx = 2
        } else {
            qx = 1
        }

        if (y.toFloat() > h3 * 2) {
            qy = 3
        } else if (y.toFloat() > h3) {
            qy = 2
        } else {
            qy = 1
        }


        val newPoint = Point(qx, qy)
        val currentUserId = getCurrentUserId()

        // Verifica se o ponto já está ocupado e se é a vez do jogador
        if (!points.contains(newPoint) && !crosses.contains(newPoint) && currentPlayerTurn == currentUserId) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (currentSymbol == "o") {
                        points.add(newPoint)
                        recordPlayerMove("o", newPoint)
                        updateSymbolInFirestore("x") // Atualizar para o próximo símbolo
                    } else if (currentSymbol == "x") {
                        crosses.add(newPoint)
                        recordPlayerMove("x", newPoint)
                        updateSymbolInFirestore("o") // Atualizar para o próximo símbolo
                    }

                    checkForWinner()
                    updateTurnInFirestore(currentUserId) // Atualiza o turno no Firestore Database
                    invalidate() // Redesenhar
                }
            }
        }

        return false
    }


    private fun updateSymbolInFirestore(nextSymbol: String) {
        Log.d("HashView", "Update")
        gameId?.let { gameId ->
            FirebaseFirestore.getInstance().collection("games").document(gameId)
                .update("simbolo", nextSymbol)
                .addOnSuccessListener {
                    Log.d("UpdateSymbol", "Símbolo atualizado com sucesso para: $nextSymbol")
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateSymbol", "Falha ao atualizar o símbolo no Firestore", e)
                }
        }
    }

    private fun recordPlayerMove(symbol: String, position: Point) {
        val db = FirebaseFirestore.getInstance()
        val moveData = hashMapOf(
            "symbol" to symbol,
            "position" to listOf(position.x, position.y)
        )

        // Adicionando a jogada na coleção "jogadas" com um ID de documento automático
        gameId?.let { gameId ->
            db.collection("games").document(gameId)
                .collection("jogadas").add(moveData)
                .addOnSuccessListener {
                    Log.d("RecordMove", "Jogada registrada com sucesso")
                }
                .addOnFailureListener { e ->
                    Log.e("RecordMove", "Erro ao registrar a jogada", e)
                }
        }
    }


    private fun getCurrentUserId(): String? {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        return user?.uid
    }


    private fun updateTurnInFirestore(currentUserId: String?) {
        // Verificar se o userId não é nulo
        if (currentUserId != null) {
            val db = FirebaseFirestore.getInstance()

            val jogoReference = gameId?.let { db.collection("games").document(it) }

            // Obter o valor das variáveis no Firestore
            jogoReference?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot? = task.result
                    if (document != null && document.exists()) {


                        val turnoAtual = document.getString("turno")
                        val players = document.get("players") as? List<String>
                        val idJogador1 = players?.get(0)
                        val idJogador2 = players?.get(1)

                        // Verificar se o jogo está no estado correto
                        if (turnoAtual != null && idJogador1 != null && idJogador2 != null) {
                            // Verificar qual jogador está jogando atualmente
                            val proximoTurno = if (turnoAtual == idJogador1) idJogador2 else idJogador1

                            // Atualizar a variável "turno" no Firestore com o próximo jogador
                            jogoReference.update("turno", proximoTurno)
                                .addOnSuccessListener {
                                    Log.d("UpdateTurn", "Turno atualizado com sucesso para: $proximoTurno")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UpdateTurn", "Falha ao atualizar o turno no Firestore", e)
                                }
                        }
                    }
                } else {
                    Log.e("UpdateTurn", "Falha ao obter informações do jogo no Firestore", task.exception)
                }
            }
        }
    }

    private fun checkForWinner() {
        val winningCombinations = listOf(
            // Linhas
            listOf(Point(1, 1), Point(2, 1), Point(3, 1)),
            listOf(Point(1, 2), Point(2, 2), Point(3, 2)),
            listOf(Point(1, 3), Point(2, 3), Point(3, 3)),
            // Colunas
            listOf(Point(1, 1), Point(1, 2), Point(1, 3)),
            listOf(Point(2, 1), Point(2, 2), Point(2, 3)),
            listOf(Point(3, 1), Point(3, 2), Point(3, 3)),
            // Diagonais
            listOf(Point(1, 1), Point(2, 2), Point(3, 3)),
            listOf(Point(3, 1), Point(2, 2), Point(1, 3))
        )

        for (combination in winningCombinations) {
            if (points.containsAll(combination)) {
                gameActive = false
                Toast.makeText(context, "O ganhou!", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    end()
                }, 2000)
                return
            }

            if (crosses.containsAll(combination)) {
                gameActive = false
                Toast.makeText(context, "X ganhou!", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    end()
                }, 2000)
                return
            }
        }

        if (points.size + crosses.size == 9) {
            gameActive = false
            Toast.makeText(context, "Velha!", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                end()
            }, 2000)
        }
    }


    // Interface para o callback
    interface OnGameEndListener {
        fun onGameEnd()
    }

    private var gameEndListener: OnGameEndListener? = null

    // Método para definir o listener
    fun setOnGameEndListener(listener: OnGameEndListener) {
        gameEndListener = listener
    }

    fun signalGameEnd() {
        gameId?.let { gameId ->
            FirebaseFirestore.getInstance().collection("games").document(gameId)
                .update("gameEnded", true)
                .addOnSuccessListener {
                    Log.d("HashView", "Sinalização de fim de jogo enviada")
                }
                .addOnFailureListener { e ->
                    Log.e("HashView", "Erro ao sinalizar fim de jogo", e)
                }
        }
    }

    fun end() {
        signalGameEnd() // Sinaliza o fim do jogo
        gameEndListener?.onGameEnd()
    }


}