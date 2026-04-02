package com.hazard.koe.presentation.transactionmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.core.graphics.toColorInt
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.vanniktech.emoji.EmojiTextView

fun createEmojiCircleMarker(
    context: Context,
    emoji: String,
    colorHex: String,
    sizeDp: Int = 52,
    emojiTextSizeSp: Float = 22f
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val size = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorHex.toColorInt()
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.9f, fillPaint)

    val emojiView = EmojiTextView(context).apply {
        text = emoji
        textSize = emojiTextSizeSp
        measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    canvas.save()
    canvas.translate(size / 2f - emojiView.measuredWidth / 2f, size / 2f - emojiView.measuredHeight / 2f)
    emojiView.draw(canvas)
    canvas.restore()

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
