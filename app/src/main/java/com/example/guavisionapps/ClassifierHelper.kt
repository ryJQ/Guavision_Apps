package com.example.guavisionapps

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.text.format

class ClassifierHelper(private val context: Context) {
    private val modelName = "guavadisease_cnn_model.tflite"
    private val imageSize = 224
    private val labels = arrayOf( "Anthracnose", "fruit_fly", "healthy_guava")
    private var interpreter: Interpreter? = null
    private val TAG = "ClassifierHelper"
    init {
        try {
            val model = loadModelFile(context, modelName)
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            Log.i(TAG, "Interpreter TFLite berhasil diinisialisasi.")
        } catch (e: IOException) {
            Log.e(TAG, "Gagal memuat model TFLite atau menginisialisasi Interpreter: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error tidak terduga saat inisialisasi: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelFileName: String): MappedByteBuffer {
        try {
            return FileUtil.loadMappedFile(context, modelFileName)
        } catch (e: IOException) {
            Log.w(TAG, "Gagal memuat model dengan FileUtil, mencoba cara manual: ${e.message}")
            val fileDescriptor = context.assets.openFd(modelFileName)
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    fun classify(bitmap: Bitmap, callback: (String, Map<String, Float>) -> Unit) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter belum diinisialisasi. Klasifikasi dibatalkan.")
            callback("Error: Interpreter not initialized.", emptyMap())
            return
        }
        Thread {
            var scaledBitmap: Bitmap? = null
            try {
                Log.d(TAG, "Memulai klasifikasi untuk bitmap: ${bitmap.width}x${bitmap.height}")
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
                Log.d(TAG, "Bitmap di-scaled menjadi: ${scaledBitmap.width}x${scaledBitmap.height}")

                val inputBuffer = convertBitmapToByteBuffer(scaledBitmap)

                val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
                inputTensor.loadBuffer(inputBuffer)
                val outputTensor = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), DataType.FLOAT32)
                Log.d(TAG, "Menjalankan inferensi model...")
                val startTime = System.currentTimeMillis()
                interpreter?.run(inputTensor.buffer, outputTensor.buffer.rewind())
                val endTime = System.currentTimeMillis()
                Log.d(TAG, "Inferensi selesai dalam ${endTime - startTime} ms")
                val confidences = outputTensor.floatArray
                logDetailedConfidences(confidences)
                val maxIdx = confidences.indices.maxByOrNull { confidences[it] } ?: -1
                val resultString: String
                val finalConfidenceMap: Map<String, Float>
                if (maxIdx != -1 && maxIdx < labels.size) {
                    resultString = "${labels[maxIdx]} (Confidence: %.2f%%)".format(confidences[maxIdx] * 100)
                    finalConfidenceMap = confidences.mapIndexed { index, score ->
                        if (index < labels.size) labels[index] to (score * 100) else "Unknown_$index" to (score * 100)
                    }.toMap()
                    Log.i(TAG, "Prediksi: $resultString")
                } else {
                    resultString = "Tidak terdeteksi atau indeks error"
                    finalConfidenceMap = emptyMap()
                    Log.w(TAG, "Tidak ada prediksi valid atau maxIdx di luar batas: $maxIdx, jumlah label: ${labels.size}")
                }
                callback(resultString, finalConfidenceMap)
            } catch (e: Exception) {
                Log.e(TAG, "Error kritis selama klasifikasi: ${e.message}", e)
                callback("Error during classification: ${e.message}", emptyMap())
            } finally {
                if (scaledBitmap != null && scaledBitmap != bitmap && !scaledBitmap.isRecycled) {
                }
            }
        }.start()
    }
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * imageSize * imageSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel++]
                val r_float = ((value shr 16) and 0xFF).toFloat()
                val g_float = ((value shr 8) and 0xFF).toFloat()
                val b_float = (value and 0xFF).toFloat()
                byteBuffer.putFloat(r_float)
                byteBuffer.putFloat(g_float)
                byteBuffer.putFloat(b_float)
            }
        }
        return byteBuffer
    }
    private fun logDetailedConfidences(confidences: FloatArray) {
        Log.d(TAG, "--- Skor Kepercayaan Mentah (setelah inferensi) ---")
        if (confidences.size == labels.size) {
            for (i in labels.indices) {
                Log.d(TAG, "Label: ${labels[i]}, Skor: %.4f (%.2f%%)".format(confidences[i], confidences[i] * 100))
            }
        } else {
            Log.w(TAG, "Ukuran array confidences (${confidences.size}) tidak cocok dengan jumlah label (${labels.size})!")
            confidences.forEachIndexed { index, score ->
                Log.d(TAG, "Conf[$index]: %.4f (%.2f%%)".format(score, score * 100))
            }
        }
        Log.d(TAG, "-------------------------------------------------")
    }
    private fun logInputBufferValues(buffer: ByteBuffer, bufferName: String, count: Int = 30) {
        val originalPosition = buffer.position()
        val sb = StringBuilder()
        sb.append("$bufferName (showing first $count float values, total size: ${buffer.limit() / 4}):\n")
        val numFloatsToRead = minOf(count, buffer.remaining() / 4)
        for (i in 0 until numFloatsToRead) {
            if (buffer.hasRemaining() && buffer.remaining() >= 4) {
                val floatValue = buffer.float
                sb.append("%.4f".format(floatValue))
                if (i < numFloatsToRead - 1) {
                    sb.append(", ")
                }
            } else {
                sb.append("\n[Buffer ended prematurely at float $i]")
                break
            }
        }
        sb.append("\n...")
        Log.d(TAG, sb.toString())
        buffer.position(originalPosition)
    }
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.i(TAG, "Interpreter TFLite ditutup.")
    }
}

