import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path

/**
 *
 * @author kn
 */
class Arrow @JvmOverloads constructor(
    var startX: Double,
    var startY: Double,
    var endX: Double,
    var endY: Double,
    var angle: Double,
    arrowHeadSize: Double = defaultArrowHeadSize
) :
    Path() {
    companion object {
        private const val defaultArrowHeadSize = 5.0
    }

    init {
        strokeProperty().bind(fillProperty())
        fill = Color.BLACK

        //Line
        elements.add(MoveTo(startX, startY))
        elements.add(LineTo(endX, endY))

        //ArrowHead
        val angle = Math.atan2(endY - startY, endX - startX) - Math.PI / 2.0
        val sin = Math.sin(angle)
        val cos = Math.cos(angle)
        //point1
        val x1 = (-1.0 / 2.0 * cos + Math.sqrt(3.0) / 2 * sin) * arrowHeadSize + endX
        val y1 = (-1.0 / 2.0 * sin - Math.sqrt(3.0) / 2 * cos) * arrowHeadSize + endY
        //point2
        val x2 = (1.0 / 2.0 * cos + Math.sqrt(3.0) / 2 * sin) * arrowHeadSize + endX
        val y2 = (1.0 / 2.0 * sin - Math.sqrt(3.0) / 2 * cos) * arrowHeadSize + endY
        elements.add(LineTo(x1, y1))
        elements.add(LineTo(x2, y2))
        elements.add(LineTo(endX, endY))
    }
}