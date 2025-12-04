package fr.axllvy.tarotmeter.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF009688)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFF9DE6DF)
private val OnPrimaryContainer = Color(0xFF00332E)
private val Secondary = Color(0xFF80CBC4)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFCBE6E3)
private val OnSecondaryContainer = Color(0xFF203331)
private val Tertiary = Color(0xFFFDD835)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFE6DBAC)
private val OnTertiaryContainer = Color(0xFF332B0B)
private val Error = Color(0xFFBC332C)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFE6B1AE)
private val OnErrorContainer = Color(0xFF330E0C)
private val Background = Color(0xFFfbfcfc)
private val OnBackground = Color(0xFF303333)
private val Surface = Color(0xFFfbfcfc)
private val OnSurface = Color(0xFF303333)
private val SurfaceVariant = Color(0xFFd7e6e4)
private val OnSurfaceVariant = Color(0xFF526664)
private val Outline = Color(0xFF7a9996)

private val PrimaryDark = Color(0xFF7FE6DC)
private val OnPrimaryDark = Color(0xFF004C46)
private val PrimaryContainerDark = Color(0xFF00665D)
private val OnPrimaryContainerDark = Color(0xFF9DE6DF)
private val SecondaryDark = Color(0xFFC0E6E2)
private val OnSecondaryDark = Color(0xFF304C4A)
private val SecondaryContainerDark = Color(0xFF406663)
private val OnSecondaryContainerDark = Color(0xFFCBE6E3)
private val TertiaryDark = Color(0xFFE6D694)
private val OnTertiaryDark = Color(0xFF4C4110)
private val TertiaryContainerDark = Color(0xFF665715)
private val OnTertiaryContainerDark = Color(0xFFE6DBAC)
private val ErrorDark = Color(0xFFE69B96)
private val OnErrorDark = Color(0xFF4C1512)
private val ErrorContainerDark = Color(0xFF661C17)
private val OnErrorContainerDark = Color(0xFFE6B1AE)
private val BackgroundDark = Color(0xFF303333)
private val OnBackgroundDark = Color(0xFFe2e6e5)
private val SurfaceDark = Color(0xFF303333)
private val OnSurfaceDark = Color(0xFFe2e6e5)
private val SurfaceVariantDark = Color(0xFF526664)
private val OnSurfaceVariantDark = Color(0xFFd1e6e4)
private val OutlineDark = Color(0xFF9cb3b0)

val lightColorScheme =
  lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
  )

val darkColorScheme =
  darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
  )
