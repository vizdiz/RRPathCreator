import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumConstraints

object TrajectoryGen {
    // Remember to set these constraints to the same values as your DriveConstants.java file in the quickstart
    private val driveConstraints = DriveConstraints(60.0, 60.0, 0.0, 270.0.toRadians, 270.0.toRadians, 0.0)

    // Remember to set your track width to an estimate of your actual bot to get accurate trajectory profile duration!
    private const val trackWidth = 16.0

    private val combinedConstraints = MecanumConstraints(driveConstraints, trackWidth)

    //private val startPose = Pose2d(-48.0, -48.0, 90.0.toRadians)

    fun createTrajectory(startPose: Pose2d, points: List<Pair<Vector2d, Double>>): ArrayList<Trajectory> {
        val list = ArrayList<Trajectory>()

        var currentPose = startPose

        points.forEach {
            if (it != points[0])
                list.add(
                    TrajectoryBuilder(currentPose, currentPose.heading, combinedConstraints)
                        .splineTo(it.first, it.second).build()
                )

            currentPose = Pose2d(it.first.x, it.first.y, it.second)
        }

        // Small Example Routine

        //println(points)

        return list
    }

    fun drawOffbounds() {
        GraphicsUtil.fillRect(Vector2d(0.0, -63.0), 18.0, 18.0) // robot against the wall
    }
}

val Double.toRadians get() = (Math.toRadians(this))
