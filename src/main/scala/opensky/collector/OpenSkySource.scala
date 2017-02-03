package opensky.collector

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Materializer, ThrottleMode}
import opensky.ThrottleControl

import scala.concurrent.ExecutionContext

private[collector] object OpenSkySource {
  import opensky.util.StreamUtils._

  def apply(endpoint: String, throttleControl: ThrottleControl, curl: Curl)(implicit ec: ExecutionContext,
                                                                            mat: Materializer,
                                                                            log: LoggingAdapter): Source[String, NotUsed] = {
    val getData = Flow[String].mapAsync(1)(curl.get)
    val in      = Source.repeat(endpoint)

    in.throttle(elements = throttleControl.elements,
                per = throttleControl.per,
                maximumBurst = throttleControl.maximumBurst,
                mode = ThrottleMode.Shaping)
      .log("OpenSkySource", _ => "Calling OpenSky")
      .via(restartOnError(getData))
      .log("OpenSkySource", _ => "Got data")
  }
}
