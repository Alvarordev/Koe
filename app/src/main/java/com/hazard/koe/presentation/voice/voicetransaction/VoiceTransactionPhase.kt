package com.hazard.koe.presentation.voice.voicetransaction

enum class VoiceTransactionPhase {
    IDLE,
    RECORDING,
    PROCESSING,
    CONFIRM,
    MANUAL_EDIT,
    SUCCESS,
    ERROR
}
