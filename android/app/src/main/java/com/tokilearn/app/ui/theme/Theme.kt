package com.tokilearn.app.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.tokilearn.app.R
import com.tokilearn.app.data.ThemeMode
import com.tokiui.semantic.SemanticTokens

val LocalDarkTheme = staticCompositionLocalOf { true }

/** 拉丁 Inter + 中文 Noto Sans SC 回退。 */
val TokiFontFamily = FontFamily(Font(R.font.inter), Font(R.font.noto_sans_sc))

private fun Typography.withTokiType(family: FontFamily): Typography {
    // TokiUI 0.2.0-b1：标题 Bold、正文 Regular。
    fun TextStyle.title() = copy(fontFamily = family, fontWeight = FontWeight.Bold)
    fun TextStyle.body() = copy(fontFamily = family, fontWeight = FontWeight.Normal)
    return Typography(
        displayLarge = displayLarge.title(), displayMedium = displayMedium.title(), displaySmall = displaySmall.title(),
        headlineLarge = headlineLarge.title(), headlineMedium = headlineMedium.title(), headlineSmall = headlineSmall.title(),
        titleLarge = titleLarge.title(), titleMedium = titleMedium.title(), titleSmall = titleSmall.title(),
        bodyLarge = bodyLarge.body(), bodyMedium = bodyMedium.body(), bodySmall = bodySmall.body(),
        labelLarge = labelLarge.body(), labelMedium = labelMedium.body(), labelSmall = labelSmall.body(),
    )
}

private fun hex(value: String): Color = Color(android.graphics.Color.parseColor(value))

/**
 * TokiUI 0.2.0-b1 的 12 个语义色角色 → Material3 ColorScheme。
 * 色值只从 tokens JSON 来，不在 Kotlin 里另写一套色板。
 */
private fun tokensToScheme(tokens: SemanticTokens, dark: Boolean) =
    (if (dark) tokens.dark else tokens.light).let { palette ->
        val scheme = { role: String -> hex(palette.getValue(role)) }
        if (dark) {
            darkColorScheme(
                primary = scheme("accent"), onPrimary = scheme("onAccent"),
                primaryContainer = scheme("accentContainer"), onPrimaryContainer = scheme("onAccentContainer"),
                secondary = scheme("accent"), onSecondary = scheme("onAccent"),
                secondaryContainer = scheme("accentContainer"), onSecondaryContainer = scheme("onAccentContainer"),
                tertiary = scheme("accent"), onTertiary = scheme("onAccent"),
                background = scheme("canvas"), onBackground = scheme("textPrimary"),
                surface = scheme("surface"), onSurface = scheme("textPrimary"),
                surfaceVariant = scheme("surfaceElevated"), onSurfaceVariant = scheme("textSecondary"),
                surfaceContainer = scheme("surfaceElevated"), surfaceContainerHigh = scheme("surfaceElevated"),
                surfaceContainerHighest = scheme("surfaceElevated"), surfaceContainerLow = scheme("surface"),
                surfaceContainerLowest = scheme("canvas"),
                error = scheme("danger"), onError = scheme("onDanger"),
                outline = scheme("border"), outlineVariant = scheme("border"),
            )
        } else {
            lightColorScheme(
                primary = scheme("accent"), onPrimary = scheme("onAccent"),
                primaryContainer = scheme("accentContainer"), onPrimaryContainer = scheme("onAccentContainer"),
                secondary = scheme("accent"), onSecondary = scheme("onAccent"),
                secondaryContainer = scheme("accentContainer"), onSecondaryContainer = scheme("onAccentContainer"),
                tertiary = scheme("accent"), onTertiary = scheme("onAccent"),
                background = scheme("canvas"), onBackground = scheme("textPrimary"),
                surface = scheme("surface"), onSurface = scheme("textPrimary"),
                surfaceVariant = scheme("surfaceElevated"), onSurfaceVariant = scheme("textSecondary"),
                surfaceContainer = scheme("surfaceElevated"), surfaceContainerHigh = scheme("surfaceElevated"),
                surfaceContainerHighest = scheme("surfaceElevated"), surfaceContainerLow = scheme("surface"),
                surfaceContainerLowest = scheme("canvas"),
                error = scheme("danger"), onError = scheme("onDanger"),
                outline = scheme("border"), outlineVariant = scheme("border"),
            )
        }
    }

private fun loadTokens(context: Context): SemanticTokens? = runCatching {
    context.assets.open("tokiui.semantic.tokens.json").bufferedReader().use {
        SemanticTokens.parse(it.readText())
    }
}.getOrNull()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TokiLearnTheme(mode: ThemeMode = ThemeMode.DARK, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val tokens = remember { loadTokens(context) }
    val colorScheme = remember(tokens, dark) {
        tokens?.let { tokensToScheme(it, dark) }
            ?: if (dark) darkColorScheme() else lightColorScheme()
    }
    // Touch the capability object before any Haze node is created.
    TokiBlurCapability.canSampleInWindow
    // MaterialExpressiveTheme does not provide LocalContentColor: bare Text()
    // would default to black and vanish on dark backgrounds. Provide the
    // theme's onBackground here, as TokiStudio 0.1.10-aligned apps do.
    CompositionLocalProvider(
        LocalDarkTheme provides dark,
        LocalContentColor provides colorScheme.onBackground,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            shapes = Shapes(),
            motionScheme = MotionScheme.expressive(),
            typography = Typography().withTokiType(TokiFontFamily),
            content = content,
        )
    }
}
