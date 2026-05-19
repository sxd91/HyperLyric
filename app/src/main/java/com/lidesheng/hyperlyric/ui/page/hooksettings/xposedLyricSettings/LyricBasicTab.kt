package com.lidesheng.hyperlyric.ui.page.hooksettings.xposedLyricSettings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lidesheng.hyperlyric.ui.utils.pageScrollModifiers
import top.yukonga.miuix.kmp.basic.ScrollBehavior

@Composable
fun LyricBasicTab(
    lazyListState: LazyListState,
    topAppBarScrollBehavior: ScrollBehavior,
    contentPadding: PaddingValues,
    lyricMode: Int,
    textSize: Int,
    onTextSizeClick: () -> Unit,
    textSizeRatio: Float,
    onTextSizeRatioClick: () -> Unit,
    fadingEdge: Int,
    onFadingEdgeChange: (Int) -> Unit,
    onFadingEdgeClick: () -> Unit,
    extractCoverColor: Boolean,
    onExtractCoverColorChange: (Boolean) -> Unit,
    extractCoverGradient: Boolean,
    onExtractCoverGradientChange: (Boolean) -> Unit,
    customFontPath: String,
    onFontPathClick: () -> Unit,
    fontWeight: Int,
    onFontWeightClick: () -> Unit,
    fontItalic: Boolean,
    onFontItalicChange: (Boolean) -> Unit,
    centerLyric: Boolean,
    onCenterLyricChange: (Boolean) -> Unit,
    marqueeMode: Boolean,
    onMarqueeModeChange: (Boolean) -> Unit,
    marqueeSpeed: Int,
    onMarqueeSpeedClick: () -> Unit,
    marqueeDelay: Int,
    onMarqueeDelayClick: () -> Unit,
    marqueeInfinite: Boolean,
    onMarqueeInfiniteChange: (Boolean) -> Unit,
    marqueeLoop: Int,
    onMarqueeLoopClick: () -> Unit,
    marqueeStopEnd: Boolean,
    onMarqueeStopEndChange: (Boolean) -> Unit,
    marqueeMetadataMode: Boolean,
    onMarqueeMetadataModeChange: (Boolean) -> Unit,
    marqueeMetadataSpeed: Int,
    onMarqueeMetadataSpeedClick: () -> Unit,
    marqueeMetadataDelay: Int,
    onMarqueeMetadataDelayClick: () -> Unit,
    marqueeMetadataInfinite: Boolean,
    onMarqueeMetadataInfiniteChange: (Boolean) -> Unit,
    marqueeMetadataLoopDelay: Int,
    onMarqueeMetadataLoopClick: () -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.pageScrollModifiers(
            enableScrollEndHaptic = true,
            showTopAppBar = true,
            topAppBarScrollBehavior = topAppBarScrollBehavior
        ),
        contentPadding = contentPadding,
    ) {
        basicSections(
            lyricMode = lyricMode,
            textSize = textSize,
            onTextSizeClick = onTextSizeClick,
            textSizeRatio = textSizeRatio,
            onTextSizeRatioClick = onTextSizeRatioClick,
            fadingEdge = fadingEdge,
            onFadingEdgeChange = onFadingEdgeChange,
            onFadingEdgeClick = onFadingEdgeClick,
            extractCoverColor = extractCoverColor,
            onExtractCoverColorChange = onExtractCoverColorChange,
            extractCoverGradient = extractCoverGradient,
            onExtractCoverGradientChange = onExtractCoverGradientChange,
            customFontPath = customFontPath,
            onFontPathClick = onFontPathClick,
            fontWeight = fontWeight,
            onFontWeightClick = onFontWeightClick,
            fontItalic = fontItalic,
            onFontItalicChange = onFontItalicChange,
            centerLyric = centerLyric,
            onCenterLyricChange = onCenterLyricChange,
            marqueeMode = marqueeMode,
            onMarqueeModeChange = onMarqueeModeChange,
            marqueeSpeed = marqueeSpeed,
            onMarqueeSpeedClick = onMarqueeSpeedClick,
            marqueeDelay = marqueeDelay,
            onMarqueeDelayClick = onMarqueeDelayClick,
            marqueeInfinite = marqueeInfinite,
            onMarqueeInfiniteChange = onMarqueeInfiniteChange,
            marqueeLoop = marqueeLoop,
            onMarqueeLoopClick = onMarqueeLoopClick,
            marqueeStopEnd = marqueeStopEnd,
            onMarqueeStopEndChange = onMarqueeStopEndChange,
            marqueeMetadataMode = marqueeMetadataMode,
            onMarqueeMetadataModeChange = onMarqueeMetadataModeChange,
            marqueeMetadataSpeed = marqueeMetadataSpeed,
            onMarqueeMetadataSpeedClick = onMarqueeMetadataSpeedClick,
            marqueeMetadataDelay = marqueeMetadataDelay,
            onMarqueeMetadataDelayClick = onMarqueeMetadataDelayClick,
            marqueeMetadataInfinite = marqueeMetadataInfinite,
            onMarqueeMetadataInfiniteChange = onMarqueeMetadataInfiniteChange,
            marqueeMetadataLoopDelay = marqueeMetadataLoopDelay,
            onMarqueeMetadataLoopClick = onMarqueeMetadataLoopClick
        )
    }
}
