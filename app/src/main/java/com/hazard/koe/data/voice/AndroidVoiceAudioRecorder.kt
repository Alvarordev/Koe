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

    override fun startRecording(): Result<Unit> {
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
        }
    }

    override fun stopRecording(): Result<RecordedVoiceAudio> {
        return runCatching {
            val recorder = mediaRecorder
            val file = outputFile

            if (!isRecording || recorder == null || file == null) {
                throw IllegalStateException("No hay grabación activa")
            }

            recorder.stop()
            recorder.reset()
            recorder.release()

            mediaRecorder = null
            isRecording = false

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
        runCatching {
            mediaRecorder?.reset()
        }
        runCatching {
            mediaRecorder?.release()
        }
        mediaRecorder = null
        isRecording = false

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
