package opensky.aggregator

import akka.NotUsed
import akka.stream.scaladsl.Flow
import opensky.domain._

import scala.concurrent.duration.FiniteDuration

object FlightsByOriginCountry {
  import opensky.util.StreamUtils._

  def apply(windowTime: FiniteDuration, eager: Boolean = true): Flow[FlightData, Map[String, Int], NotUsed] = {
    val groupByCountry = window[FlightData, FlightsByCountry](windowTime, eager)(_.groupByCountry)((flightsByCountry, newData) =>
      flightsByCountry updatedWith newData)
    val countFlights = Flow[FlightsByCountry].map(_ mapValues (_.size))

    groupByCountry.via(countFlights)
  }
}
