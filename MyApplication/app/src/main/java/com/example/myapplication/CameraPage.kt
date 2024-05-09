package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.myapplication.ml.AutoModel4
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt


class CameraPage : AppCompatActivity() {

    // initialization of dots and line
    val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    // Initialization of soundPool
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    val soundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(audioAttributes)
        .build()

    private var end: Int = 0
    private var start: Int = 0
    private var error: Int = 0


    lateinit var imageProcessor: ImageProcessor
    lateinit var model: AutoModel4
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var exercise: Exercise
    var isExerciseStarted = false
    lateinit var captureBitmap : Bitmap
    val resultList = mutableListOf<Boolean>()
    private lateinit var filteredKeypointsGlobal: Array<Pair<Float, Float>>
    private var confidenceThresholdGlobal: Float = 0.3f
    private lateinit var frontCameraId: String
    private lateinit var backCameraId: String
    private var currentCameraId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_page)
        get_permissions()

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder
            .setTitle("Critical for best results")
            .setMessage("Before starting the activity, ensure the camera is positioned to capture your entire body. This is critical for best results.")
            .setCancelable(false)

            .setPositiveButton("OK") { dialog, which ->
                currentCameraId?.let { open_camera(it) }
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        // init sounds
        start = soundPool.load(this, R.raw.start, 1)
        end = soundPool.load(this, R.raw.end, 1)
        error = soundPool.load(this, R.raw.error, 1)

        val difficulty = intent.getStringExtra("LEVEL")
        val title = intent.getStringExtra("TITLE")

        // Start correcting form and timer
        val startExerciseButton: Button = findViewById(R.id.startBtn)
        val countDown: TextView = findViewById(R.id.countDown)
        val feedbackTextView: TextView = findViewById(R.id.feedbackTextView)
        var countDownTimer: CountDownTimer? = null
        startExerciseButton.setOnClickListener {
           if( isExerciseStarted ){
               soundPool.play(end , 1f, 1f, 0, 0, 1f)
               isExerciseStarted = false
               countDownTimer?.cancel()
               countDown.text = ""
               feedbackTextView.text = ""
           } else {
               soundPool.play(start , 1f, 1f, 0, 0, 1f)
               isExerciseStarted = true
               when(difficulty){
                   "Easy" -> countDownTimer = startCountDownTimer(countDown, 30000, 1000);
                   "Intermediate" -> countDownTimer = startCountDownTimer(countDown , 90000, 1000);
                   "Expert" -> countDownTimer = startCountDownTimer(countDown , 300000, 1000);
                   else -> {
                       isExerciseStarted = false;
                   }
               }
           }
        }

        //initializationo of Components inside activity_camera_page
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = AutoModel4.newInstance(this)
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        val cameraToggle: ImageButton = findViewById(R.id.cameraToggle);

        // Initialize camera IDs
        if (!cameraIdList.isNullOrEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (cameraId in cameraIdList) {
                    val cameraCharacteristics = cameraManager?.getCameraCharacteristics(cameraId)
                    val cameraLensFacing =
                        cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING)
                    if (cameraLensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                        frontCameraId = cameraId
                    } else if (cameraLensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        backCameraId = cameraId
                        currentCameraId = backCameraId
                    }
                }
            }
        }

        // Ensure both front and back camera ids are initialized
        if (!::frontCameraId.isInitialized || !::backCameraId.isInitialized) {
            // Handle error, for example, by displaying a message and closing the activity
            Toast.makeText(this, "Failed to get front and back camera ids", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cameraToggle.setOnClickListener {
            switchCamera(currentCameraId)
        }


        // Setting the title of the pose from the button that was click
        val titleTextView: TextView = findViewById(R.id.title)
        titleTextView.text = title

        //Setting Difficulty Level
        val difficultyTextView: TextView = findViewById(R.id.difficulty)
        difficultyTextView.text = difficulty;


        // back to main view
        val backBtn: ImageButton = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        // setup of model
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            // when application is ready it opens the users camera
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }
            // When application will close
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }
            // when image detect frames or images
                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                    // setup Model and load
                    bitmap = textureView.bitmap!!
                    var tensorImage = TensorImage(DataType.UINT8)
                    tensorImage.load(bitmap)
                    tensorImage = imageProcessor.process(tensorImage)

                    // convert image into tensorflow acceptable data type and size
                    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                    inputFeature0.loadBuffer(tensorImage.buffer)

                    // display the processed image into the image
                    val outputs = model.process(inputFeature0)
                    val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
                    var mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                    if (isExerciseStarted) {
                        captureBitmap = mutableBitmap
                    } else {
                        // If exercise is not started, do nothing or handle the case accordingly
                    }

                var canvas = Canvas(mutableBitmap)
                    val h = bitmap.height
                    val w = bitmap.width

                // join the pair into a line
                val jointCoordinates = Array<Pair<Float, Float>?>(17) { null }
                val confidenceThreshold = 0.3f

                // setting keypoints
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
                // pairing keypoints
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
                    }
                }

                // Convert the extracted keypoints to the required format
                val filteredKeypoints = jointCoordinates.filterNotNull().toTypedArray()
                filteredKeypointsGlobal = jointCoordinates.filterNotNull().toTypedArray()
                // Initialize Object with type of pose
                when(title){
                    "High Plank" -> exercise = HighPlankExercise(this@CameraPage)
                    "Low Side Plank" -> exercise = LowSidePlankExercise(this@CameraPage)
                    "High Side Plank" -> exercise = HighSidePlankExercise(this@CameraPage)
                    "Bird Dog" -> exercise = BirdDogExercise(this@CameraPage)
                    // ADD MORE POSE HERE
                    // EDIT CLASS IF NAME IS NOT THE SAME
                }

                // start correcting when "Start" button is clicked
                if (isExerciseStarted) {
                    startExerciseButton.text = "Stop"
                    val isCorrect = exercise.analyzeKeypoints(filteredKeypoints, confidenceThreshold)
                    runOnUiThread {
                        feedbackTextView.text = if (isCorrect) "Correct $title" else "Adjust Your Position"
                    }
                    if(!isCorrect) soundPool.play(error, 1f, 1f, 0, 0, 1f)
                }else {
                    startExerciseButton.text = "Start"
                }

                // Applying the paint to the pose form
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

    // close or destory the model
    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    // Function to toggle between front and back cameras
    private fun switchCamera(curr: String?) {
        if(curr == frontCameraId){
            currentCameraId = backCameraId
            open_camera(currentCameraId!!);
        } else if (curr == backCameraId){
            currentCameraId = frontCameraId
            open_camera(currentCameraId!!)
        }
    }

    // open camera function
    @SuppressLint("MissingPermission")
    var cameraDevice: CameraDevice? = null
    var cameraCaptureSession: CameraCaptureSession? = null

    @SuppressLint("MissingPermission")
    fun open_camera(cameraId: String) {
        // Close the old camera device if it's already open
        cameraDevice?.close()
        cameraDevice = null // Reset cameraDevice reference

        // Open the new camera device
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                val captureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(textureView.surfaceTexture)
                captureRequest?.addTarget(surface)

                cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        captureRequest?.let {
                            cameraCaptureSession?.setRepeatingRequest(it.build(), null, null)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        // Handle configuration failure
                    }
                }, handler)
            }

            override fun onDisconnected(camera: CameraDevice) {
                cameraDevice?.close() // Close the camera device if it's disconnected
                cameraDevice = null // Reset cameraDevice reference
            }

            override fun onError(camera: CameraDevice, error: Int) {
                // Handle camera error
                cameraDevice?.close() // Close the camera device in case of an error
                cameraDevice = null // Reset cameraDevice reference
            }
        }, handler)
    }


    // get app permission to use camera
    fun get_permissions(){
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
    }
    override fun onRequestPermissionsResult(  requestCode: Int, permissions: Array<out String>, grantResults: IntArray  ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) get_permissions()
    }

    // timer countdown
    fun startCountDownTimer(countDown: TextView, totalTime: Long, countDownInterval: Long): CountDownTimer {
        val timer = object : CountDownTimer(totalTime, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the TextView
                val secondsRemaining = millisUntilFinished / 1000
                countDown.text = "$secondsRemaining"

                // Store the result of analyzeKeypoints in the list
                val isCorrect = exercise.analyzeKeypoints(filteredKeypointsGlobal, confidenceThresholdGlobal)
                resultList.add(isCorrect)
            }

            override fun onFinish() {
                // Display finished text
                countDown.text = "FINISH!!"
                isExerciseStarted = false
                soundPool.play(end, 1f, 1f, 0, 0, 1f)
                val score = calculateScore(resultList)
                toScorePage(score)
            }
        }
        timer.start()
        return timer
    }

    fun saveBitmapToTempStorage(bitmap: Bitmap): Uri? {
        val context = applicationContext
        val cacheDir = context.cacheDir
        val file = File.createTempFile("temp_image", ".jpg", cacheDir)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    }

    fun toScorePage(score: Double) {
        // Retrieve data from intent extras
        val difficulty = intent.getStringExtra("LEVEL")
        val title = intent.getStringExtra("TITLE")

        // Save bitmap to temporary storage
        val capturedBitmap = captureBitmap // Replace `yourBitmap` with the actual Bitmap object
        val uri = saveBitmapToTempStorage(capturedBitmap)

        // Start ScorePage activity with intent
        val intent = Intent(this, ScorePage::class.java)
        intent.putExtra("LEVEL", difficulty)
        intent.putExtra("TITLE", title)
        intent.putExtra("SCORE", score)
        uri?.let { intent.putExtra("FRAME", it.toString()) }
        startActivity(intent)
    }

// Function to calculate score based on resultList
    fun calculateScore(resultList: List<Boolean>): Double {
        // Count the number of correct results
        val correctCount = resultList.count { it }

        // Calculate the total number of results
        val total = resultList.size

        // Calculate the score as percentage of correct answers
        val score = if (total > 0) {
            val rawScore = correctCount.toDouble() / total.toDouble() * 100
            // Round the score to two decimal places
            (rawScore * 100).roundToInt() / 100.0
        } else {
            0.0 // Handle division by zero
        }
        return if (score > 85.0) 100.0 else score
    }

}