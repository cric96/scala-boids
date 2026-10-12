package it.unibo.render

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.geometry.Point3D
import javafx.scene.image.WritableImage
import javafx.scene.paint.{Color, PhongMaterial}
import javafx.scene.shape.{MeshView, TriangleMesh}
import javafx.scene.transform.{Rotate, Translate}
import javafx.scene.{AmbientLight, DirectionalLight, Group, PerspectiveCamera, Scene, SceneAntialiasing}

import java.util.concurrent.atomic.AtomicReference

/** The density map as a lit relief in a JavaFX scene, which the mouse orbits (drag) and zooms (wheel). */
class ReliefView extends JFXPanel:
  import ReliefView.*

  private val mesh = TriangleMesh()
  private val azimuth = Rotate(0, Rotate.Y_AXIS)
  private val elevation = Rotate(-Elevation, Rotate.X_AXIS)
  private val distance = Translate()
  // the latest relief not shown yet: the simulation may outrun the JavaFX thread, which shows only the newest
  private val pending = AtomicReference[Option[Relief]](None)
  Platform.setImplicitExit(false)
  Platform.runLater(() => setScene(scene()))

  def update(relief: Relief): Unit =
    if (pending.getAndSet(Some(relief)).isEmpty) Platform.runLater(() => pending.getAndSet(None).foreach(show))

  /** The map laid on the floor, its centre at the origin, its top side away from the viewer and its hills rising. */
  private def show(relief: Relief): Unit =
    val points = relief.points.toArray
    val (xs, ys) = (points.indices by Relief.CoordinatesPerPoint).map(i => (points(i), points(i + 1))).unzip
    val (centreX, centreY) = ((xs.min + xs.max) / 2, (ys.min + ys.max) / 2)
    val floor = (points.indices by Relief.CoordinatesPerPoint).flatMap { i =>
      Seq(points(i) - centreX, -points(i + 2), centreY - points(i + 1))
    }
    val faces = relief.faces.toArray
    mesh.getPoints.setAll(floor.toArray*)
    mesh.getTexCoords.setAll(relief.densities.toArray.flatMap(density => Array(alongTheStrip(density), 1f / 2))*)
    // laid on the floor the map is mirrored, so each triangle turns the other way to keep facing up
    val upwards = faces.grouped(Relief.CornersPerFace).flatMap(_.reverse)
    mesh.getFaces.setAll(upwards.flatMap(vertex => Array(vertex, vertex)).toArray*)
    if (distance.getZ == 0) distance.setZ(-Distance * (xs.max - xs.min))

  private def scene(): Scene =
    val view = MeshView(mesh)
    view.setMaterial(PhongMaterial(Color.WHITE, magmaStrip, null, null, null))
    val camera = PerspectiveCamera(true)
    camera.setFarClip(FarClip)
    camera.getTransforms.addAll(azimuth, elevation, distance)
    val light = DirectionalLight(Color.gray(1 - Ambient))
    light.setDirection(LightDirection)
    val scene = Scene(Group(view, AmbientLight(Color.gray(Ambient)), light), 0, 0, true, SceneAntialiasing.BALANCED)
    scene.setFill(fxColor(Palette.Sky))
    scene.setCamera(camera)
    orbitWithTheMouse(scene)
    scene

  private def orbitWithTheMouse(scene: Scene): Unit =
    val last = AtomicReference((0.0, 0.0))
    scene.setOnMousePressed(event => last.set((event.getSceneX, event.getSceneY)))
    scene.setOnMouseDragged { event =>
      val (x, y) = last.getAndSet((event.getSceneX, event.getSceneY))
      azimuth.setAngle(azimuth.getAngle + (event.getSceneX - x) * DegreesPerPixel)
      elevation.setAngle((elevation.getAngle - (event.getSceneY - y) * DegreesPerPixel).max(-Overhead).min(0))
    }
    scene.setOnScroll(event => distance.setZ(distance.getZ * math.pow(ZoomPerPixel, -event.getDeltaY)))

object ReliefView:
  /** How far above the floor the camera looks down at the map at first, in degrees. */
  private val Elevation = 45.0

  /** Straight above the map, the highest the camera goes. */
  private val Overhead = 90.0

  /** Away from the centre of the map at first, in widths of the map: the whole map in sight. */
  private val Distance = 1.4

  /** Far enough not to cut the map at any zoom. */
  private val FarClip = 100_000.0

  private val DegreesPerPixel = 0.3

  /** Each pixel the wheel scrolls brings the camera this much closer. */
  private val ZoomPerPixel = 1.002

  /** The share of the light that reaches every face, the rest comes from [[LightDirection]]. */
  private val Ambient = 0.45

  /** Where the light travels to: down and away from the viewer's left shoulder, lighting the slopes that face them. */
  private val LightDirection = Point3D(0.4, 1.0, 0.6)

  /** The palette as a texture one pixel high: each vertex picks its colour by its density along it. */
  private lazy val magmaStrip: WritableImage =
    val strip = WritableImage(Palette.ChannelMax + 1, 1)
    (0 to Palette.ChannelMax).foreach(x =>
      strip.getPixelWriter.setColor(x, 0, fxColor(Palette.magma(x.toDouble / Palette.ChannelMax)))
    )
    strip

  /** The centre of the pixel of the strip for the density: the edges blend the two ends, as the texture wraps. */
  private def alongTheStrip(density: Float): Float = (density * Palette.ChannelMax + 1f / 2) / (Palette.ChannelMax + 1)

  private def fxColor(rgb: Int): Color =
    val (red, green, blue) = Palette.channels(rgb)
    Color.rgb(red, green, blue)
