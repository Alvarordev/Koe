package com.example.tracker.domain.usecase.yape

import android.net.Uri
import com.example.tracker.data.db.dao.ProcessedNotificationDao
import com.example.tracker.data.ocr.YapeImageOcrProcessor
import com.example.tracker.domain.exception.DuplicateTransactionException
import com.example.tracker.domain.model.YapeOcrResult

class ProcessYapeShareImageUseCase(
    private val ocrProcessor: YapeImageOcrProcessor,
    private val processedNotificationDao: ProcessedNotificationDao
) {
    suspend operator fun invoke(imageUri: Uri): Result<YapeOcrResult> {
        val result = ocrProcessor.extractFromUri(imageUri)
            ?: return Result.failure(Exception("No se pudo leer la imagen de Yape"))

        if (result.operationNumber != null) {
            if (processedNotificationDao.existsByOperationNumber(result.operationNumber)) {
                return Result.failure(DuplicateTransactionException(result.operationNumber))
            }
        }

        return Result.success(result)
    }
}
