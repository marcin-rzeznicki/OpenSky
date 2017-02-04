package opensky

import opensky.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.language.implicitConversions

trait Protocol {
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

  import Protocol._
  implicit def toReadOps(jsonString: String): JsonReadOps   = new JsonReadOps(jsonString)
  implicit def toWriteOps[A: Writes](a: A): JsonWriteOps[A] = new JsonWriteOps[A](a)
}

object Protocol {
  class JsonWriteOps[A: Writes](val a: A) {
    def toJson: String = Json.stringify(Json.toJson(a))
  }

  class JsonReadOps(val jsonString: String) extends AnyVal {
    def parseJson[A: Reads]: JsResult[A] = Json.parse(jsonString).validate[A]
  }
}
