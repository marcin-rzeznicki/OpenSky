package opensky.cassandra

import akka.kafka.scaladsl.Consumer.Control
import akka.stream.scaladsl.Source
import opensky.Protocol
import opensky.cassandra.database.OpenSkyDatabase
import opensky.domain._
import play.api.libs.json.JsSuccess

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Main extends Setup with Protocol {
  def openSkyDataSource: Source[FlightData, Control] =
    KafkaSource(kafkaSource).map(msg => msg.value.parseJson[FlightData]).collect { case JsSuccess(data, _) => data }

  val openSkyDatabase = new OpenSkyDatabase(connector)
  logger.info(s"Creating schema in ${connector.name}")
  openSkyDatabase.create(autocreateTimeout)

  def main(args: Array[String]): Unit = {
    val done = openSkyDataSource
      .log("cassandra-feed", flightData => s"About to insert ${flightData.states.size} elements to Cassandra")
      .async
      .mapConcat(flightData => flightData.states.map(flightData.time -> _))
      .runWith(CassandraSink(openSkyDatabase, subscriberConfig))

    done.onComplete {
      case Success(_)     => logger.info("Done"); System.exit(0)
      case Failure(error) => logger.error(error, "Shutting down"); System.exit(1)
    }
    Await.result(done, Duration.Inf)
  }

}
