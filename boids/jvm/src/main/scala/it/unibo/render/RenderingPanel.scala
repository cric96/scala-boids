package it.unibo.render

import com.formdev.flatlaf.{FlatDarkLaf, FlatLaf}
import com.formdev.flatlaf.util.UIScale
import it.unibo.boundary.Parameter

import java.awt.{
  BasicStroke,
  BorderLayout,
  Color,
  Component,
  Dimension,
  FlowLayout,
  Graphics,
  Graphics2D,
  LayoutManager,
  RenderingHints
}
import java.awt.image.BufferedImage
import javax.swing.{BorderFactory, BoxLayout, Icon, JButton, JCheckBox, JComponent, JLabel, JPanel, OverlayLayout}
import scala.jdk.CollectionConverters.*

/** Draws the simulation image with the floating control panel (the same one of the web version) on top. */
class RenderingPanel(control: Boolean, var backgroundImage: Option[BufferedImage]) extends JPanel:
  private val sliders: Map[Parameter, Slider] = Parameter.values.map(parameter => parameter -> Slider(parameter)).toMap
  private val showBoids = JCheckBox("Show boids")
  private val threeD = JCheckBox("3D")
  val relief: ReliefView = ReliefView()
  private val reset = JButton("Reset")
  private val sections = if (control) Parameter.sections.map(section) else Nil
  private val body = column(sections :+ footer*)
  private val collapse = JButton(Chevron(() => body.isVisible))
  private val card = Card(BorderLayout())
  private val overlay = JPanel(FlowLayout(FlowLayout.LEFT, 16, 16))
  collapse.putClientProperty("JButton.buttonType", "borderless")
  collapse.addActionListener(_ => body.setVisible(!body.isVisible))
  reset.addActionListener(_ => sliders.values.foreach(_.reset()))
  card.add(header, BorderLayout.NORTH)
  card.add(body, BorderLayout.CENTER)
  overlay.setOpaque(false)
  overlay.add(card)
  threeD.addActionListener(_ => relief.setVisible(threeD.isSelected))
  relief.setVisible(false)
  // the control panel floats above the relief, both from the top left corner
  setLayout(OverlayLayout(this))
  Seq(overlay, relief).foreach { layer =>
    layer.setAlignmentX(Component.LEFT_ALIGNMENT)
    layer.setAlignmentY(Component.TOP_ALIGNMENT)
    add(layer)
  }

  def parametersOf(param: Parameter): Double = sliders(param).value
  def boidsEnabled: Boolean = showBoids.isSelected
  def reliefEnabled: Boolean = threeD.isSelected

  /** The control panel overlaps the relief, which must be painted first. */
  override def isOptimizedDrawingEnabled: Boolean = false
  override def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    backgroundImage.foreach(backgroundImage => g.drawImage(backgroundImage, 0, 0, this))

  private def header: JComponent =
    val title = JLabel("Boids")
    title.putClientProperty("FlatLaf.styleClass", "h3")
    transparent(JPanel(BorderLayout()), title -> BorderLayout.WEST, collapse -> BorderLayout.EAST)

  private def section(name: String, parameters: List[Parameter]): JComponent =
    val title = JLabel(name.toUpperCase)
    title.setForeground(Theme.muted)
    title.putClientProperty("FlatLaf.style", "font: bold -2")
    title.setBorder(BorderFactory.createEmptyBorder(14, 0, 6, 0))
    column(title +: parameters.map(sliders(_).render)*)

  private def footer: JComponent =
    val footer =
      transparent(JPanel(BorderLayout()), column(showBoids, threeD) -> BorderLayout.WEST, reset -> BorderLayout.EAST)
    footer.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(14, 0, 0, 0),
        BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border),
          BorderFactory.createEmptyBorder(12, 0, 4, 0)
        )
      )
    )
    footer

private def column(components: JComponent*): JPanel =
  val panel = JPanel()
  panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
  components.foreach(_.setAlignmentX(Component.LEFT_ALIGNMENT))
  transparent(panel, components.map(_ -> null)*)

private def transparent(panel: JPanel, children: (JComponent, AnyRef)*): JPanel =
  children.foreach(panel.add(_, _))
  Seq(panel).concat(children.map(_._1)).foreach(_.setOpaque(false))
  panel

/** Rounded translucent background of the control panel. */
private class Card(layout: LayoutManager) extends JPanel(layout):
  setOpaque(false)
  setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16))
  override def getPreferredSize: Dimension = Dimension(UIScale.scale(260), super.getPreferredSize.height)
  override def paintComponent(g: Graphics): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(Theme.panel)
    g2.fillRoundRect(0, 0, getWidth, getHeight, 28, 28)
    g2.setColor(Theme.border)
    g2.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 28, 28)
    g2.dispose()

/** Chevron pointing up while the panel is expanded, down while it is collapsed. */
private class Chevron(expanded: () => Boolean) extends Icon:
  override def getIconWidth: Int = 16
  override def getIconHeight: Int = 16
  override def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit =
    val g2 = g.create().asInstanceOf[Graphics2D]
    val (tip, base) = if (expanded()) (y + 6, y + 10) else (y + 10, y + 6)
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(Theme.muted)
    g2.setStroke(BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g2.drawPolyline(Array(x + 4, x + 8, x + 12), Array(base, tip, base), 3)
    g2.dispose()

/** Design tokens shared with `boids/js/style.css`. */
object Theme:
  val accent: Color = Color(0x4fd1c5)
  val muted: Color = Color(0x9aa3b2)
  val panel: Color = Color(15, 17, 26, 191)
  val border: Color = Color(255, 255, 255, 20)

  def install(): Unit =
    FlatLaf.setGlobalExtraDefaults(
      Map(
        "@accentColor" -> "#4fd1c5",
        "@foreground" -> "#e6e8ef",
        "Button.background" -> "#ffffff14",
        "Slider.trackWidth" -> "4",
        "Slider.thumbSize" -> "16,16",
        "Slider.trackColor" -> "#ffffff29",
        "Slider.trackValueColor" -> "@accentColor",
        "Slider.thumbColor" -> "#ffffff",
        "Slider.hoverThumbColor" -> "#ffffff",
        "Slider.pressedThumbColor" -> "#ffffff",
        "Slider.thumbBorderColor" -> "@accentColor",
        "Slider.focusedThumbBorderColor" -> "@accentColor",
        "Slider.thumbBorderWidth" -> "3"
      ).asJava
    )
    FlatDarkLaf.setup()
