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
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ml.AutoModel4
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class CameraPage : AppCompatActivity() {

    val paint = Paint()
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

        paint.color = Color.GREEN

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

                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                var canvas = Canvas(mutable)
                var h = bitmap.height
                var w = bitmap.width
                var x = 0

                Log.d("output__", outputFeature0.size.toString())
                val jointCoordinates = mutableListOf<Pair<Float, Float>>()
                val requiredKeypoints = setOf(0, 1, 2, 3, 5, 6, 7, 8, 10)
                while(x <= 49){
                    if (outputFeature0.get(x + 2) > 0.45 && outputFeature0.get(x).toInt() in requiredKeypoints) {
                        canvas.drawCircle(outputFeature0.get(x+1)*w, outputFeature0.get(x)*h, 10f, paint)
                        jointCoordinates.add(Pair(outputFeature0.get(x + 1) * w, outputFeature0.get(x) * h))
                    }
                    x+=3
                }
                val threshold = 0.4 // Adjust this threshold as needed
                val connections = listOf(
                    Pair(9, 8),  // right wrist -> right elbow
                    Pair(8, 7),  // right elbow -> right shoulder
                    Pair(7, 6),  // right shoulder -> left shoulder
                    Pair(6, 5),  // left shoulder -> left elbow
                    Pair(5, 9),  // left elbow -> left wrist
                    Pair(7, 11), // right shoulder -> right hip
                    Pair(6, 12), // left shoulder -> left hip
                    Pair(11, 13),// right hip -> right knee
                    Pair(12, 14),// left hip -> left knee
                    Pair(13, 15),// right knee -> right ankle
                    Pair(14, 16) // left knee -> left ankle
                )

                paint.strokeWidth = 5f

                for ((start, end) in connections) {
                    if (start < jointCoordinates.size && end < jointCoordinates.size) {
                        canvas.drawLine(jointCoordinates[start].first, jointCoordinates[start].second,
                            jointCoordinates[end].first, jointCoordinates[end].second, paint)
                    } else {
                        Log.e("Error", "Invalid indices for drawing line: ($start, $end)")
                    }
                }
                imageView.setImageBitmap(mutable)
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