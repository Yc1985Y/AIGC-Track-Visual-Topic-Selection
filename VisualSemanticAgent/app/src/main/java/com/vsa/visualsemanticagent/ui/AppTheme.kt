package com.vsa.visualsemanticagent.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsa.visualsemanticagent.R

val Literata = FontFamily(
    Font(R.font.literata_regular, FontWeight.Normal),
    Font(R.font.literata_medium, FontWeight.Medium),
    Font(R.font.literata_semibold, FontWeight.SemiBold),
)

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

object AppColors {
    val Primary = Color(0xFF003528)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF0B4D3D)
    val OnPrimaryContainer = Color(0xFFE7FFF5)
    val InversePrimary = Color(0xFF97D3BD)
    val PrimaryFixed = Color(0xFFB2EFD9)
    val PrimaryFixedDim = Color(0xFF97D3BD)
    val OnPrimaryFixed = Color(0xFF002118)
    val OnPrimaryFixedVariant = Color(0xFF105040)

    val Secondary = Color(0xFF9E3F42)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFE8989)
    val OnSecondaryContainer = Color(0xFF762125)
    val SecondaryFixed = Color(0xFFFFDAD8)
    val SecondaryFixedDim = Color(0xFFFFB3B1)
    val OnSecondaryFixed = Color(0xFF410007)
    val OnSecondaryFixedVariant = Color(0xFF7F282C)

    val Tertiary = Color(0xFF8A6500)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFB67A00)
    val OnTertiaryContainer = Color(0xFFFFF6E0)
    val TertiaryFixed = Color(0xFFFFDEA1)
    val TertiaryFixedDim = Color(0xFFFFD47E)
    val OnTertiaryFixed = Color(0xFF370E08)
    val OnTertiaryFixedVariant = Color(0xFF6C3830)

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val Background = Color(0xFFFFF8F2)
    val OnBackground = Color(0xFF261900)
    val Surface = Color(0xFFFFF8F2)
    val SurfaceDim = Color(0xFFFFF0DE)
    val SurfaceBright = Color(0xFFFFFCF8)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFFFF2DF)
    val SurfaceContainer = Color(0xFFFFEBCB)
    val SurfaceContainerHigh = Color(0xFFFFE5B6)
    val SurfaceContainerHighest = Color(0xFFFFDEA1)
    val OnSurface = Color(0xFF261900)
    val OnSurfaceVariant = Color(0xFF404945)
    val InverseSurface = Color(0xFF402D00)
    val InverseOnSurface = Color(0xFFFFEFD5)

    val Outline = Color(0xFF707975)
    val OutlineVariant = Color(0xFFBFC9C3)
    val SurfaceTint = Color(0xFF2E6857)

    val CardBorder = Color(0xFFF0E4D6)
    val GlassSurface = Color(0xCCFFFCF8)
    val GlassBorder = Color(0xFFF0E4D6)
    val InputBackground = Color(0xFFFFF7F0)
    val MintAccent = Color(0xFFCCF2E6)
    val CoralSoft = Color(0xFFFFE0DC)
    val GoldSoft = Color(0xFFFFEDC0)
}

object AppTypography {
    val DisplayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 42.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.02f * 42).sp
    )

    val HeadlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )

    val HeadlineLargeMobile = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )

    val HeadlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )

    val BodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 28.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val LabelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.14.sp
    )

    val LabelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.48.sp
    )

    val MonoMetric = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        lineHeight = 24.sp
    )

    val LabelLarge = LabelMedium
}

object AppShapes {
    val ExtraSmall: Dp = 8.dp
    val Small: Dp = 12.dp
    val Medium: Dp = 16.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val Full: Dp = 999.dp
}

object AppSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 40.dp
    val containerPadding: Dp = 24.dp
    val gutter: Dp = 16.dp
}
