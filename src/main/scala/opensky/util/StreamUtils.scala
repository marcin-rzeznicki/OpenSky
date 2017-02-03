package opensky.util

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Attributes
import akka.stream.Supervision._
import akka.stream.impl.fusing.GraphStages.SimpleLinearGraphStage
import akka.stream.scaladsl._
import akka.stream.stage._

import scala.concurrent.duration.FiniteDuration

trait StreamUtils {
  private def resumingDecider(implicit log: LoggingAdapter): Decider = e => {
    log.error(e, "Resuming after error")
    Resume
  }
  private def restartingDecider(implicit log: LoggingAdapter): Decider = e => {
    log.error(e, "Restarting after error")
    Restart
  }
  def resumeOnError[In, Out, Mat](flow: Flow[In, Out, Mat])(implicit log: LoggingAdapter): Flow[In, Out, Mat] =
    flow.withAttributes(supervisionStrategy(resumingDecider))
  def restartOnError[In, Out, Mat](flow: Flow[In, Out, Mat])(implicit log: LoggingAdapter): Flow[In, Out, Mat] =
    flow.withAttributes(supervisionStrategy(restartingDecider))

  private final class Pulse[T](val interval: FiniteDuration, val initiallyOpen: Boolean = false) extends SimpleLinearGraphStage[T] {
    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new TimerGraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

        setHandlers(in, out, this)

        override def preStart(): Unit = if (!initiallyOpen) pulse()
        override def onPush(): Unit   = push(out, grab(in))
        override def onPull(): Unit = if (!pulsing) {
          pull(in)
          pulse()
        }

        override protected def onTimer(timerKey: Any): Unit = {
          log.debug("Pulse")
          if (!isClosed(in) && !hasBeenPulled(in)) pull(in) else log.warning("Window expired")
        }

        private def pulse() = {
          pulsing = true
          log.debug("Pulsing")
          schedulePeriodically("PulseTimer", interval)
        }
        private var pulsing = false
      }
    override def toString = "Pulse"
  }

  def window[A, S](of: FiniteDuration, eager: Boolean = true)(seed: A => S)(aggregate: (S, A) ⇒ S): Flow[A, S, NotUsed] =
    Flow[A].conflateWithSeed(seed)(aggregate).via(new Pulse(of, eager))

}

object StreamUtils extends StreamUtils
