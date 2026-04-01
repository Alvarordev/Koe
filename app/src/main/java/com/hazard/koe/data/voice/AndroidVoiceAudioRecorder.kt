package com.hazard.koe.data.voice

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class AndroidVoiceAudioRecorder(
    private val context: Context
) : VoiceAudioRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording: Boolean = false
    private var rmsCallback: ((Float) -> Unit)? = null
    private var rmsThread: Thread? = null

    override fun startRecording(onRmsChanged: ((Float) -> Unit)?): Result<Unit> {
        return runCatching {
            if (isRecording) return@runCatching

            val file = File.createTempFile("voice_txn_", ".m4a", context.cacheDir)
            val recorder = createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64_000)
                setAudioSamplingRate(16_000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            outputFile = file
            mediaRecorder = recorder
            isRecording = true
            rmsCallback = onRmsChanged

            if (onRmsChanged != null) {
                rmsThread = Thread {
                    while (isRecording) {
                        try {
                            val maxAmplitude = recorder.maxAmplitude
                            val normalized = if (maxAmplitude > 0) {
                                (maxAmplitude / 32767f).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            onRmsChanged(normalized)
                            Thread.sleep(50)
                        } catch (_: Exception) {
                            break
                        }
                    }
                }.also { it.start() }
            }
        }
    }

    override fun stopRecording(): Result<RecordedVoiceAudio> {
        return runCatching {
            val recorder = mediaRecorder
            val file = outputFile

            if (!isRecording || recorder == null || file == null) {
                throw IllegalStateException("No hay grabación activa")
            }

            isRecording = false
            rmsThread?.join(200)
            rmsThread = null
            rmsCallback = null

            recorder.stop()
            recorder.reset()
            recorder.release()

            mediaRecorder = null

            val bytes = file.readBytes()
            file.delete()
            outputFile = null

            RecordedVoiceAudio(
                bytes = bytes,
                mimeType = "audio/mp4"
            )
        }.onFailure {
            cleanupRecorder(deleteFile = true)
        }
    }

    override fun cancelRecording() {
        cleanupRecorder(deleteFile = true)
    }

    override fun release() {
        cleanupRecorder(deleteFile = true)
    }

    private fun cleanupRecorder(deleteFile: Boolean) {
        isRecording = false
        rmsThread?.join(200)
        rmsThread = null
        rmsCallback = null

        runCatching {
            mediaRecorder?.reset()
        }
        runCatching {
            mediaRecorder?.release()
        }
        mediaRecorder = null

        if (deleteFile) {
            runCatching {
                outputFile?.delete()
            }
            outputFile = null
        }
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder {
        return MediaRecorder()
    }
}
