package com.duma.snap

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isVideoMode = false
    private var recording: Recording? = null

    companion object {
        private const val PERMISSION_REQUEST = 10
        private const val TAG = "DUMA_SNAP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)

        captureButton.setOnClickListener {
            if (isVideoMode) toggleVideoRecording() else takePhoto()
        }
        findViewById<Button>(R.id.switchCameraButton).setOnClickListener {
            if (recording != null) {
                Toast.makeText(this, "Stop recording before switching cameras", Toast.LENGTH_SHORT).show()
            } else {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                startCamera()
            }
        }
        findViewById<Button>(R.id.flashButton).setOnClickListener {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            imageCapture?.flashMode = flashMode
            val label = when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> "FLASH ON"
                ImageCapture.FLASH_MODE_AUTO -> "FLASH AUTO"
                else -> "FLASH OFF"
            }
            (it as Button).text = label
        }
        findViewById<Button>(R.id.zoom05).setOnClickListener { setZoom(0.5f) }
        findViewById<Button>(R.id.zoom1x).setOnClickListener { setZoom(1f) }
        findViewById<Button>(R.id.zoom2x).setOnClickListener { setZoom(2f) }
        findViewById<Button>(R.id.zoom5x).setOnClickListener {
            val max = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 5f
            setZoom(max)
        }
        findViewById<Button>(R.id.modePhoto).setOnClickListener {
            isVideoMode = false
            captureButton.text = "●"
            Toast.makeText(this, "PHOTO", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.modeVideo).setOnClickListener {
            isVideoMode = true
            captureButton.text = "●"
            Toast.makeText(this, "VIDEO · Tap to record", Toast.LENGTH_SHORT).show()
        }

        requestCameraPermissions()
    }

    private fun requestCameraPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 23) permissions.add(Manifest.permission.RECORD_AUDIO)
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) startCamera()
        else ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            val cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (cameraGranted) startCamera()
            else Toast.makeText(this, "Camera permission is required to use DUMA SNAP", Toast.LENGTH_LONG).show()
        }
    }

    private fun setZoom(requested: Float) {
        val state = camera?.cameraInfo?.zoomState?.value ?: return
        camera?.cameraControl?.setZoomRatio(requested.coerceIn(state.minZoomRatio, state.maxZoomRatio))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: run {
            Toast.makeText(this, "Camera is starting. Try again in a moment.", Toast.LENGTH_SHORT).show()
            return
        }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "DUMA_SNAP_$stamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/DUMA SNAP")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val options = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        ).build()
        capture.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed", exception)
                Toast.makeText(this@MainActivity, "Photo could not be saved", Toast.LENGTH_SHORT).show()
            }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                        contentResolver.update(uri, done, null, null)
                    }
                }
                Toast.makeText(this@MainActivity, "Photo saved to Pictures / DUMA SNAP", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleVideoRecording() {
        if (recording != null) {
            recording?.stop()
            recording = null
            captureButton.text = "●"
            return
        }
        val capture = videoCapture ?: run {
            Toast.makeText(this, "Video camera is not ready", Toast.LENGTH_SHORT).show()
            return
        }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "DUMA_SNAP_$stamp.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/DUMA SNAP")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            MediaStoreOutputOptions.Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(values).build()
        } else {
            val folder = getExternalFilesDir("Movies") ?: filesDir
            FileOutputOptions.Builder(java.io.File(folder, "DUMA_SNAP_$stamp.mp4")).build()
        }
        var pending = capture.output.prepareRecording(this, outputOptions)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pending = pending.withAudioEnabled()
        }
        recording = pending.start(ContextCompat.getMainExecutor(this)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    captureButton.text = "■"
                    Toast.makeText(this, "Recording… tap ■ to stop", Toast.LENGTH_SHORT).show()
                }
                is VideoRecordEvent.Finalize -> {
                    recording = null
                    captureButton.text = "●"
                    if (!event.hasError()) {
                        event.outputResults.outputUri.takeIf { it != android.net.Uri.EMPTY }?.let { uri ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                                contentResolver.update(uri, done, null, null)
                            }
                        }
                        Toast.makeText(this, "Video saved to Movies / DUMA SNAP", Toast.LENGTH_LONG).show()
                    } else {
                        Log.e(TAG, "Video recording failed: ${event.error}")
                        Toast.makeText(this, "Video recording failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                cameraProvider = future.get()
                val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(flashMode)
                    .build()
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.fromOrderedList(
                            listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                            QualitySelector.FALLBACK_STRATEGY_LOWER
                        )
                    ).build()
                videoCapture = VideoCapture.withOutput(recorder)
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(this, selector, preview, imageCapture, videoCapture)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to start camera", exception)
                Toast.makeText(this, "Camera could not start on this device", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        recording?.stop()
        cameraExecutorShutdown()
        super.onDestroy()
    }

    private fun cameraExecutorShutdown() {
        // CameraX work is lifecycle-bound; no dedicated executor is required.
    }
}
