package example

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.api.features.unitArrows
import com.raquo.laminar.receivers.ChildReceiver.text
import example.styles.GlobalStyles
import example.styles.GlobalStyles.applyStyle
import org.scalajs.dom
import org.scalajs.dom.*
import org.soundsofscala.instrument.{SamplePlayer, Sampler, Synth}
import org.soundsofscala.models.*
import org.soundsofscala.playback.*
import org.soundsofscala.songexamples.ExampleSong2
import org.soundsofscala.songexamples.ExampleSong2.musicalEvent
import org.soundsofscala.syntax.all.*
import org.soundsofscala.synthesis.Oscillator.SineOscillator
import scalacss.ProdDefaults.*
import scalacss.StyleA
import scalacss.internal.CanIUse.canvas
import scalacss.internal.mutable.GlobalRegistry

import scala.scalajs.js.typedarray.{ArrayBuffer, Float32Array}




@main
def helloWorld(): Unit =
  GlobalRegistry.addToDocumentOnRegistration()
  GlobalRegistry.register(GlobalStyles)
  renderOnDomContentLoaded(dom.document.querySelector("#app"), appElement())



def attemptWaveView(): Unit =
  val audioCtx = new AudioContext()
  val request = new dom.XMLHttpRequest()
  request.open("GET", "resources/audio/misc/rhubarbSample.wav", true)
  request.responseType = "arraybuffer"
  request.onload =  (e:Event) =>
    val audioData = request.response.asInstanceOf[ArrayBuffer]
    audioCtx.decodeAudioData(audioData,  (buffer: AudioBuffer) =>
      val canvas = dom.document.getElementById("canvas").asInstanceOf[dom.html.Canvas]
      val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
      val data = buffer.getChannelData(0)
      drawWaveform(ctx, data)
    )
  request.send()

  def drawWaveform(ctx: CanvasRenderingContext2D, data: Float32Array): Unit =
    val canvas = ctx.canvas
    ctx.fillStyle = "rgb(200, 200, 200)"
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    val step = Math.ceil(data.length.toDouble / canvas.width).toInt
    val amp = canvas.height / 2
    for (i <- 0 until canvas.width.toInt)
      val min = data.slice(i * step.toInt, i * step.toInt + step.toInt).min
      val max = data.slice(i * step.toInt, i * step.toInt + step.toInt).max
      ctx.fillRect(i, (1 + min) * amp, 1, Math.max(1, (max - min) * amp))


def createKeyboard(settings: Option[SamplePlayer.Settings]) =
  given AudioContext = new AudioContext()

  val minOctave = -2
  val maxOctave = 10

  def whiteKeys(octave: Octave) = Seq(
    C(octave), D(octave), E(octave), F(octave), G(octave), A(octave), B(octave)
  )
  def blackKeys(octave: Octave) = Seq(
    C(octave).sharp, D(octave).sharp, F(octave).sharp, G(octave).sharp, A(octave).sharp
  )

  def createKey(key: MusicalEvent, isBlack: Boolean, id: String) =
    val keyClass = if (isBlack) "black-key" else "white-key"
    val keyStyle = if (isBlack) GlobalStyles.black else GlobalStyles.white
    div(cls := keyClass, keyStyle, idAttr := id, onClick --> playSingleSampleNote(key, settings).unsafeRunAndForget())

  def createOctave(octave: Octave) =
    val whiteKeysForOctave = whiteKeys(octave)
    val blackKeysForOctave = blackKeys(octave)
    div(cls := "octave",
      GlobalStyles.octave,
      //whiteKeysForOctave.map(key => createKey(key, isBlack = false)),
      whiteKeysForOctave.zipWithIndex.map { case (key, index) => createKey(key, isBlack = false, id = s"white-key-${octave.value}-$index") },
      div(cls := "black-keys",
        GlobalStyles.blackKeys,
        createKey(blackKeysForOctave.head, isBlack = true, id = s"black-key-${octave.value}-0"),
        createKey(blackKeysForOctave(1), isBlack = true, id = s"black-key-${octave.value}-1"),
        div(cls := "spacer", GlobalStyles.spacer),
        createKey(blackKeysForOctave(2), isBlack = true, id = s"black-key-${octave.value}-2"),
        createKey(blackKeysForOctave(3), isBlack = true, id = s"black-key-${octave.value}-3"),
        createKey(blackKeysForOctave(4), isBlack = true, id = s"black-key-${octave.value}-4")
      )
    )

  val keyboard = div(cls := "keyboard",
    GlobalStyles.keyboard,
    createOctave(Octave(1)),
    createOctave(Octave(2)),
    createOctave(Octave(3)),
    createOctave(Octave(4)),
  )
  keyboard


def firstMusicProgram(): AudioContext ?=> IO[Unit] = ExampleSong2.play()

def continuousPlayback(settings: Option[SamplePlayer.Settings]): AudioContext ?=> IO[Unit] =
  println("Playing continuous notes")
  for {
    piano <- Sampler.rhubarb
    song = Song(
      title = Title("Rhubarb Loop"),
      tempo = Tempo(110),
      swing = Swing(0),
      mixer = Mixer(
        Track(
          Title("rhubarb D3"),
          D(Octave(3)).loop,
          piano,
          settings
         )
      )
    )
    _ <- song.play()
  } yield ()


def playSingleSampleNote(key: MusicalEvent, settings: Option[SamplePlayer.Settings]): AudioContext ?=> IO[Unit] =

  for {
    piano <- Sampler.piano
    song = Song(
      title = Title("Rhubarb Loop"),
      tempo = Tempo(110),
      swing = Swing(0),
      mixer = Mixer(
        Track(
          Title("rhubarb D3"),
          key,
          piano,
          settings

         )
      )
    )
    _ <- song.play()
  } yield ()



def appElement(): HtmlElement =
  given AudioContext = new AudioContext()

  val loopCheckbox = Var(false)
  val reverseCheckbox = Var(false)

  val volumeSlider = Var(0.5)
  val fadeInSlider = Var(0.0)
  val fadeOutSlider = Var(0.0)
  val playbackRateSlider = Var(1.0)
  val startTimeSlider = Var(0.0)
  val offsetSlider = Var(0.0)
  val durationSlider = Var(0.0)


  val keyToNote = Map(
    "a" -> "white-key-2-0",
    "w" -> "black-key-2-0",
    "s" -> "white-key-2-1",
    "e" -> "black-key-2-1",
    "d" -> "white-key-2-2",
    "f" -> "white-key-2-3",
    "t" -> "black-key-2-2",
    "g" -> "white-key-2-4",
    "z" -> "black-key-2-3",
    "h" -> "white-key-2-5",
    "u" -> "black-key-2-4",
    "j" -> "white-key-2-6",
  )

  dom.document.addEventListener("keydown", (e: KeyboardEvent) => {

    val key = e.key
    keyToNote.get(key).foreach { keyId =>
      val keyElement = dom.document.getElementById(keyId)
      if (keyElement != null) {
        keyElement.asInstanceOf[dom.html.Element].click()
        keyElement.asInstanceOf[dom.html.Element].style.transition = "transform 0.2s"
        keyElement.asInstanceOf[dom.html.Element].style.transform = "perspective(330px) rotateX(-2deg)"
        keyElement.asInstanceOf[dom.html.Element].style.transformOrigin = "top"

      }
    }
  })

  dom.document.addEventListener("keyup", (e: KeyboardEvent) => {
    val key = e.key
    keyToNote.get(key).foreach { keyId =>
      val keyElement = dom.document.getElementById(keyId)
      if (keyElement != null) {
        keyElement.asInstanceOf[dom.html.Element].style.transform = ""
      }
    }
  })

  val customSettings: Signal[SamplePlayer.Settings] =
    for {
      loop <- loopCheckbox.signal
      reverse <- reverseCheckbox.signal
      volume <- volumeSlider.signal
      fadeIn <- fadeInSlider.signal
      fadeOut <- fadeOutSlider.signal
      playbackRate <- playbackRateSlider.signal
      startTime <- startTimeSlider.signal
      offset <- offsetSlider.signal
      duration <- durationSlider.signal

    } yield SamplePlayer.Settings(
      volume = volume,
      fadeIn = fadeIn,
      fadeOut = fadeOut,
      playbackRate = playbackRate,
      reversed = reverse,
      loop = if (loop) Some(Loop(start = 1, end = 2)) else None,
      startTime = startTime, offset = offset, duration = Some(duration)
    )

  div(
    GlobalStyles.header,

    h1("SOS Sampler"),

    div(
      GlobalStyles.controls,
      input(typ := "range", minAttr := "0", maxAttr := "1.0", stepAttr := "0.1", defaultValue := "0.8", GlobalStyles.slider, value <-- volumeSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> volumeSlider),
      label("Volume", GlobalStyles.labels),
      input(typ := "range", minAttr := "0", maxAttr := "3", stepAttr := "0.1", defaultValue := "0", GlobalStyles.slider, value <-- fadeInSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> fadeInSlider),
      label("Fade In", GlobalStyles.labels),
      input(typ := "range",minAttr := "0", maxAttr := "3", stepAttr := "0.1", defaultValue := "0", GlobalStyles.slider, value <-- fadeOutSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> fadeOutSlider),
      label("Fade Out", GlobalStyles.labels),
      input(typ := "range", minAttr := "0", maxAttr := "3", stepAttr := "0.1", defaultValue := "0", GlobalStyles.slider, value <-- playbackRateSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> playbackRateSlider),
      label("Playback Rate", GlobalStyles.labels),
      input(typ := "range", minAttr := "0", maxAttr := "3", stepAttr := "0.1", defaultValue := "0", GlobalStyles.slider, value <-- startTimeSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> startTimeSlider),
      label("Start Time", GlobalStyles.labels),
      input(typ := "range", minAttr := "0", maxAttr := "3", stepAttr := "0.1", defaultValue := "0", GlobalStyles.slider, value <-- offsetSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> offsetSlider),
      label("Offset", GlobalStyles.labels),
      input(typ := "range", minAttr := "0", maxAttr := "8", stepAttr := "0.1", defaultValue := "5", GlobalStyles.slider, value <-- durationSlider.signal.map(_.toString), onInput.mapToValue.map(_.toDouble) --> durationSlider),
      label("Duration", GlobalStyles.labels),
      input(typ := "checkbox", GlobalStyles.checkbox, checked <-- loopCheckbox.signal, onInput.mapToChecked --> loopCheckbox),
      label("Loop", GlobalStyles.labels),
      input(typ := "checkbox", GlobalStyles.checkbox, checked <-- reverseCheckbox.signal, onInput.mapToChecked --> reverseCheckbox),
      label("Reverse", GlobalStyles.labels),
      button("Waveform", onClick --> attemptWaveView()),
      canvasTag(idAttr := "canvas", GlobalStyles.canvas)),



    div(
      cls := "keyboard-container",
      GlobalStyles.keyboardContainer,
      child <-- customSettings.map(settings => createKeyboard(Some(settings)))
    )
  )

