import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {
    private val joystickRadius = 100f
    private val buttonRadius = 50f
    private val dpadSize = 150

    private var joystickCenterX: Float = 0f
    private var joystickCenterY: Float = 0f
    private var isJoystickPressed = false

    private var button1CenterX: Float = 0f
    private var button1CenterY: Float = 0f
    private var isButton1Pressed = false

    private var button2CenterX: Float = 0f
    private var button2CenterY: Float = 0f
    private var isButton2Pressed = false

    private var paint: Paint = Paint()

    init {
        holder.addCallback(this)
        paint.isAntiAlias = true
        paint.color = Color.BLUE
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Initialize control positions
        joystickCenterX = width / 2f
        joystickCenterY = height / 2f

        button1CenterX = width / 4f
        button1CenterY = height / 4f

        button2CenterX = width * 3f / 4f
        button2CenterY = height / 4f

        drawControls()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Not needed
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Not needed
    }

    private fun drawControls() {
        val canvas = holder.lockCanvas()
        canvas?.let {
            it.drawColor(Color.WHITE)
            // Draw joystick
            paint.color = Color.BLUE
            it.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, paint)
            // Draw button 1
            paint.color = if (isButton1Pressed) Color.RED else Color.GREEN
            it.drawCircle(button1CenterX, button1CenterY, buttonRadius, paint)
            // Draw button 2
            paint.color = if (isButton2Pressed) Color.RED else Color.GREEN
            it.drawCircle(button2CenterX, button2CenterY, buttonRadius, paint)
            // Draw D-pad
            paint.color = Color.BLACK
            it.drawRect(0f, (height - dpadSize).toFloat(), dpadSize.toFloat(), height.toFloat(), paint)
            holder.unlockCanvasAndPost(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y

                // Joystick
                val dx = x - joystickCenterX
                val dy = y - joystickCenterY
                isJoystickPressed = Math.sqrt((dx * dx + dy * dy).toDouble()) <= joystickRadius
                if (isJoystickPressed) {
                    joystickCenterX = x
                    joystickCenterY = y
                }

                // Button 1
                isButton1Pressed = Math.sqrt(((x - button1CenterX) * (x - button1CenterX) + (y - button1CenterY) * (y - button1CenterY)).toDouble()) <= buttonRadius

                // Button 2
                isButton2Pressed = Math.sqrt(((x - button2CenterX) * (x - button2CenterX) + (y - button2CenterY) * (y - button2CenterY)).toDouble()) <= buttonRadius

                drawControls()
            }
            MotionEvent.ACTION_UP -> {
                isJoystickPressed = false
                isButton1Pressed = false
                isButton2Pressed = false
                drawControls()
            }
        }
        return true
    }
}
