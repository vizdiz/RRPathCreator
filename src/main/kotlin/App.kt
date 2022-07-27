import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Duration
import kotlin.math.*

class App : Application() {

    private val robotRect = Rectangle(100.0, 100.0, 10.0, 10.0)
    private val startRect = Rectangle(100.0, 100.0, 10.0, 10.0)
    private val endRect = Rectangle(100.0, 100.0, 10.0, 10.0)

    private var startTime = Double.NaN
    private val points = ArrayList<Pair<Vector2d, Double>>()
    private var pointMap = HashMap<Int, Pair<Circle, Arrow>>()

    private val arrowDistance = 20.0
    private val halfWidth = 72.0

    private var startPose = Vector2d(Math.random() * halfWidth, Math.random() * halfWidth)
    private var secondPose = Vector2d(Math.random() * halfWidth, Math.random() * halfWidth)


    private val root = Group()

    private lateinit var fieldImage: Image
    private lateinit var stage: Stage
    private lateinit var trajectories: ArrayList<Trajectory>
    private lateinit var trajectoryDurations: List<Double>

    private var activeTrajectoryIndex = 0
    private var duration = 0.0
    private var numberOfTrajectories = 0

    companion object {
        var WIDTH = 0.0
        var HEIGHT = 0.0
    }

    override fun start(stage: Stage?) {
        this.stage = stage!!
        fieldImage = Image("/field.png")

        WIDTH = fieldImage.width
        HEIGHT = fieldImage.height
        GraphicsUtil.pixelsPerInch = WIDTH / GraphicsUtil.FIELD_WIDTH
        GraphicsUtil.halfFieldPixels = WIDTH / 2.0

        val canvas = Canvas(WIDTH, HEIGHT)
        val gc = canvas.graphicsContext2D
        val t1 = Timeline(KeyFrame(Duration.millis(10.0), EventHandler<ActionEvent> { run(gc) }))
        t1.cycleCount = Timeline.INDEFINITE

        stage.scene = Scene(
            StackPane(
                root
            )
        )

        root.children.addAll(canvas, startRect, endRect, robotRect)

        addPoint(coordinateToPixel(startPose.x, startPose.y).first, coordinateToPixel(startPose.x, startPose.y).second, true, 0)
        addPoint(coordinateToPixel(secondPose.x, secondPose.y).first, coordinateToPixel(secondPose.x, secondPose.y).second, false, 1)

        createTrajectory()

        canvas.setOnMouseClicked {
            addPoint(it.sceneX, it.sceneY, false, points.size)

            createTrajectory()
        }

        trajectoryDurations = trajectories.map { it.duration() }
        duration = trajectoryDurations.sum()
        numberOfTrajectories = trajectories.size

        stage.title = "PathVisualizer"
        stage.isResizable = false

        //println("duration $duration")

        stage.show()
        t1.play()
    }

    private fun run(gc: GraphicsContext) {

        updatePoints()
        createTrajectory()

        trajectoryDurations = trajectories.map { it.duration() }
        duration = trajectoryDurations.sum()
        numberOfTrajectories = trajectories.size

        if (startTime.isNaN())
            startTime = Clock.seconds

        GraphicsUtil.gc = gc
        gc.drawImage(fieldImage, 0.0, 0.0)

        gc.lineWidth = GraphicsUtil.LINE_THICKNESS

        gc.globalAlpha = 0.5
        GraphicsUtil.setColor(Color.RED)
        TrajectoryGen.drawOffbounds()
        gc.globalAlpha = 1.0

        val trajectory = trajectories[activeTrajectoryIndex]

        var x = 0.0
        for (i in 0 until activeTrajectoryIndex)
            x += trajectoryDurations[i]
        val prevDurations: Double = x

        val time = Clock.seconds
        val profileTime = time - startTime - prevDurations
        val duration = trajectoryDurations[activeTrajectoryIndex]

        val start = trajectories.first().start()
        val end = trajectories.last().end()
        val current = trajectory[profileTime]

        if (profileTime >= duration) {
            activeTrajectoryIndex++
            if(activeTrajectoryIndex >= numberOfTrajectories) {
                activeTrajectoryIndex = 0
                startTime = time
            }
        }

        trajectories.forEach{GraphicsUtil.drawSampledPath(it.path)}

        GraphicsUtil.updateRobotRect(startRect, start, GraphicsUtil.END_BOX_COLOR, 0.5)
        GraphicsUtil.updateRobotRect(endRect, end, GraphicsUtil.END_BOX_COLOR, 0.5)

        GraphicsUtil.updateRobotRect(robotRect, current, GraphicsUtil.ROBOT_COLOR, 0.75)
        GraphicsUtil.drawRobotVector(current)

        stage.title = "Profile duration : ${"%.2f".format(duration)} - time in profile ${"%.2f".format(profileTime)}"
    }

    private fun addPoint(sceneX: Double, sceneY: Double, replacePoint: Boolean, index: Int){

        val x = - (sceneY - HEIGHT / 2) / (HEIGHT / 2) * halfWidth
        val y = - (sceneX - WIDTH / 2) / (WIDTH / 2) * halfWidth

        val point = Circle(sceneX, sceneY, WIDTH / 64.0)

        /*
        point.setOnMouseReleased{
            root.children.remove(point)
        }
         */

        val arrowAngle = Math.random() * 2 * PI
        val arrow = Arrow(sceneX - arrowDistance * sqrt(2.0) * cos(arrowAngle), sceneY - arrowDistance * sqrt(2.0) * sin(arrowAngle), sceneX + arrowDistance * sqrt(2.0) * cos(arrowAngle), sceneY + arrowDistance * sqrt(2.0) * sin(arrowAngle), arrowAngle)

        if (arrowAngle > PI ) {
            arrow.angle = - arrowAngle - PI / 2
        }
        else {
            arrow.angle = 1.5 * PI - arrowAngle
        }

        if (!replacePoint)
            points.add(Pair(Vector2d(x, y), arrow.angle))
        else
            points.add(index, Pair(Vector2d(x, y), arrow.angle))
            pointMap[index] = Pair(point, arrow)


        //print(points.size)
        enableDrag(point, arrow, index)
        enableRotations(arrow, index)

        root.children.add(point)
        root.children.add(arrow)
    }

    private fun pixelToCoordinate(sceneX: Double, sceneY: Double): Pair<Double, Double> {
        val x = - (sceneX - WIDTH / 2) / (WIDTH / 2) * halfWidth
        val y = - (sceneY - HEIGHT / 2) / (HEIGHT / 2) * halfWidth

        return Pair(x, y)
    }

    private fun coordinateToPixel(x: Double, y: Double): Pair<Double, Double> {

        val sceneX = - (x / halfWidth) * (WIDTH / 2) + WIDTH / 2
        val sceneY = - (y / halfWidth) * (HEIGHT / 2) + HEIGHT / 2

        return Pair(sceneX, sceneY)
    }

    private fun createTrajectory() {
        trajectories = TrajectoryGen.createTrajectory(
            Pose2d(points[0].first.x, points[0].first.y, points[0].second), points)
        trajectoryDurations = trajectories.map { it.duration() }
        duration = trajectoryDurations.sum()
    }

    private fun updatePoints(){
        for (i in 0 until pointMap.size) {
            if (pointMap[i] != null) {
                val point = Vector2d(
                    pointMap[i]?.first?.let { pixelToCoordinate(it.centerY, it.centerX).first }!!,
                    pointMap[i]?.first?.let { pixelToCoordinate(it.centerY, it.centerX).second }!!
                )
                points[i] = Pair(point, pointMap[i]?.second?.angle) as Pair<Vector2d, Double>
            }
        }
    }

    internal class Delta {
        var x = 0.0
        var y = 0.0
    }

    private fun enableDrag(circle: Circle, arrow: Arrow, index: Int) {
        val dragDelta = Delta()
        var currentArrow = arrow

        var startX = 0.0
        var startY = 0.0
        var endX = 0.0
        var endY = 0.0
        var angle = 0.0

        circle.onMousePressed = EventHandler { mouseEvent -> // record a delta distance for the drag and drop operation.
            dragDelta.x = circle.centerX - mouseEvent.x
            dragDelta.y = circle.centerY - mouseEvent.y

            startX = mouseEvent.x - arrowDistance * sqrt(2.0) * cos(angle)
            startY = mouseEvent.y - arrowDistance * sqrt(2.0) * sin(angle)
            endX = mouseEvent.x + arrowDistance * sqrt(2.0) * cos(angle)
            endY = mouseEvent.y + arrowDistance * sqrt(2.0) * sin(angle)
            angle = arrow.angle

            circle.scene.cursor = Cursor.MOVE
            root.children.remove(arrow)
        }
        circle.onMouseReleased =
            EventHandler {
                root.children.remove(arrow)
                currentArrow = Arrow(startX, startY, endX, endY, angle)
                root.children.add(currentArrow)
                enableRotations(currentArrow, index)

                if (angle > PI ) {
                    arrow.angle = - angle - PI / 2
                }
                else {
                    arrow.angle = 1.5 * PI - angle
                }

                enableDrag(circle, currentArrow, index)

                pointMap[index] = Pair(circle, currentArrow)
                points[index] = Pair(Vector2d(pixelToCoordinate(circle.centerX, circle.centerY).second, pixelToCoordinate(circle.centerX, circle.centerY).first), currentArrow.angle)
                circle.scene.cursor = Cursor.HAND
            }
        circle.onMouseDragged = EventHandler { mouseEvent ->

            //root.children.remove(arrow)

            //arrow.isVisible = false

            //root.children.remove(arrow)
            //root.children.remove(currentArrow)

            //root.children.remove(arrow)

            circle.centerX = mouseEvent.x + dragDelta.x
            circle.centerY = mouseEvent.y + dragDelta.y

            startX = mouseEvent.x + dragDelta.x - arrowDistance * sqrt(2.0) * cos(angle)
            startY = mouseEvent.y + dragDelta.y - arrowDistance * sqrt(2.0) * sin(angle)
            endX = mouseEvent.x + dragDelta.x + arrowDistance * sqrt(2.0) * cos(angle)
            endY = mouseEvent.y + dragDelta.y + arrowDistance * sqrt(2.0) * sin(angle)

            points[index] = Pair(Vector2d(pixelToCoordinate(circle.centerX, circle.centerY).second, pixelToCoordinate(circle.centerX, circle.centerY).first), currentArrow.angle)
        }
        circle.onMouseEntered = EventHandler { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown) {
                circle.scene.cursor = Cursor.HAND
            }
        }
        circle.onMouseExited = EventHandler { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown) {

                circle.scene.cursor = Cursor.DEFAULT
            }
        }
    }

    private fun enableRotations(arrow: Arrow, index: Int) {
        val dragDelta = Delta()
        var currentArrow = arrow
        currentArrow.onMousePressed = EventHandler{
            mouseEvent ->

            dragDelta.x = mouseEvent.x - (currentArrow.startX + currentArrow.endX) / 2
            dragDelta.y = mouseEvent.y - (currentArrow.startY + currentArrow.endY) / 2

            currentArrow.scene.cursor = Cursor.MOVE
        }
        currentArrow.onMouseDragged = EventHandler {
            currentArrow.scene.cursor = Cursor.HAND
        }
        currentArrow.onMouseReleased =
            EventHandler {
                dragDelta.x = it.x - (currentArrow.startX + currentArrow.endX) / 2
                dragDelta.y = it.y - (currentArrow.startY + currentArrow.endY) / 2
                val angle = atan2(dragDelta.y, dragDelta.x)

                val deltaX = arrowDistance * sqrt(2.0) * cos(angle)
                val deltaY = arrowDistance * sqrt(2.0) * sin(angle)

                val startX = (currentArrow.startX + currentArrow.endX) / 2 - deltaX
                val startY = (currentArrow.startY + currentArrow.endY) / 2 - deltaY
                val endX = (currentArrow.startX + currentArrow.endX) / 2 + deltaX
                val endY = (currentArrow.startY + currentArrow.endY) / 2  + deltaY

                root.children.remove(currentArrow)
                currentArrow = Arrow(startX, startY, endX, endY, angle)
                root.children.add(currentArrow)
                enableRotations(currentArrow, index)

                if (angle > PI ) {
                    arrow.angle = - angle - PI / 2
                }
                else {
                    arrow.angle = 1.5 * PI - angle
                }

                if (sign(angle) < 0.0 ) {
                    currentArrow.angle = - angle - PI / 2
                }
                else {
                    currentArrow.angle = 1.5 * PI - angle
                }

                pointMap[index] = Pair(pointMap[index]?.first, currentArrow) as Pair<Circle, Arrow>

                pointMap[index]?.first?.let { it -> enableDrag(it, currentArrow, index) }

                currentArrow.scene.cursor = Cursor.DEFAULT
            }


        currentArrow.onMouseEntered = EventHandler { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown) {
                currentArrow.scene.cursor = Cursor.HAND
            }
        }

        currentArrow.onMouseExited = EventHandler { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown) {
                currentArrow.scene.cursor = Cursor.DEFAULT
            }
        }


    }
}

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}