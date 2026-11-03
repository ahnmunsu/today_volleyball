package com.example.todayvolleyball.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LazyColumn에 세로 스크롤바(트랙 및 Thumb)를 그리는 Modifier 확장 함수.
 * 스크롤이 필요 없는 경우(모든 아이템이 화면에 다 노출되는 경우)에는 스크롤바를 표시하지 않습니다.
 */
fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 6.dp,
    thumbColor: Color,
    trackColor: Color = Color.Transparent,
    cornerRadius: Dp = 3.dp,
    paddingEnd: Dp = 2.dp
): Modifier = this.drawWithContent {
    drawContent()

    val layoutInfo = state.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    val visibleItemsInfo = layoutInfo.visibleItemsInfo

    if (totalItemsCount == 0 || visibleItemsInfo.isEmpty()) return@drawWithContent

    val firstVisibleItem = visibleItemsInfo.first()
    val lastVisibleItem = visibleItemsInfo.last()

    // 모든 아이템이 한 화면에 다 보이는지 판단
    val isAllVisible = firstVisibleItem.index == 0 &&
            lastVisibleItem.index == totalItemsCount - 1 &&
            firstVisibleItem.offset >= layoutInfo.viewportStartOffset &&
            (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset

    if (isAllVisible) return@drawWithContent

    val viewportHeight = layoutInfo.viewportSize.height.toFloat()
    if (viewportHeight <= 0f) return@drawWithContent

    val widthPx = width.toPx()
    val paddingEndPx = paddingEnd.toPx()
    val cornerRadiusPx = cornerRadius.toPx()
    val xOffset = size.width - widthPx - paddingEndPx

    // 트랙 그리기 (트랙 색상이 지정된 경우)
    if (trackColor != Color.Transparent && trackColor.alpha > 0f) {
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(xOffset, 0f),
            size = Size(widthPx, viewportHeight),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
    }

    // 아이템들의 평균 높이를 바탕으로 전체 높이 추정
    val totalItemSizes = visibleItemsInfo.sumOf { it.size }
    val avgItemSize = totalItemSizes.toFloat() / visibleItemsInfo.size
    val estimatedTotalHeight = avgItemSize * totalItemsCount

    val thumbHeightPx = (viewportHeight / estimatedTotalHeight * viewportHeight)
        .coerceIn(32.dp.toPx(), viewportHeight)

    val maxScrollOffset = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)
    val currentScrollOffset = (state.firstVisibleItemIndex * avgItemSize + state.firstVisibleItemScrollOffset)
        .coerceIn(0f, maxScrollOffset)

    val scrollFraction = (currentScrollOffset / maxScrollOffset).coerceIn(0f, 1f)
    val thumbOffsetY = scrollFraction * (viewportHeight - thumbHeightPx)

    // Thumb 그리기
    if (thumbColor.alpha > 0f) {
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(xOffset, thumbOffsetY),
            size = Size(widthPx, thumbHeightPx),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
    }
}
