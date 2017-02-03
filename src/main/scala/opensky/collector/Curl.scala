package opensky.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

private[collector] class Curl(implicit actorSystem: ActorSystem) {
  private val client = Http()

  def get(endpoint: String)(implicit ec: ExecutionContext, mat: Materializer): Future[String] =
    client
      .singleRequest(HttpRequest(uri = endpoint))
      .flatMap(resp =>
        resp.status match {
          case status if status.isSuccess() => Unmarshal(resp.entity).to[String]
          case error                        => Future.failed(sys.error(s"GET $endpoint: ${error.intValue()}"))
      })
}
