package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ml.AutoModel4
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class CameraPage : AppCompatActivity() {

    val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    lateinit var imageProcessor: ImageProcessor
    lateinit var model: AutoModel4
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_page)
        get_permissions()

        imageProcessor = ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = AutoModel4.newInstance(this)
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        val backBtn: ImageButton = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = textureView.bitmap!!
                var tensorImage = TensorImage(DataType.UINT8)
                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                inputFeature0.loadBuffer(tensorImage.buffer)

                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                var mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                var canvas = Canvas(mutableBitmap)
                val h = bitmap.height
                val w = bitmap.width

                val jointCoordinates = Array<Pair<Float, Float>?>(17) { null }
                val confidenceThreshold = 0.5f

                val KEYPOINT_DICT = mapOf(
                    "left_shoulder" to 5,
                    "right_shoulder" to 6,
                    "left_elbow" to 7,
                    "right_elbow" to 8,
                    "left_wrist" to 9,
                    "right_wrist" to 10,
                    "left_hip" to 11,
                    "right_hip" to 12,
                    "left_knee" to 13,
                    "right_knee" to 14,
                    "left_ankle" to 15,
                    "right_ankle" to 16
                )

                val KEYPOINT_EDGE_INDS_TO_COLOR = mapOf(
                    Pair("left_shoulder", "right_shoulder") to "#00FF00", // Green for upper torso
                    Pair("left_shoulder", "left_elbow") to "#0000FF", // Blue for left arm
                    Pair("left_elbow", "left_wrist") to "#0000FF", // Blue
                    Pair("right_shoulder", "right_elbow") to "#FFFF00", // Yellow for right arm
                    Pair("right_elbow", "right_wrist") to "#FFFF00", // Yellow
                    Pair("left_shoulder", "left_hip") to "#00FFFF", // Cyan for left side of torso
                    Pair("right_shoulder", "right_hip") to "#FF00FF", // Magenta for right side of torso
                    Pair("left_hip", "right_hip") to "#FFA500", // Orange for lower torso
                    Pair("left_hip", "left_knee") to "#A52A2A", // Brown for left leg
                    Pair("left_knee", "left_ankle") to "#A52A2A", // Brown
                    Pair("right_hip", "right_knee") to "#800080", // Purple for right leg
                    Pair("right_knee", "right_ankle") to "#800080"  // Purple
                )
                // Extract keypoints with names for readability
                for (i in 0 until outputFeature0.size step 3) {
                    val confidence = outputFeature0[i + 2]
                    if (confidence > confidenceThreshold) {
                        val y = outputFeature0[i] * h
                        val x = outputFeature0[i + 1] * w
                        jointCoordinates[i / 3] = Pair(x, y)
                        // Optionally draw keypoints here
                    }
                }

                // Use your paint object as needed for drawing
                val paint = Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    color = Color.BLACK
                }

                // Drawing lines using named connections and color coding
                for ((start, end) in KEYPOINT_EDGE_INDS_TO_COLOR.keys) {
                    val startPoint = jointCoordinates[KEYPOINT_DICT.getValue(start)]
                    val endPoint = jointCoordinates[KEYPOINT_DICT.getValue(end)]
                    if (startPoint != null && endPoint != null) {
                        paint.color = Color.parseColor(KEYPOINT_EDGE_INDS_TO_COLOR.getValue(start to end)) // Update paint color
                        canvas.drawLine(startPoint.first, startPoint.second, endPoint.first, endPoint.second, paint)
                    }
                }

                imageView.setImageBitmap(mutableBitmap)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                var captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                captureRequest.addTarget(surface)
                p0.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }
                    override fun onConfigureFailed(p0: CameraCaptureSession) {

                    }
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        }, handler)
    }
    fun get_permissions(){
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
    }
    override fun onRequestPermissionsResult(  requestCode: Int, permissions: Array<out String>, grantResults: IntArray  ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) get_permissions()
    }
}