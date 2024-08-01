package example.styles

import com.raquo.laminar.api.L.{HtmlElement, Mod, cls}
import example.styles.GlobalStyles.&
import scalacss.ProdDefaults.*
import scalacss.internal.mutable.GlobalRegistry

import scala.language.{implicitConversions, postfixOps}

object GlobalStyles extends StyleSheet.Inline {

  import dsl.*

  implicit def applyStyle(styleA: StyleA): Mod[HtmlElement] =
    cls(styleA.className.value)

  val bodyStyle: StyleA = style(
    unsafeRoot("body")(
      backgroundImage := "radial-gradient(circle at top left, #505050, #404040, #303030, #000000)",
      margin.`0`,
      padding.`0`,
      height(100.vh),
      width(100.vw),

    )
  )

  val header: StyleA = style("header")(
    color.white,
    fontFamily.attr := "fantasy",
    margin.auto,
    fontSize(62 px),
  )

  val canvas: StyleA = style("canvas")(
    border(1 px, solid),
    borderColor.orange,
    backgroundColor.black,
  )

  val keyboardContainer: StyleA = style("keyboard-container")(
    display.flex,
    justifyContent.center,
  )

  val keyboard: StyleA = style("keyboard")(
    display.flex,
    flexDirection.row,
    borderTop(20.px, solid, darkgrey),
    borderRight(20.px, solid, darkgrey),
    borderLeft(20.px, solid, darkgrey),
    borderBottom(20.px, solid, grey),
    backgroundColor.red
  )

  val octave: StyleA = style("octave")(
    display.flex,
    position.relative,
  )

  val white: StyleA = style("white")(
    width(40 px),
    height(250 px),
    backgroundColor.white,
    border(1 px, solid),
    borderColor.black,
    boxSizing.borderBox,
    textAlign.center,
    lineHeight(200 px),
    cursor.pointer,
    borderRadius(0 px, 0 px, 5 px, 5 px),
    boxShadow := "0px 0px 0px 62px rgba(0, 0, 0, 0.65) inset, -11px 4px 10px 0px rgba(0, 0, 0, 0.55) inset, 12px -19px 10px 0px rgba(0, 0, 0, 0.55) inset",
    &.active(
      transition := "transform 0.2s",
      transform := "perspective(330px) rotateX(-2deg)",
      transformOrigin := "top"

    ),
  )
  val black: StyleA = style("black")(

    width(30 px),
    height(170 px),
    backgroundColor.white,
    border(1px, solid),
    borderColor.black,
    boxSizing.borderBox,
    marginRight(10 px),
    textAlign.center,
    lineHeight(120 px),
    cursor.pointer,
    borderRadius(0 px, 0 px, 5 px, 5 px),
    boxShadow := "0px 0px 0px 62px rgba(0, 0, 0, 0.65) inset, -11px 4px 10px 0px rgba(0, 0, 0, 0.55) inset, 12px -19px 10px 0px rgba(0, 0, 0, 0.55) inset",
    &.active(
      transition := "transform 0.2s",
      transform := "perspective(1500px) rotateX(-6deg)",
      transformOrigin := "top"
    ),
  )

  val blackKeys: StyleA = style("black-keys")(
    display.flex,
    position.absolute,
    top(0 px),
    left(0 px),
    zIndex(1),
    marginLeft(25 px),
  )

  val spacer: StyleA = style("spacer")(
    width(40 px),
  )

  val controls: StyleA = style("controls")(
    display.flex,
    justifyContent.center,
  )

  val slider: StyleA = style("slider")(
    all.unset,
    width(50.px),
    height(10.px),
    backgroundColor(c"#d3d3d3"),
    outline.none,
    opacity(0.7),
    transition := "opacity .2s",
    &.hover(
      opacity(1)
    )
  )


  val checkbox: StyleA = style("checkbox")(
    all.unset,
    backgroundColor.darkred,
    color.white,
    border(2px, solid),
    borderColor.darkblue,
    padding(10 px, 20 px),
    margin(10 px),
    fontSize(16 px),
    borderRadius(5 px),
    cursor.pointer,
    transition := "background-color 0.3s ease, color 0.3s ease, transform 0.2s ease",
    boxShadow := "0 4px 6px rgba(0, 0, 0, 0.1)",
    &.checked(
      backgroundColor.darkblue,
      borderColor.darkred,
      color.white,
      transform := "translateY(-2px)"
    ),
  )

  val labels: StyleA = style("labels")(
    color.white,
    fontSize(10 px),
    margin(2 px),
  )
}
