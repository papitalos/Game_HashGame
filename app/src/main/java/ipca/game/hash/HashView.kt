package ipca.game.hash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class HashView : View {
    var points = arrayListOf<Point>()
    var crosses = arrayListOf<Point>()

    var isPoints = true


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


        paint.color = Color.BLACK
        paint.strokeWidth = 10f
        val hSpacing = width / 3f
        val vSpacing = height / 3f
        canvas.drawLine(hSpacing,0f,hSpacing, height.toFloat(), paint)
        canvas.drawLine(2*hSpacing,0f,2*hSpacing, height.toFloat(), paint)
        canvas.drawLine(0f,vSpacing, width.toFloat(), vSpacing, paint)
        canvas.drawLine(0f,2*vSpacing, width.toFloat(), 2*vSpacing, paint)

        paint.color = Color.RED

        for (p in points){
            canvas.drawCircle(
                (hSpacing) * p.x.toFloat() - hSpacing/2,
                (vSpacing) * p.y.toFloat() - vSpacing/2,
                hSpacing/2.2f, paint)
        }

        paint.color = Color.GREEN

        for (p in crosses){
            Log.d("customcontrol", "w3:${hSpacing} h3:${vSpacing} px:${p.x} py:${p.y}")
            Log.d("customcontrol", "x:${hSpacing*(p.x.toFloat() - hSpacing)}  y:${vSpacing*(p.y.toFloat() - vSpacing)} ")
            canvas.drawLine(
                hSpacing*p.x.toFloat() - hSpacing,
                vSpacing*p.y.toFloat() - vSpacing,
                hSpacing*p.x.toFloat(),
                vSpacing*p.y.toFloat(),
                paint
            )

            canvas.drawLine(
                hSpacing*p.x.toFloat(),
                vSpacing*p.y.toFloat() - vSpacing,
                hSpacing*p.x.toFloat() - hSpacing,
                vSpacing*p.y.toFloat(),
                paint
            )
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val x = event?.x?:0
        val y = event?.y?:0

        val w3 = width / 3f
        val h3 = height / 3f

        var qx = 0
        var qy = 0

        if (x.toFloat() > w3*2) {
            qx = 3
        }else if (x.toFloat() > w3) {
            qx = 2
        }else {
            qx = 1
        }

        if (y.toFloat() > h3*2) {
            qy = 3
        }else if (y.toFloat() > h3) {
            qy = 2
        }else {
            qy = 1
        }


        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                if (isPoints)
                    points.add(Point(qx,qy))
                else
                    crosses.add(Point(qx,qy))
                isPoints = !isPoints
                onTurnChanged?.invoke(isPoints)
                invalidate()
                return true
            }
        }
        return false
    }

    fun clear(){
        points.clear()
        crosses.clear()
        invalidate()
    }


}