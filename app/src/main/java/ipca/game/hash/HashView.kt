package ipca.game.hash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class HashView : View {
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

    var pointArrayList = arrayListOf<Point>();

    private var pointsChanged : ((Int) -> Unit)? = null;
    fun setOnClickPointListener(callback : (Int) -> Unit){
        pointsChanged = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint();

        paint.color =  Color.BLACK
        paint.strokeWidth = 20.0f

        val hSpacing : Float = width / 3.0f
        val vSpacing : Float = height / 3.0f


        canvas.drawLine(hSpacing, 0f, hSpacing, height.toFloat(), paint)
        canvas.drawLine(hSpacing*2, 0f, hSpacing*2,2*height.toFloat(), paint)
        canvas.drawLine(vSpacing, 0f, vSpacing, width.toFloat(), paint)
        canvas.drawLine(vSpacing*2, 0f, vSpacing*2, 2*width.toFloat(), paint)
    }


}