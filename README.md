# RRPathCreator

A JavaFX-based trajectory visualization tool for FTC (First Tech Challenge) robotics using Road Runner path planning. This application allows you to create, visualize, and simulate robot trajectories on a field with interactive waypoint editing.

## Features

- **Interactive Path Creation**: Click on the field to add waypoints with orientation arrows
- **Real-time Trajectory Generation**: Uses Road Runner library to generate smooth spline trajectories
- **Visual Simulation**: Watch your robot follow the generated path with real-time animation
- **Drag & Drop Editing**: Move waypoints and adjust orientations by dragging
- **Field Visualization**: Displays the FTC field with proper scaling and coordinate system
- **Trajectory Constraints**: Configurable drive constraints for realistic path planning

## Requirements

- Java 8 or higher
- JavaFX 14
- Gradle build system

## Dependencies

- **Road Runner Core**: `com.acmerobotics.roadrunner:core:0.5.1`
- **JavaFX**: Graphics and controls modules
- **Kotlin**: JVM version 1.3.50

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd RRPathCreator
```

2. Build the project:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew run
```

## Usage

### Creating Paths

1. **Add Waypoints**: Click anywhere on the field to add waypoints
2. **Adjust Orientation**: Drag the arrow attached to each waypoint to set the robot's heading
3. **Move Waypoints**: Drag the circular waypoint markers to reposition them
4. **View Simulation**: The robot will automatically follow the generated trajectory

### Configuration

The trajectory constraints can be modified in `TrajectoryGen.kt`:

```kotlin
private val driveConstraints = DriveConstraints(
    60.0,    // max velocity (in/s)
    60.0,    // max acceleration (in/s²)
    0.0,     // max angular velocity (rad/s)
    270.0.toRadians,  // max angular acceleration (rad/s²)
    270.0.toRadians,  // max angular velocity (rad/s)
    0.0      // start velocity (in/s)
)
```

### Field Dimensions

- **Field Width**: 144 inches (12 feet)
- **Coordinate System**: Origin at field center, Y-axis pointing up
- **Robot Size**: 18-inch square representation

## Project Structure

```
src/main/kotlin/
├── App.kt              # Main application class with JavaFX UI
├── TrajectoryGen.kt    # Road Runner trajectory generation
├── GraphicsUtil.kt     # Drawing utilities and coordinate conversion
├── arrow.kt           # Arrow widget for orientation display
└── Clock.kt           # Time utilities for animation

src/main/resources/
└── field.png          # FTC field background image
```

## Key Components

### App.kt
- Main JavaFX application
- Handles user interactions (clicking, dragging)
- Manages trajectory simulation and animation
- Coordinate system conversion between pixels and field coordinates

### TrajectoryGen.kt
- Creates Road Runner trajectories from waypoints
- Configures drive constraints and mecanum constraints
- Generates spline paths between waypoints

### GraphicsUtil.kt
- Drawing utilities for paths, robot representation, and field elements
- Coordinate conversion between field coordinates and screen pixels
- Robot visualization with heading vectors

## Coordinate System

The application uses a coordinate system where:
- Origin (0,0) is at the field center
- Y-axis points upward (positive Y is toward the audience)
- X-axis points rightward (positive X is toward the driver station)
- All measurements are in inches
- Robot heading is measured in radians

## Customization

### Adding New Field Elements
Modify `TrajectoryGen.drawOffbounds()` to add field-specific obstacles or boundaries.

### Changing Robot Constraints
Update the `driveConstraints` and `trackWidth` values in `TrajectoryGen.kt` to match your robot's capabilities.

### Modifying Field Image
Replace `field.png` with your custom field image. The application will automatically scale to the image dimensions.

## Troubleshooting

### Build Issues
- Ensure Java 8+ is installed
- Verify JavaFX is properly configured
- Check that all dependencies are resolved

### Runtime Issues
- Make sure the field image (`field.png`) exists in the resources folder
- Verify JavaFX modules are available at runtime

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is open source. Please check the license file for specific terms.

## Acknowledgments

Thank you to FTC Team 7236, Recharged Green, for creating this open-source path visualization software for the FTC community!

- Built with [Road Runner](https://github.com/acmerobotics/road-runner) trajectory library
- Uses JavaFX for the user interface
- Designed for FTC robotics teams
