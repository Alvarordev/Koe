package com.example.tracker.presentation.accounts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.tracker.data.model.Account
import kotlinx.coroutines.flow.drop
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
fun AccountsCarousel(
    accounts: List<Account>,
    onAccountClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { accounts.size })
    val density = LocalDensity.current
    val pushOutPx = with(density) { 30.dp.toPx() }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .drop(1)
            .collect {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 0.dp
    ) { page ->
        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val absoluteOffset = pageOffset.absoluteValue
        val account = accounts[page]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f - absoluteOffset)
                .graphicsLayer {
                    val scale = 1f - (absoluteOffset * 0.3f).coerceIn(0f, 0.15f)
                    scaleX = scale
                    scaleY = scale

                    val baseTranslation = pageOffset * 0.85f * size.width

                    val pushOutProgress = if (absoluteOffset <= 0.5f) {
                        absoluteOffset / 0.5f
                    } else {
                        (1f - absoluteOffset) / 0.5f
                    }.coerceIn(0f, 1f)

                    val directionSign = -sign(pageOffset)
                    val pushOutTranslation = directionSign * pushOutProgress * pushOutPx

                    translationX = baseTranslation + pushOutTranslation
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .height(220.dp)
                    .aspectRatio(1.58f)
                    .clip(RoundedCornerShape(20.dp))
                    .drawWithContent {
                        drawContent()
                        val overlayAlpha = absoluteOffset.coerceIn(0f, 1f) * 0.5f
                        drawRect(Color.Black, alpha = overlayAlpha)
                    }
            ) {
                AccountCard(
                    account = account,
                    onClick = { onAccountClick(account.id) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}