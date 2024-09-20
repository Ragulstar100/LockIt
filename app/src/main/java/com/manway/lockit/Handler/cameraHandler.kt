package com.manway.lockit.Handler


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getMainExecutor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

public interface FaceListener {
    companion object{
        var faceDetectorOptions= FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL).build()
        var detecter: FaceDetector = FaceDetection.getClient(faceDetectorOptions)

    }

    public fun Bitmap.analyzeBitmap(context: Context):Array<FloatArray>{
        var faceOutputArray = Array(1) { FloatArray(192) }

        val image= InputImage.fromBitmap(this,0)
        detecter.process(image).addOnSuccessListener { faces->

            val list=ArrayList<Bitmap>()
            val reacts=ArrayList<Rect>()
            val vectorList=ArrayList<Array<FloatArray>>()
            for (face in faces){
                list.add(cropBitmap(face.boundingBox))
                reacts.add(face.boundingBox)
                vectorList.add( (cropBitmap(faces.get(0).boundingBox)).convertToTensor(context))
                Rect(face.boundingBox.left,face.boundingBox.top,face.boundingBox.right,face.boundingBox.bottom)
            }

        }
            .addOnFailureListener {
            }

        return faceOutputArray
    }

    public fun Bitmap.analyzeBitmapValue(context: Context):ArrayList<Bitmap>{
        val list=ArrayList<Bitmap>()

        val image= InputImage.fromBitmap(this,0)
        detecter.process(image).addOnSuccessListener { faces->


            val reacts=ArrayList<Rect>()
            val vectorList=ArrayList<Array<FloatArray>>()
            for (face in faces){
                list.add(cropBitmap(face.boundingBox))
                reacts.add(face.boundingBox)
                vectorList.add( (cropBitmap(faces.get(0).boundingBox)).convertToTensor(context))
                Rect(face.boundingBox.left,face.boundingBox.top,face.boundingBox.right,face.boundingBox.bottom)
            }

        }
            .addOnFailureListener {
            }

        return list
    }

    public fun Bitmap.getOneFaceData(listener:(Rect)->Unit){
        val list=ArrayList<Rect>()
        val image= InputImage.fromBitmap(this,0)
        detecter.process(image).addOnSuccessListener { faces->
            for (face in faces){
                list.add(face.boundingBox)
            }
            if(list.size==1) listener(list[0])

        }.addOnFailureListener {

        }


    }

    fun Bitmap.cropBitmap(boundingBox:Rect):Bitmap{
        if(boundingBox.top >= 0 && boundingBox.bottom <= getWidth() && boundingBox.top + boundingBox.height() <= getHeight() && boundingBox.left >= 0 && boundingBox.left + boundingBox.width() <= getWidth())
            return Bitmap.createBitmap(this,boundingBox.left,boundingBox.top,boundingBox.width(),boundingBox.height())
        else return Bitmap.createBitmap(1024,1024,Bitmap.Config.ARGB_8888)
    }

    fun Bitmap.convertToTensor(context: Context):Array<FloatArray>{
        var faceNetImageProcessor: ImageProcessor? = ImageProcessor.Builder().add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR)).add(NormalizeOp(0f, 255f)).build()
        val tensorImage = TensorImage.fromBitmap(this)
        var faceNetByteBuffer= faceNetImageProcessor?.process(tensorImage)?.getBuffer()
        val  faceNetInterpreter = Interpreter(FileUtil.loadMappedFile(context, "mobile_face_net.tflite"))
        val faceOutputArray = Array(1) { FloatArray(192) }
        try {
            faceNetInterpreter.run(faceNetByteBuffer,faceOutputArray)
        }
        catch (e:Exception){

        }
        return faceOutputArray

    }

    fun Context.calculateFaceMatchFactor(bitmap1: Bitmap,bitmap2: Bitmap):Double{
        return calculateFaceMatchFactor(bitmap1.convertToTensor(this),bitmap2.convertToTensor(this))
    }

    fun calculateFaceMatchFactor(fv1: Array<FloatArray>,fv2: Array<FloatArray>): Double {
        var sum=0f
        for(i in fv1.indices){
            val k=fv2[0][i]-fv1[0][i]
            sum+=(k*k)
        }
        var rate=Math.sqrt(Math.sqrt(sum.toDouble()))
        return Math.sqrt(rate)
    }

    fun Context.calculateCosineSimilarity(bitmap1: Bitmap, bitmap2: Bitmap): Float{
        return calculateCosineSimilarity(bitmap1.convertToTensor(this)[0],bitmap2.convertToTensor(this)[0])
    }


    fun calculateCosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        // Ensure both vectors have the same length
        require(!(vector1.size != vector2.size || vector1.size == 0)) { "Vectors must have the same non-zero length" }

        // Calculate dot product
        var dotProduct = 0f
        for (i in vector1.indices) {
            dotProduct += vector1[i] * vector2[i]
        }

        // Calculate magnitudes
        val magnitude1 = calculateMagnitude(vector1)
        val magnitude2 = calculateMagnitude(vector2)

        // Calculate cosine similarity
        val cosineSimilarity = if (magnitude1 != 0f && magnitude2 != 0f) {
            dotProduct / (magnitude1 * magnitude2)
        } else {
            0f // Handle zero vector case
        }

        return cosineSimilarity
    }

    private fun calculateMagnitude(vector: FloatArray): Float {
        var sum = 0f
        for (value in vector) {
            sum += value * value
        }
        return sqrt(sum.toDouble()).toFloat()
    }


}

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier = Modifier){
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView({
        PreviewView(it).apply {
            this.controller=controller
            controller.bindToLifecycle(lifecycleOwner)
        }
    },modifier)
}

public fun takePhoto(context: Context, controller: LifecycleCameraController, onPhotoTaken:(Bitmap)->Unit){
    //   controller.cameraSelector= CameraSelector.DEFAULT_FRONT_CAMERA

    controller.takePicture(getMainExecutor(context),object: OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)
            onPhotoTaken(image.toBitmap())
            image.close()
        }
    }
    )

}

fun File.getFrameFromVideo( timeInMs: Long): Bitmap?{
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(path)
        retriever.getFrameAtTime(timeInMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
    } catch (e: Exception) {
        null
    } finally {
        retriever.release()
    }
}

class RVideoCapture(var context: Context){

    var recording: Recording?=null

    @SuppressLint("MissingPermission")
    fun recordVideo(controller: LifecycleCameraController, outputFile:(File)->Unit) {

        if(recording!=null){
            recording?.stop()
            recording=null
            return
        }

        val file = File(context.filesDir, "test.mp4")
        recording = controller.startRecording(FileOutputOptions.Builder(file).build(), AudioConfig.create(true), getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        recording?.close()
                        recording=null
                    } else {
                        Toast.makeText(context, "capture", Toast.LENGTH_SHORT).show()
                        outputFile(file)

                    }
                }
            }
        }

    }





}

fun FaceListenerScope(listener: FaceListener.()->Unit){
    listener(object :FaceListener{})
}

fun Bitmap.toByteArray():ByteArray{
    var steam=ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG,100,steam)
    return steam.toByteArray()

}

fun ByteArray.toBitmap():Bitmap{
    return BitmapFactory.decodeByteArray(this,0,size)
}
