package com.aisnap.camera.pro
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import java.util.concurrent.Executors
class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isVideoMode = false
    private var recording: Recording? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 10)
        findViewById<Button>(R.id.captureButton).setOnClickListener { if(isVideoMode) recordVideo() else takePhoto() }
        findViewById<Button>(R.id.switchCameraButton).setOnClickListener { lensFacing = if(lensFacing==CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK; startCamera() }
        findViewById<Button>(R.id.flashButton).setOnClickListener { flashMode = when(flashMode){0->1; 1->2; else->0}; imageCapture?.flashMode=flashMode; Toast.makeText(this, arrayOf("Flash OFF","Flash ON","Flash AUTO")[flashMode], Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.zoom05).setOnClickListener { camera?.cameraControl?.setLinearZoom(0f) }
        findViewById<Button>(R.id.zoom1x).setOnClickListener { camera?.cameraControl?.setLinearZoom(0.3f) }
        findViewById<Button>(R.id.zoom2x).setOnClickListener { camera?.cameraControl?.setLinearZoom(0.6f) }
        findViewById<Button>(R.id.zoom5x).setOnClickListener { camera?.cameraControl?.setLinearZoom(1f) }
        findViewById<Button>(R.id.modePhoto).setOnClickListener { isVideoMode=false }
        findViewById<Button>(R.id.modeVideo).setOnClickListener { isVideoMode=true; Toast.makeText(this,"VIDEO mode",Toast.LENGTH_SHORT).show() }
    }
    private fun takePhoto(){
        val file = File(externalMediaDirs.firstOrNull(), SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(System.currentTimeMillis())+".jpg")
        imageCapture?.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageSavedCallback{
            override fun onError(e: ImageCaptureException){ Log.e("CAM","Error",e) }
            override fun onImageSaved(o: ImageCapture.OutputFileResults){ Toast.makeText(this@MainActivity, "Saved "+file.name, Toast.LENGTH_SHORT).show() }
        })
    }
    private fun recordVideo(){
        if(recording!=null){ recording?.stop(); recording=null; return }
        val file = File(externalMediaDirs.firstOrNull(), SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(System.currentTimeMillis())+".mp4")
        recording = videoCapture?.output?.prepareRecording(this, FileOutputOptions.Builder(file).build())?.start(ContextCompat.getMainExecutor(this)){ if(it is VideoRecordEvent.Finalize) Toast.makeText(this, "Video saved", Toast.LENGTH_SHORT).show() }
    }
    private fun startCamera(){
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            val preview = Preview.Builder().build().also{ it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build().also{ it.flashMode=flashMode }
            videoCapture = VideoCapture.withOutput(Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build())
            try{ cameraProvider?.unbindAll(); camera = cameraProvider?.bindToLifecycle(this, CameraSelector.Builder().requireLensFacing(lensFacing).build(), preview, imageCapture, videoCapture) }catch(e:Exception){ Log.e("CAM","bind",e) }
        }, ContextCompat.getMainExecutor(this))
    }
}
