package opensky.aggregator

import akka.kafka.scaladsl.Consumer.Control
import akka.stream.scaladsl.Source
import opensky.domain._
import play.api.libs.json.JsSuccess

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Main extends Setup with Protocol {

  def openSkyDataSource: Source[Map[String, Int], Control] =
    KafkaSource(source)
      .log("aggregator", msg => s"${msg.serializedValueSize()} bytes pulled out from $source")
      .map(msg => msg.value.parseJson[FlightData])
      .collect { case JsSuccess(data, _) => data }
      .async
      .via(FlightsByOriginCountry(windowTime))

  def main(args: Array[String]): Unit = {
    val done = openSkyDataSource.runForeach(logStats)

    done.onComplete {
      case Success(_)     => logger.info("Done"); System.exit(0)
      case Failure(error) => logger.error(error, "Shutting down"); System.exit(1)
    }
    Await.result(done, Duration.Inf)
  }

  /**
    * Can't do it properly because things like `prefixAndTail` are currently buggy with kafka-reactive
    */
  private var firstStats = true
  private def logInitialStats(flightsPerCountry: Map[String, Int]) = {
    logger.info("Initial stats: ")
    flightsPerCountry foreach {
      case (country, n) => logger.info(s"$country, ${printNice("flight", n)} in the air")
    }
  }
  private def logStats(flightsPerCountry: Map[String, Int]) = {
    if (firstStats) {
      logInitialStats(flightsPerCountry)
      firstStats = false
    } else
      flightsPerCountry foreach {
        case (country, n) =>
          logger.info(s"$country, ${printNice("flight", n)} in the last ${printNice("minute", windowTime.toMinutes)}")
      }
  }
  private def printNice(word: String, arity: Long) = s"$arity ${if (arity == 1) word else word + 's'}"
}
