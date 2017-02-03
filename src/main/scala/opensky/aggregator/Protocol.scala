package opensky.aggregator

import opensky.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait Protocol extends opensky.Protocol {
  implicit val flightStateJSONReads: Reads[FlightState] = (
    __(0).read[String] and
      __(1).readNullable[String] and
      __(2).read[String] and
      __(3).readNullable[Float] and
      __(4).readNullable[Float] and
      __(5).readNullable[Float] and
      __(6).readNullable[Float] and
      __(7).readNullable[Float] and
      __(8).read[Boolean] and
      __(9).readNullable[Float] and
      __(10).readNullable[Float] and
      __(11).readNullable[Float] and
      __(12).readNullable[Vector[Long]].map(_.getOrElse(Vector.empty))
  )(FlightState.apply _)
  implicit val flightDataJSONReads: Reads[FlightData] = Json.reads[FlightData]
}
