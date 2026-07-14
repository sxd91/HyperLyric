package com.lidesheng.hyperlyric.ui.page.hooksettings.media.notification

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lidesheng.hyperlyric.R
import com.lidesheng.hyperlyric.common.RootConstants
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

fun LazyListScope.notificationCenterMediaCardSection(
    cardTheme: Int,
    onCardThemeChange: (Int) -> Unit,
    coverStyle: Int,
    onCoverStyleChange: (Int) -> Unit,
    hideCoverSource: Boolean,
    onHideCoverSourceChange: (Boolean) -> Unit,
    hideDeviceSwitch: Boolean,
    onHideDeviceSwitchChange: (Boolean) -> Unit,
    backgroundStyle: Int,
    onBackgroundStyleChange: (Int) -> Unit,
    backgroundColorAnimation: Boolean,
    onBackgroundColorAnimationChange: (Boolean) -> Unit,
    backgroundAutoInvert: Boolean,
    onBackgroundAutoInvertChange: (Boolean) -> Unit,
    ambientFlowMode: Int,
    onAmbientFlowModeChange: (Int) -> Unit
) {
    item(key = "notification_center_media_card") {
        SmallTitle(text = stringResource(R.string.title_notification_center_media_card))
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .fillMaxWidth()
        ) {
            val coverStyleValues = listOf(
                RootConstants.NOTIFICATION_MEDIA_COVER_STYLE_DEFAULT,
                RootConstants.NOTIFICATION_MEDIA_COVER_STYLE_CIRCLE,
                RootConstants.NOTIFICATION_MEDIA_COVER_STYLE_ROTATING_CIRCLE,
                RootConstants.NOTIFICATION_MEDIA_COVER_STYLE_HIDDEN
            )
            OverlayDropdownPreference(
                title = stringResource(R.string.title_audio_cover_style),
                items = listOf(
                    stringResource(R.string.option_audio_cover_style_default),
                    stringResource(R.string.option_audio_cover_style_circle),
                    stringResource(R.string.option_audio_cover_style_rotating_circle),
                    stringResource(R.string.option_audio_cover_style_hidden)
                ),
                selectedIndex = coverStyleValues.indexOf(coverStyle).coerceAtLeast(0),
                onSelectedIndexChange = { index ->
                    onCoverStyleChange(coverStyleValues[index])
                }
            )
            SwitchPreference(
                title = stringResource(R.string.title_hide_audio_cover_source),
                checked = hideCoverSource,
                onCheckedChange = onHideCoverSourceChange
            )
            SwitchPreference(
                title = stringResource(R.string.title_hide_media_device_switch),
                checked = hideDeviceSwitch,
                onCheckedChange = onHideDeviceSwitchChange
            )
            val backgroundStyleValues = listOf(
                RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_DEFAULT,
                RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_COVER_ART,
                RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_BLURRED_COVER,
                RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_RADIAL_GRADIENT,
                RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_LINEAR_GRADIENT
            )
            OverlayDropdownPreference(
                title = stringResource(R.string.title_notification_media_background_style),
                items = listOf(
                    stringResource(R.string.option_notification_media_background_default),
                    stringResource(R.string.option_notification_media_background_cover_art),
                    stringResource(R.string.option_notification_media_background_blurred_cover),
                    stringResource(R.string.option_notification_media_background_radial_gradient),
                    stringResource(R.string.option_notification_media_background_linear_gradient)
                ),
                selectedIndex = backgroundStyleValues.indexOf(backgroundStyle).coerceAtLeast(0),
                onSelectedIndexChange = { index ->
                    onBackgroundStyleChange(backgroundStyleValues[index])
                }
            )
            val customBackground =
                backgroundStyle != RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_DEFAULT
            SwitchPreference(
                title = stringResource(R.string.title_notification_media_background_color_animation),
                checked = backgroundColorAnimation,
                enabled = customBackground,
                onCheckedChange = onBackgroundColorAnimationChange
            )
            SwitchPreference(
                title = stringResource(R.string.title_notification_media_background_auto_invert),
                checked = backgroundAutoInvert,
                enabled = backgroundStyle ==
                    RootConstants.NOTIFICATION_MEDIA_BACKGROUND_STYLE_LINEAR_GRADIENT,
                onCheckedChange = onBackgroundAutoInvertChange
            )
            val themeValues = listOf(
                RootConstants.MEDIA_CARD_THEME_FOLLOW_SYSTEM,
                RootConstants.MEDIA_CARD_THEME_ALWAYS_LIGHT,
                RootConstants.MEDIA_CARD_THEME_ALWAYS_DARK
            )
            OverlayDropdownPreference(
                title = stringResource(R.string.title_media_card_background_theme),
                items = listOf(
                    stringResource(R.string.option_media_card_theme_follow_system_default),
                    stringResource(R.string.option_media_card_theme_always_light),
                    stringResource(R.string.option_media_card_theme_always_dark)
                ),
                selectedIndex = themeValues.indexOf(cardTheme).coerceAtLeast(0),
                enabled = !customBackground,
                onSelectedIndexChange = { index ->
                    onCardThemeChange(themeValues[index])
                }
            )
            val modeValues = listOf(
                RootConstants.NOTIFICATION_MEDIA_AMBIENT_FLOW_MODE_DYNAMIC,
                RootConstants.NOTIFICATION_MEDIA_AMBIENT_FLOW_MODE_COVER_COLOR,
                RootConstants.NOTIFICATION_MEDIA_AMBIENT_FLOW_MODE_DISABLED
            )
            OverlayDropdownPreference(
                title = stringResource(R.string.title_notification_media_ambient_flow_mode),
                items = listOf(
                    stringResource(R.string.option_notification_media_ambient_flow_dynamic),
                    stringResource(R.string.option_notification_media_ambient_flow_cover_color),
                    stringResource(R.string.option_notification_media_ambient_flow_disabled)
                ),
                selectedIndex = modeValues.indexOf(ambientFlowMode).coerceAtLeast(0),
                enabled = !customBackground,
                onSelectedIndexChange = { index ->
                    onAmbientFlowModeChange(modeValues[index])
                }
            )
        }
    }
}
