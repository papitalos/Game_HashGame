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

class HashView : View {
    var points = arrayListOf<Point>()
    var crosses = arrayListOf<Point>()

    var isPoints = true
    var gameActive = true


    private var onTurnChanged : ((Boolean)->Unit)? = null

    fun setOnTurnChanged (callback: (Boolean)->Unit) {
        onTurnChanged = callback
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

        // Verifica se o ponto já está ocupado
        if (!points.contains(newPoint) && !crosses.contains(newPoint)) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isPoints)
                        points.add(newPoint)
                    else
                        crosses.add(newPoint)

                    checkForWinner()

                    isPoints = !isPoints
                    onTurnChanged?.invoke(isPoints)
                    invalidate()
                    return true
                }
            }
        }
        return false
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
                    clear()
                }, 2000)
                return
            }

            if (crosses.containsAll(combination)) {
                gameActive = false
                Toast.makeText(context, "X ganhou!", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    clear()
                }, 2000)
                return
            }
        }

        if (points.size + crosses.size == 9) {
            gameActive = false
            Toast.makeText(context, "Velha!", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                clear()
            }, 2000)
        }
    }

    fun clear(){
        points.clear()
        crosses.clear()
        gameActive = true
        invalidate()
    }


}