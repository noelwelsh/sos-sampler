package example

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.features.unitArrows

import org.scalajs.dom
import org.scalajs.dom.*
import scala.scalajs.js.typedarray.{ArrayBuffer, Float32Array}

import org.soundsofscala.instrument.{SamplePlayer, Sampler, Synth}
import org.soundsofscala.models.*
import org.soundsofscala.playback.*
import org.soundsofscala.syntax.all.*

@main
def SosSampler(): Unit =
  renderOnDomContentLoaded(dom.document.querySelector("#app"), appElement())

def loadSampleView(samplePath: String): Unit =
  val audioCtx = new AudioContext()
  val request = new dom.XMLHttpRequest()
  request.open("GET", samplePath, true)
  request.responseType = "arraybuffer"
  request.onload = (e: Event) =>
    val audioData = request.response.asInstanceOf[ArrayBuffer]
    audioCtx.decodeAudioData(audioData, (buffer: AudioBuffer) =>
      val canvas = dom.document.getElementById("canvas").asInstanceOf[dom.html.Canvas]
      val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
      val data = buffer.getChannelData(0)
      drawWaveform(ctx, data)
    )
  request.send()

  def drawWaveform(ctx: CanvasRenderingContext2D, data: Float32Array): Unit =
    val canvas = ctx.canvas
    ctx.fillStyle = "#E2FF00"
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    val step = Math.ceil(data.length.toDouble / canvas.width).toInt
    val amp = canvas.height / 2
    for (i <- 0 until canvas.width.toInt)
      val min = data.slice(i * step.toInt, i * step.toInt + step.toInt).min
      val max = data.slice(i * step.toInt, i * step.toInt + step.toInt).max
      ctx.fillRect(i, (1 + min) * amp, 1, Math.max(1, (max - min) * amp))
end loadSampleView

def createKeyboard(sampler: Sampler, settings: Option[SamplePlayer.Settings], octaves: List[Octave]) =
  given AudioContext = new AudioContext()

  def whiteKeys(octave: Octave) = Seq(
    C(octave), D(octave), E(octave), F(octave), G(octave), A(octave), B(octave)
  )

  def blackKeys(octave: Octave) = Seq(
    C(octave).sharp, D(octave).sharp, F(octave).sharp, G(octave).sharp, A(octave).sharp
  )

  def createKey(key: MusicalEvent, isBlack: Boolean, id: String) =
    val keyClass = if (isBlack) "black-key" else "white-key"
    div(cls := keyClass, idAttr := id, onClick --> playSingleSampleNote(sampler, key, settings).unsafeRunAndForget())

  def createOctave(octave: Octave) =
    val whiteKeyElements = whiteKeys(octave).zipWithIndex.map:
      case (key, index) =>
        createKey(key, isBlack = false, id = s"white-key-${octave.value}-$index")

    val blackKeyElements = blackKeys(octave).zipWithIndex.flatMap:
      case (key, 2) =>
        Seq(div(cls := "spacer"), createKey(key, isBlack = true, id = s"black-key-${octave.value}-2"))
      case (key, index) =>
        Seq(createKey(key, isBlack = true, id = s"black-key-${octave.value}-$index"))

    div(cls := "octave",
      whiteKeyElements,
      div(cls := "black-keys", blackKeyElements)
    )
  end createOctave

  val keyboard = div(cls := "keyboard",
    octaves.map(createOctave),
    createKey(C(Octave(5)), isBlack = false, id = "white-key-5-0"),
  )
  keyboard
end createKeyboard

def playSingleSampleNote(sampler: Sampler, key: MusicalEvent, settings: Option[SamplePlayer.Settings]): AudioContext ?=> IO[Unit] =
  val song = Song(
    title = Title("Single Note"),
    tempo = Tempo(110),
    swing = Swing(0),
    mixer = Mixer(
      Track(
        Title(s"${sampler} Note"),
        key,
        sampler,
        settings
      )
    )
  )
  song.play()


def appElement(): HtmlElement =
  given AudioContext = new AudioContext()

  val selectedSampler = Var[Option[Sampler]](None)


  def loadSampler(instrument: String): IO[Sampler] =
    instrument.toLowerCase match
      case "piano" => Sampler.piano
      case "guitar" => Sampler.guitar
      case "rhubarb" => Sampler.rhubarb
      case _ => IO.raiseError(new IllegalArgumentException(s"Unknown instrument: $instrument"))


  val instruments = List("Piano", "Guitar", "Rhubarb")

  def instrumentClickHandler(instrument: String): Unit =
    loadSampler(instrument).map:
      sampler =>
        selectedSampler.set(Some(sampler))
        instrument.toLowerCase match
          case "piano" => loadSampleView("resources/audio/piano/C3.wav")
          case "guitar" => loadSampleView("resources/audio/guitar/C3.wav")
          case "rhubarb" => loadSampleView("resources/audio/misc/rhubarbSample.wav")
          case _ => println("Unknown instrument selected")
    .unsafeRunAndForget()

  val instrumentElements = instruments.map:
    instrument =>
      p(s" - $instrument", onClick --> (_ => instrumentClickHandler(instrument)))


  val volumeSlider = Var(0.5)
  val fadeInSlider = Var(0.0)
  val fadeOutSlider = Var(0.0)
  val playbackRateSlider = Var(1.0)
  val startTimeSlider = Var(0.0)
  val offsetSlider = Var(0.0)
  val lengthSlider = Var(5.0)
  val loopCheckbox = Var(false)
  val reverseCheckbox = Var(false)

  def createSliderElement(name: String, slider: Var[Double], min: Double, max: Double, default: Double, step: Double) =
    val sliderBackgroundSignal = slider.signal.map:
      value =>
        val percentage = ((value - min) / (max - min)) * 100
        s"linear-gradient(to top, #E2FF00 0%, #E2FF00 $percentage%, #2E2E2E $percentage%)"

    val sliderBoxShadowSignal = slider.signal.map:
      value =>
        val spread = ((value - min) / (max - min)) * 1.5
        val blur = ((value - min) / (max - min)) * 10
        s"0px 0px ${blur}px ${spread}px #E2FF00"

    val sliderElement = div(cls := "slider-container",
      div(cls := "slider",
        input(
          cls := "slider-track slider-thumb",
          defaultValue := default.toString,
          minAttr := min.toString,
          maxAttr := max.toString,
          stepAttr := step.toString,
          typ := "range",
          value <-- slider.signal.map(_.toString),
          background <-- sliderBackgroundSignal,
          boxShadow <-- sliderBoxShadowSignal,
          inContext: thisNode =>
            onInput.mapTo(thisNode.ref.valueAsNumber) --> slider
        )
      ),
      label(name, cls := "slider-label")
    )
    sliderElement
  end createSliderElement

  def createCheckboxElement(name: String, checkbox: Var[Boolean]) =
    div(cls := "checkbox-container",
      input(typ := "checkbox", cls := "custom-checkbox", checked <-- checkbox.signal, onInput.mapToChecked --> checkbox),
      label(name, cls := "checkbox-label")
    )

  val customSettings: Signal[SamplePlayer.Settings] =
    for
      loop <- loopCheckbox.signal
      reverse <- reverseCheckbox.signal
      volume <- volumeSlider.signal
      fadeIn <- fadeInSlider.signal
      fadeOut <- fadeOutSlider.signal
      playbackRate <- playbackRateSlider.signal
      startTime <- startTimeSlider.signal
      offset <- offsetSlider.signal
      duration <- lengthSlider.signal
    yield SamplePlayer.Settings(
      volume = volume,
      fadeIn = fadeIn,
      fadeOut = fadeOut,
      playbackRate = playbackRate,
      reversed = reverse,
      loop = if (loop) Some(Loop(start = 1, end = 2)) else None,
      startTime = startTime, offset = offset, duration = Some(duration)
    )

  val octaves = List(Octave(1), Octave(2), Octave(3), Octave(4))

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
    "k" -> "white-key-3-0",
    "o" -> "black-key-3-0",
    "l" -> "white-key-3-1",
    "p" -> "black-key-3-1",
    "ö" -> "white-key-3-2",
    "ä" -> "white-key-3-3",
  )

  dom.document.addEventListener("keydown", (e: KeyboardEvent) =>
    val key = e.key
    keyToNote.get(key).foreach:
      keyId =>
        val keyElement = dom.document.getElementById(keyId)
        if (keyElement != null) then
          keyElement.asInstanceOf[dom.html.Element].click()
          keyElement.asInstanceOf[dom.html.Element].style.transition = "transform 0.2s"
          keyElement.asInstanceOf[dom.html.Element].style.transform = "perspective(330px) rotateX(-2deg)"
          keyElement.asInstanceOf[dom.html.Element].style.transformOrigin = "top"
  )

  dom.document.addEventListener("keyup", (e: KeyboardEvent) =>
    val key = e.key
    keyToNote.get(key).foreach:
      keyId =>
        val keyElement = dom.document.getElementById(keyId)
        if (keyElement != null) then
          keyElement.asInstanceOf[dom.html.Element].style.transform = ""
  )

  div(cls := "sampler-container",
    div(cls := "sampler-section",
      div(cls := "waveform-loader",
        div(cls := "text-container",
          p("Instruments:"),
          instrumentElements,
        ),
        canvasTag(idAttr := "canvas")),
    ),
    div(cls := "sampler-section",
      div(cls := "sliders",
        createSliderElement("VOL", volumeSlider, 0.0, 1.0, 0.5, 0.1),
        createSliderElement("FADE IN", fadeInSlider, 0.0, 3.0, 0.0, 0.1),
        createSliderElement("FADE OUT", fadeOutSlider, 0.0, 3.0, 0.0, 0.1),
        createSliderElement("PLAYBACK RATE", playbackRateSlider, 0.1, 2.0, 1.0, 0.1),
        createSliderElement("START DELAY", startTimeSlider, 0.0, 3.0, 0.0, 0.1),
        createSliderElement("OFFSET", offsetSlider, 1.0, 5.0, 0.0, 0.1),
        createSliderElement("LENGTH", lengthSlider, 0.5, 10, 5.0, 1.0),
      ),
      div(cls := "playback-controls",
        createCheckboxElement("LOOP", loopCheckbox),
        createCheckboxElement("REVERSE", reverseCheckbox)),

      div(cls := "playback-controls"),
      // TO DO: Add play and stop buttons
    ),
    div(cls := "keyboard-container sampler-section",
      child <-- selectedSampler.signal.flatMap:
        case Some(sampler) =>
          customSettings.map(settings => createKeyboard(sampler, Some(settings), octaves))
        case None =>
          Val(div(cls := "select-text", "Please select an instrument to start."))
    )
  )
