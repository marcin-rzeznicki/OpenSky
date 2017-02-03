package opensky.collector

import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Main extends Setup {
  def main(args: Array[String]): Unit = {
    val done = OpenSkySource(endpoint, throttleControl, new Curl()).map(data => new ProducerRecord(sink, data)).runWith(Kafka)

    done.onComplete {
      case Success(_)     => logger.info("Done"); System.exit(0)
      case Failure(error) => logger.error(error, "OpenSkySource shutting down"); System.exit(1)
    }
    Await.result(done, Duration.Inf)
  }

}
