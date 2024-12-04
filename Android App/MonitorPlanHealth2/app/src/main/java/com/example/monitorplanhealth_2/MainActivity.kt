package com.example.monitorplanhealth_2

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var predictionTextView: TextView
    private lateinit var previewView: PreviewView
    private lateinit var interpreter: Interpreter

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                android.util.Log.e("MainActivity", "Camera permission was denied. The app cannot proceed without it.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        predictionTextView = findViewById(R.id.predictionTextView)
        previewView = findViewById(R.id.previewView)

        val modelFile = loadModelFromAssets(assets)
        val options = Interpreter.Options().apply {
            numThreads = 4
        }
        interpreter = Interpreter(modelFile, options)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        analyzeImage(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmapFromImageProxy()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, true) // Resize to match Edge Impulse model input

        val result = predict(resizedBitmap)

        runOnUiThread {
            predictionTextView.text = result
        }

        imageProxy.close()
    }

    private fun predict(bitmap: Bitmap): String {
        val inputBuffer = ByteBuffer.allocateDirect(4 * 96 * 96 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(96 * 96)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixelIndex = 0
        for (i in 0 until 96) {
            for (j in 0 until 96) {
                val pixelValue = intValues[pixelIndex++]
                inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
                inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
                inputBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))
            }
        }

        val outputBuffer = Array(1) { FloatArray(2) }

        interpreter.run(inputBuffer, outputBuffer)


        return if (outputBuffer[0][1] > outputBuffer[0][0]) "Healthy" else "Diseased"
    }



    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }

    private fun ImageProxy.toBitmapFromImageProxy(): Bitmap {
        val yBuffer = this.planes[0].buffer
        val uBuffer = this.planes[1].buffer
        val vBuffer = this.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 100, out)
        val byteArray = out.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun loadModelFromAssets(assetManager: AssetManager): ByteBuffer {
        val inputStream = assetManager.open("ei-monitor-plant-health-transfer-learning-tensorflow-lite-float32-model.lite")

        val byteArray = inputStream.readBytes()

        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size)
        byteBuffer.put(byteArray)
        byteBuffer.rewind()

        return byteBuffer
    }
}
