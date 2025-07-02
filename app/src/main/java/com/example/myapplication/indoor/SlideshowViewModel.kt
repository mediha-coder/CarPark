package com.example.myapplication.indoor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.myapplication.R
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class SlideshowViewModel(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // Chemins
    private val path = Path()
    private val pathRetour = Path()
    private val arrowPath = Path()

    // Points enregistrés
    //private val loadedPoints = mutableListOf<PointF>()

    // Position courante
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastX = 0f
    private var lastY = 0f

    // Points de départ / arrivée
    private var startX = -1f
    private var startY = -1f
    private var endX = -1f
    private var endY = -1f
    private var startRetourX = -1f
    private var startRetourY = -1f

    // Zoom et mode
    private var isRetourMode = false
    private var scaleFactor = 1.0f
    private val minZoom = 0.1f
    private val maxZoom = 5.0f

    //private val arrowSize = 30f
    private var centerX = 0f
    private var centerY = 0f
    private var headingText: String = ""
    private var headingAngle: Float = 0f


    // Styles de dessin
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val returnPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val arrowPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val startBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.car_service)
    }

    private val endBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.stop)
    }
    private val locBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.location__1_)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h.toFloat() - 100f
        lastX = centerX
        lastY = centerY
        path.moveTo(centerX, centerY)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val realX = (event.x / scaleFactor) - offsetX
        val realY = (event.y / scaleFactor) - offsetY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (startX == -1f && startY == -1f) {
                    startX = realX
                    startY = realY
                    path.moveTo(startX, startY)
                } else if (endX == -1f && endY == -1f) {
                    endX = realX
                    endY = realY
                } else if (startRetourX == -1f && startRetourY == -1f) {
                    startRetourX = realX
                    startRetourY = realY
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val margin = 200f
                if (event.x < margin) offsetX -= (margin - event.x) / scaleFactor
                if (event.x > width - margin) offsetX += (event.x - (width - margin)) / scaleFactor
                if (event.y < margin) offsetY -= (margin - event.y) / scaleFactor
                if (event.y > height - margin) offsetY += (event.y - (height - margin)) / scaleFactor
                invalidate()
            }
        }
        return true
    }
    // Callback vers l’activité pour mise à jour UI
    var onDistanceChanged: ((Float) -> Unit)? = null
    private val trajectoryPoints = mutableListOf<TrajectoryPoint>()
    fun updatePosition(dx: Float, dy: Float) {
        lastX += dx
        lastY += dy

        if (isRetourMode) {
            pathRetour.lineTo(lastX, lastY)

        } else {
            path.lineTo(lastX, lastY)
            trajectoryPoints.add(TrajectoryPoint(lastX, lastY))
        }
        onDistanceChanged?.invoke(calculateDistance())
        invalidate()
    }
    data class TrajectoryData(
        val aller: List<TrajectoryPoint>,

    )
    data class TrajectoryPoint(val x: Float, val y: Float)

    private fun calculateDistance(): Float {
        var distancePx = 0f
        for (i in 1 until trajectoryPoints.size) {
            val p1 = trajectoryPoints[i - 1]
            val p2 = trajectoryPoints[i]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            distancePx += sqrt(dx * dx + dy * dy)
        }

        val metersPerPixel = 0.0254f / resources.displayMetrics.xdpi
        return distancePx * metersPerPixel
    }

    /*fun saveTrajectoryToFile(context: Context, filename: String) {
        val content = trajectoryPoints.joinToString("\n") { "x=${it.x}, y=${it.y}" }

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, "$filename.txt")

        try {
            file.writeText(content)
            Toast.makeText(context, "Fichier sauvegardé : ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

     */
    fun saveTrajectoryToFile(context: Context, filename: String) {
        val gson = Gson()
        val data = TrajectoryData(aller = trajectoryPoints)
        val json = gson.toJson(data)

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, "$filename.json")

        try {
            file.writeText(json)
            Toast.makeText(context, "Fichier sauvegardé : ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    fun loadTrajectoryFromFile(context: Context, filename: String )
    {
        val gson = Gson()
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, "$filename.json")

        if (!file.exists()) {
            Toast.makeText(context, "Fichier non trouvé", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val json = file.bufferedReader().use { it.readText() }
            val data = gson.fromJson(json, TrajectoryData::class.java)

            trajectoryPoints.clear()
            path.reset()

            data.aller.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(point.x, point.y)
                else path.lineTo(point.x, point.y)
                trajectoryPoints.add(point)
            }

            invalidate()
            Toast.makeText(context, "Trajectoire chargée avec succès", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur de lecture : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    fun enableRetourMode() {
        isRetourMode = true
        // Réinitialise les coordonnées pour commencer une nouvelle trajectoire
        lastX = startRetourX
        lastY = startRetourY
        pathRetour.moveTo(lastX, lastY)
        invalidate()
    }


    fun addStep(azimuth: Float) {
        val stepLength = 20f
        val angleRad = Math.toRadians(azimuth.toDouble())

        val dx = (stepLength * sin(angleRad)).toFloat()
        val dy = -(stepLength * cos(angleRad)).toFloat()

        val newX = lastX + dx
        val newY = lastY + dy

        if (isRetourMode) {
            pathRetour.lineTo(newX, newY)
        } else {
            path.lineTo(newX, newY)
            trajectoryPoints.add(TrajectoryPoint(newX, newY))
        }

        lastX = newX
        lastY = newY
        onDistanceChanged?.invoke(calculateDistance())
        invalidate()
    }




    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Appliquer le zoom et le déplacement
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)
        canvas.translate(-offsetX, -offsetY)



        if (startX != -1f && startY != -1f) {
            canvas.drawBitmap(startBitmap, startX - startBitmap.width / 2, startY - startBitmap.height / 2, null)
        }

        canvas.drawPath(path, paint)
        canvas.drawPath(arrowPath, arrowPaint)
        if (endX != -1f && endY != -1f) {
            canvas.drawBitmap(endBitmap, endX - endBitmap.width / 2, endY - endBitmap.height / 2, null)
        }
        if (startRetourX != -1f && startRetourY != -1f) {
            canvas.drawBitmap(
                locBitmap,
                startRetourX - locBitmap.width / 2,
                startRetourY - locBitmap.height / 2,
                null
            )
            canvas.drawPath(pathRetour, returnPaint)
        }
        // Fin de la zone zoomée
        canvas.restore()

        // --- Partie NON-ZOOMÉE : Texte direction + Boussole ---

        // Texte direction
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            isAntiAlias = true
        }
        val textBounds = Rect()
        textPaint.getTextBounds(headingText, 0, headingText.length, textBounds)

        val padding = 20
        val left = 50f
        val top = 50f
        val right = left + textBounds.width() + padding * 2
        val bottom = top + textBounds.height() + padding * 2

        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#AAFFFFFF")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        canvas.drawRect(left, top, right, bottom, backgroundPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawText(headingText, left + padding, top + padding + textBounds.height(), textPaint)

        // Boussole
        val radius = 90f
        val centerX = width - 160f
        val centerY = bottom + 70f

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(Color.parseColor("#ECECEC"), Color.parseColor("#B0B0B0")),
                floatArrayOf(0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        canvas.drawCircle(centerX, centerY, radius, circleStrokePaint)

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 3f
        }
        for (angle in 0 until 360 step 10) {
            val angleRad = Math.toRadians(angle.toDouble())
            val startX = centerX + (radius - 15) * cos(angleRad).toFloat()
            val startY = centerY + (radius - 15) * sin(angleRad).toFloat()
            val stopX = centerX + radius * cos(angleRad).toFloat()
            val stopY = centerY + radius * sin(angleRad).toFloat()
            canvas.drawLine(startX, startY, stopX, stopY, tickPaint)
        }

        // Aiguille rouge
        val angleRad = Math.toRadians(headingAngle.toDouble())
        val tipX = centerX + radius * 0.85f * sin(angleRad).toFloat()
        val tipY = centerY - radius * 0.85f * cos(angleRad).toFloat()
        val baseLeftX = centerX + 20f * sin(angleRad + Math.PI / 2).toFloat()
        val baseLeftY = centerY - 20f * cos(angleRad + Math.PI / 2).toFloat()
        val baseRightX = centerX + 20f * sin(angleRad - Math.PI / 2).toFloat()
        val baseRightY = centerY - 20f * cos(angleRad - Math.PI / 2).toFloat()

        val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 4f
        }
        val path = Path().apply {
            moveTo(tipX, tipY)
            lineTo(baseLeftX, baseLeftY)
            lineTo(baseRightX, baseRightY)
            close()
        }
        canvas.drawPath(path, arrowPaint)

        // Aiguille arrière grise
        val backTipX = centerX - radius * 0.25f * sin(angleRad).toFloat()
        val backTipY = centerY + radius * 0.25f * cos(angleRad).toFloat()
        val backBaseLeftX = centerX + 10f * sin(angleRad + Math.PI / 2).toFloat()
        val backBaseLeftY = centerY - 10f * cos(angleRad + Math.PI / 2).toFloat()
        val backBaseRightX = centerX + 10f * sin(angleRad - Math.PI / 2).toFloat()
        val backBaseRightY = centerY - 10f * cos(angleRad - Math.PI / 2).toFloat()

        val backPath = Path().apply {
            moveTo(backTipX, backTipY)
            lineTo(backBaseLeftX, backBaseLeftY)
            lineTo(backBaseRightX, backBaseRightY)
            close()
        }
        val backArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            style = Paint.Style.FILL
        }
        canvas.drawPath(backPath, backArrowPaint)

        val centerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, 15f, centerCirclePaint)

        val dirPaint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.SANS_SERIF
        }
        canvas.drawText("N", centerX, centerY - radius + 25f, dirPaint)
        canvas.drawText("S", centerX, centerY + radius - 10f, dirPaint)
        canvas.drawText("E", centerX + radius - 15f, centerY + 10f, dirPaint)
        canvas.drawText("O", centerX - radius + 15f, centerY + 10f, dirPaint)
    }


    fun updateHeadingText(direction: String, degree: Int) {
        headingText = "Position  = $direction, Degré = $degree"
        headingAngle = degree.toFloat()
        invalidate()
    }

    fun zoomIn() {
        if (scaleFactor < maxZoom) {
            scaleFactor += 0.1f
            invalidate()
        }
    }

    fun zoomOut() {
        if (scaleFactor > minZoom) {
            scaleFactor -= 0.1f
            invalidate()
        }
    }



}

