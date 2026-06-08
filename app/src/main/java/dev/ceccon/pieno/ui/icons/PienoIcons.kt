package dev.ceccon.pieno.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

// Set di icone custom, lineari, coerenti: griglia 24, tratto 1.75, estremi e
// giunzioni arrotondati. Il colore lo applica Icon(tint=...) al punto d'uso.
// Vedi design-principles.md sezione 6.

private fun strokeIcon(name: String, path: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathData(path),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.75f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()

private fun filledIcon(name: String, path: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(pathData = PathData(path), fill = SolidColor(Color.Black))
    }.build()

object PienoIcons {

    val Card: ImageVector = strokeIcon("Card") {
        moveTo(3.5f, 6f); lineTo(20.5f, 6f); lineTo(20.5f, 18f); lineTo(3.5f, 18f); close()
        moveTo(3.5f, 9.5f); lineTo(20.5f, 9.5f)
    }

    val Pin: ImageVector = strokeIcon("Pin") {
        moveTo(12f, 21f)
        curveTo(8.5f, 16f, 6f, 12.8f, 6f, 9.8f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 18f, 9.8f)
        curveTo(18f, 12.8f, 15.5f, 16f, 12f, 21f)
        close()
        moveTo(12f, 7.7f)
        arcToRelative(2.2f, 2.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 4.4f)
        arcToRelative(2.2f, 2.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -4.4f)
        close()
    }

    val Receipt: ImageVector = strokeIcon("Receipt") {
        moveTo(6.5f, 3.5f); lineTo(17.5f, 3.5f); lineTo(17.5f, 20.5f)
        lineTo(15f, 18.8f); lineTo(12f, 20.5f); lineTo(9f, 18.8f); lineTo(6.5f, 20.5f); close()
        moveTo(9.5f, 8f); lineTo(14.5f, 8f)
        moveTo(9.5f, 11.5f); lineTo(14.5f, 11.5f)
    }

    val Person: ImageVector = strokeIcon("Person") {
        moveTo(12f, 4.8f)
        arcToRelative(3.2f, 3.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 6.4f)
        arcToRelative(3.2f, 3.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -6.4f)
        close()
        moveTo(5.5f, 19.5f)
        curveTo(5.5f, 15.9f, 8.4f, 14f, 12f, 14f)
        curveTo(15.6f, 14f, 18.5f, 15.9f, 18.5f, 19.5f)
    }

    val Droplet: ImageVector = strokeIcon("Droplet") {
        moveTo(12f, 3.5f)
        curveTo(9f, 8f, 6f, 11.5f, 6f, 15f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = false, 18f, 15f)
        curveTo(18f, 11.5f, 15f, 8f, 12f, 3.5f)
        close()
    }

    val Refresh: ImageVector = strokeIcon("Refresh") {
        moveTo(18.4f, 8.6f)
        arcTo(7f, 7f, 0f, isMoreThanHalf = true, isPositiveArc = false, 19.4f, 13.6f)
        moveTo(18.4f, 8.6f); lineTo(15.1f, 8.2f)
        moveTo(18.4f, 8.6f); lineTo(18.9f, 5.2f)
    }

    val Wallet: ImageVector = strokeIcon("Wallet") {
        moveTo(4f, 8f); lineTo(17f, 8f)
        curveTo(18.7f, 8f, 20f, 9.3f, 20f, 11f); lineTo(20f, 16f)
        curveTo(20f, 17.7f, 18.7f, 19f, 17f, 19f); lineTo(7f, 19f)
        curveTo(5.3f, 19f, 4f, 17.7f, 4f, 16f); lineTo(4f, 8f)
        close()
        moveTo(4f, 8f)
        curveTo(4f, 6.6f, 5.1f, 5.5f, 6.5f, 5.5f); lineTo(15f, 5.5f)
        moveTo(15.5f, 12f); lineTo(20f, 12f); lineTo(20f, 15f); lineTo(15.5f, 15f)
        curveTo(14.7f, 15f, 14f, 14.3f, 14f, 13.5f)
        curveTo(14f, 12.7f, 14.7f, 12f, 15.5f, 12f)
        close()
    }

    val Share: ImageVector = strokeIcon("Share") {
        moveTo(6f, 10.4f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 3.2f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -3.2f)
        close()
        moveTo(17f, 4.4f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 3.2f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -3.2f)
        close()
        moveTo(17f, 16.4f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 3.2f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -3.2f)
        close()
        moveTo(7.5f, 11.2f); lineTo(15.5f, 6.8f)
        moveTo(7.5f, 12.8f); lineTo(15.5f, 17.2f)
    }

    val Scan: ImageVector = strokeIcon("Scan") {
        moveTo(4f, 8.5f); lineTo(4f, 6f); curveTo(4f, 4.9f, 4.9f, 4f, 6f, 4f); lineTo(8.5f, 4f)
        moveTo(15.5f, 4f); lineTo(18f, 4f); curveTo(19.1f, 4f, 20f, 4.9f, 20f, 6f); lineTo(20f, 8.5f)
        moveTo(20f, 15.5f); lineTo(20f, 18f); curveTo(20f, 19.1f, 19.1f, 20f, 18f, 20f); lineTo(15.5f, 20f)
        moveTo(8.5f, 20f); lineTo(6f, 20f); curveTo(4.9f, 20f, 4f, 19.1f, 4f, 18f); lineTo(4f, 15.5f)
        moveTo(4f, 12f); lineTo(20f, 12f)
    }

    val Bell: ImageVector = strokeIcon("Bell") {
        moveTo(12f, 4f)
        curveTo(8.7f, 4f, 7f, 6.2f, 7f, 9f)
        curveTo(7f, 14f, 5f, 16f, 5f, 16f); lineTo(19f, 16f)
        curveTo(19f, 16f, 17f, 14f, 17f, 9f)
        curveTo(17f, 6.2f, 15.3f, 4f, 12f, 4f)
        close()
        moveTo(10f, 19f)
        curveTo(10.4f, 20.2f, 11.1f, 20.8f, 12f, 20.8f)
        curveTo(12.9f, 20.8f, 13.6f, 20.2f, 14f, 19f)
    }

    val Info: ImageVector = strokeIcon("Info") {
        moveTo(12f, 4f)
        arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 16f)
        arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -16f)
        close()
        moveTo(12f, 11f); lineTo(12f, 16f)
        moveTo(12f, 7.8f); lineTo(12f, 8.1f)
    }

    val Logout: ImageVector = strokeIcon("Logout") {
        moveTo(13f, 4.5f); lineTo(6.5f, 4.5f)
        curveTo(5.7f, 4.5f, 5f, 5.2f, 5f, 6f); lineTo(5f, 18f)
        curveTo(5f, 18.8f, 5.7f, 19.5f, 6.5f, 19.5f); lineTo(13f, 19.5f)
        moveTo(10f, 12f); lineTo(20f, 12f)
        moveTo(16.5f, 8.5f); lineTo(20f, 12f); lineTo(16.5f, 15.5f)
    }

    val Close: ImageVector = strokeIcon("Close") {
        moveTo(6f, 6f); lineTo(18f, 18f)
        moveTo(18f, 6f); lineTo(6f, 18f)
    }

    val Check: ImageVector = strokeIcon("Check") {
        moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 6.5f)
    }

    val ChevronRight: ImageVector = strokeIcon("ChevronRight") {
        moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
    }

    val ChevronLeft: ImageVector = strokeIcon("ChevronLeft") {
        moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f)
    }

    val ChevronDown: ImageVector = strokeIcon("ChevronDown") {
        moveTo(5f, 9f); lineTo(12f, 16f); lineTo(19f, 9f)
    }

    val Search: ImageVector = strokeIcon("Search") {
        moveTo(10.5f, 4.5f)
        arcToRelative(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 12f)
        arcToRelative(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -12f)
        close()
        moveTo(15f, 15f); lineTo(20f, 20f)
    }

    val Sliders: ImageVector = strokeIcon("Sliders") {
        moveTo(4f, 7f); lineTo(20f, 7f)
        moveTo(4f, 12f); lineTo(20f, 12f)
        moveTo(4f, 17f); lineTo(20f, 17f)
        moveTo(8f, 5.4f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 3.2f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -3.2f)
        close()
        moveTo(15f, 10.4f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 3.2f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -3.2f)
        close()
        moveTo(9f, 15.4f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 3.2f)
        arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -3.2f)
        close()
    }

    val ArrowRight: ImageVector = strokeIcon("ArrowRight") {
        moveTo(5f, 12f); lineTo(19f, 12f)
        moveTo(13f, 6f); lineTo(19f, 12f); lineTo(13f, 18f)
    }

    val Plus: ImageVector = strokeIcon("Plus") {
        moveTo(12f, 5f); lineTo(12f, 19f)
        moveTo(5f, 12f); lineTo(19f, 12f)
    }

    val Pump: ImageVector = strokeIcon("Pump") {
        moveTo(6f, 20.5f); lineTo(6f, 5.5f)
        curveTo(6f, 4.7f, 6.7f, 4f, 7.5f, 4f); lineTo(12.5f, 4f)
        curveTo(13.3f, 4f, 14f, 4.7f, 14f, 5.5f); lineTo(14f, 20.5f)
        moveTo(4.5f, 20.5f); lineTo(15.5f, 20.5f)
        moveTo(8f, 8f); lineTo(12f, 8f)
        moveTo(14f, 10f); lineTo(16.5f, 10f)
        curveTo(17.3f, 10f, 18f, 10.7f, 18f, 11.5f); lineTo(18f, 15.5f)
        curveTo(18f, 16.3f, 18.7f, 17f, 19.5f, 17f)
        curveTo(20.3f, 17f, 21f, 16.3f, 21f, 15.5f); lineTo(21f, 9f); lineTo(18.5f, 6.5f)
    }

    val ListView: ImageVector = strokeIcon("ListView") {
        moveTo(8f, 7f); lineTo(20f, 7f)
        moveTo(8f, 12f); lineTo(20f, 12f)
        moveTo(8f, 17f); lineTo(20f, 17f)
        moveTo(4.2f, 7f); lineTo(4.4f, 7f)
        moveTo(4.2f, 12f); lineTo(4.4f, 12f)
        moveTo(4.2f, 17f); lineTo(4.4f, 17f)
    }

    val ChevronUp: ImageVector = strokeIcon("ChevronUp") {
        moveTo(5f, 15f); lineTo(12f, 8f); lineTo(19f, 15f)
    }

    val Edit: ImageVector = strokeIcon("Edit") {
        moveTo(4f, 20f); lineTo(4f, 16.5f); lineTo(14.5f, 6f); lineTo(18f, 9.5f); lineTo(7.5f, 20f); close()
        moveTo(12.5f, 8f); lineTo(16f, 11.5f)
    }

    val Navigation: ImageVector = strokeIcon("Navigation") {
        moveTo(12f, 3.5f); lineTo(19.5f, 20.5f); lineTo(12f, 16.5f); lineTo(4.5f, 20.5f); close()
    }

    val Eye: ImageVector = strokeIcon("Eye") {
        moveTo(2.5f, 12f)
        curveTo(4.6f, 7.8f, 8f, 5.7f, 12f, 5.7f)
        curveTo(16f, 5.7f, 19.4f, 7.8f, 21.5f, 12f)
        curveTo(19.4f, 16.2f, 16f, 18.3f, 12f, 18.3f)
        curveTo(8f, 18.3f, 4.6f, 16.2f, 2.5f, 12f)
        close()
        moveTo(12f, 9.2f)
        arcToRelative(2.8f, 2.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 5.6f)
        arcToRelative(2.8f, 2.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -5.6f)
        close()
    }

    val Star: ImageVector = strokeIcon("Star") {
        moveTo(12f, 3.5f)
        lineTo(14.3f, 8.6f); lineTo(19.8f, 9.1f); lineTo(15.6f, 12.8f); lineTo(16.9f, 18.2f)
        lineTo(12f, 15.3f); lineTo(7.1f, 18.2f); lineTo(8.4f, 12.8f); lineTo(4.2f, 9.1f); lineTo(9.7f, 8.6f)
        close()
    }

    val StarFilled: ImageVector = filledIcon("StarFilled") {
        moveTo(12f, 3.5f)
        lineTo(14.3f, 8.6f); lineTo(19.8f, 9.1f); lineTo(15.6f, 12.8f); lineTo(16.9f, 18.2f)
        lineTo(12f, 15.3f); lineTo(7.1f, 18.2f); lineTo(8.4f, 12.8f); lineTo(4.2f, 9.1f); lineTo(9.7f, 8.6f)
        close()
    }

    val MoreHorizontal: ImageVector = filledIcon("MoreHorizontal") {
        moveTo(3.4f, 12f)
        arcToRelative(1.8f, 1.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.6f, 0f)
        arcToRelative(1.8f, 1.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.6f, 0f)
        close()
        moveTo(10.2f, 12f)
        arcToRelative(1.8f, 1.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.6f, 0f)
        arcToRelative(1.8f, 1.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.6f, 0f)
        close()
        moveTo(17f, 12f)
        arcToRelative(1.8f, 1.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.6f, 0f)
        arcToRelative(1.8f, 1.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.6f, 0f)
        close()
    }
}
