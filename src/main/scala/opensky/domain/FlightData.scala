package opensky.domain

import scala.language.postfixOps

case class FlightData(time: Long, states: Flights) {
  def groupByCountry: FlightsByCountry = states groupBy (_.originCountry) mapValues (_ map (_.icao24) toSet)
}
