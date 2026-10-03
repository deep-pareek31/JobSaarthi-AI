package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sober, prestigious brand colors inspired by classic Indian consumer wordmarks (e.g. shaadi.com)
val BrandDeepNavy = Color(0xFF0B192C)
val BrandMutedNavy = Color(0xFF1E293B)
val BrandCyanAccent = Color(0xFF38BDF8)
val BrandGoldDot = Color(0xFFFBBF24)
val BrandLightBg = Color(0xFF0F172A)

/**
 * Premium typographic logo where the app name 'JobSaarthi' is the logo itself,
 * crafted in the sober, trustworthy design language of shaadi.com.
 */
@Composable
fun JobSaarthiBrandLogo(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.LARGE,
    showContainer: Boolean = true,
    showTagline: Boolean = true
) {
    val textFontSize = when (size) {
        LogoSize.COMPACT -> 18.sp
        LogoSize.MEDIUM -> 24.sp
        LogoSize.LARGE -> 32.sp
    }

    val subtitleSize = when (size) {
        LogoSize.COMPACT -> 8.sp
        LogoSize.MEDIUM -> 10.sp
        LogoSize.LARGE -> 11.sp
    }

    val containerPadding = when (size) {
        LogoSize.COMPACT -> 8.dp
        LogoSize.MEDIUM -> 12.dp
        LogoSize.LARGE -> 16.dp
    }

    val cornerRadius = when (size) {
        LogoSize.COMPACT -> 12.dp
        LogoSize.MEDIUM -> 16.dp
        LogoSize.LARGE -> 20.dp
    }

    val content = @Composable {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Wordmark: 'job' in pure white, 'saarthi' in cyan with dot accent
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("job")
                        }
                        withStyle(
                            SpanStyle(
                                color = BrandCyanAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("saarthi")
                        }
                    },
                    fontSize = textFontSize,
                    fontFamily = FontFamily.SansSerif
                )

                // The signature accent dot on the wordmark
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(if (size == LogoSize.COMPACT) 6.dp else 8.dp)
                        .background(BrandGoldDot, shape = CircleShape)
                )
            }

            if (showTagline) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(BrandCyanAccent.copy(alpha = 0.7f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CAREER NAVIGATOR",
                        color = Color(0xFF94A3B8),
                        fontSize = subtitleSize,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(BrandCyanAccent.copy(alpha = 0.7f), CircleShape)
                    )
                }
            }
        }
    }

    if (showContainer) {
        Box(
            modifier = modifier
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = BrandDeepNavy, spotColor = BrandDeepNavy)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BrandDeepNavy, BrandMutedNavy)
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(horizontal = containerPadding * 1.5f, vertical = containerPadding),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    } else {
        content()
    }
}

enum class LogoSize {
    COMPACT,
    MEDIUM,
    LARGE
}
