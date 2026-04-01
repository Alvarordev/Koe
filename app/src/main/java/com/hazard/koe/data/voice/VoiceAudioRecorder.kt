package com.hazard.koe.data.voice

data class RecordedVoiceAudio(
    val bytes: ByteArray,
    val mimeType: String
)

interface VoiceAudioRecorder {
    fun startRecording(): Result<Unit>
    fun stopRecording(): Result<RecordedVoiceAudio>
    fun cancelRecording()
    fun release()
}
