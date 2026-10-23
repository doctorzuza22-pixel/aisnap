package com.aisnap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isVideoMode = false
    private var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        if (allPermissionsGranted()) startCamera() 
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 10)

        findViewById<Button>(R.id.captureButton).setOnClickListener { if(isVideoMode) recordVideo() else takePhoto() }
        findViewById<Button>(R.id.switchCameraButton).setOnClickListener { 
            lensFacing = if(lensFacing==CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera() 
        }
        findViewById<Button>(R.id.flashButton).setOnClickListener {
            flashMode = when(flashMode){
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            imageCapture?.flashMode = flashMode
            Toast.makeText(this, if(flashMode==0) "Flash OFF" else if(flashMode==1) "Flash ON" else "Flash AUTO", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.zoom05).setOnClickListener { camera?.cameraControl?.setLinearZoom(0f) }
        findViewById<Button>(R.id.zoom1x).setOnClickListener { camera?.cameraControl?.setLinearZoom(0.25f) }
        findViewById<Button>(R.id.zoom2x).setOnClickListener { camera?.cameraControl?.setLinearZoom(0.5f) }
        findViewById<Button>(R.id.zoom5x).setOnClickListener { camera?.cameraControl?.setLinearZoom(0.9f) }
        findViewById<Button>(R.id.modePhoto).setOnClickListener { isVideoMode = false; Toast.makeText(this,"PHOTO mode",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.modeVideo).setOnClickListener { isVideoMode = true; Toast.makeText(this,"VIDEO mode - tap CAP to record",Toast.LENGTH_SHORT).show() }
    }

    private fun takePhoto(){
        val imageCapture = imageCapture ?: return
        val photoFile = File(externalMediaDirs.firstOrNull(), SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())+".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) { Log.e("AISnap","Photo failed: ${exc.message}", exc) }
            override fun onImageSaved(output: ImageCapture.OutputFileResults){ Toast.makeText(baseContext, "Photo Saved: ${photoFile.name}", Toast.LENGTH_SHORT).show() }
        })
    }
    
    private fun recordVideo(){
        val videoCapture = this.videoCapture ?: return
        if(recording != null){ recording?.stop(); recording=null; Toast.makeText(this,"Video Saved",Toast.LENGTH_SHORT).show(); return }
        val videoFile = File(externalMediaDirs.firstOrNull(), SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())+".mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()
        recording = videoCapture.output.prepareRecording(this, outputOptions).start(ContextCompat.getMainExecutor(this)){ event ->
            if(event is VideoRecordEvent.Finalize){ if(!event.hasError()) Toast.makeText(this,"Video saved: ${videoFile.name}",Toast.LENGTH_SHORT).show() }
        }
        Toast.makeText(this,"Recording...",Toast.LENGTH_SHORT).show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build().also { it.flashMode = flashMode }
            val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture)
            } catch(exc: Exception) { Log.e("AISnap", "Use case binding failed", exc) }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onDestroy() { super.onDestroy(); cameraExecutor.shutdown() }
}
