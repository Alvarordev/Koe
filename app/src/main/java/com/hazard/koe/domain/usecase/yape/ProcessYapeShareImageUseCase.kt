package com.hazard.koe.domain.usecase.yape

import android.net.Uri
import com.hazard.koe.data.db.dao.ProcessedNotificationDao
import com.hazard.koe.data.ocr.YapeImageOcrProcessor
import com.hazard.koe.domain.exception.DuplicateTransactionException
import com.hazard.koe.domain.model.YapeOcrResult

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
