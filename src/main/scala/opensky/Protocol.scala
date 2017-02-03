package opensky

import play.api.libs.json._

import scala.language.implicitConversions

trait Protocol {
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
