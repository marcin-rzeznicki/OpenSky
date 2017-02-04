package opensky.cassandra

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.outworkers.phantom.streams._
import opensky.cassandra.database.OpenSkyDatabase
import opensky.domain.FlightState

import scala.concurrent.{Future, Promise}

object CassandraSink {
  def apply(openSkyDatabase: OpenSkyDatabase, subscriberConfig: SubscriberConfig)(
      implicit system: ActorSystem): Sink[(Long, FlightState), Future[Done]] = {
    import openSkyDatabase.flightStates._
    val promise = Promise[Done]
    val subscriber = openSkyDatabase.flightStates.subscriber(batchSize = subscriberConfig.batchSize,
                                                             concurrentRequests = subscriberConfig.concurrentRequests,
                                                             flushInterval = subscriberConfig.flushInterval,
                                                             completionFn = () => promise.success(Done),
                                                             errorFn = t => promise.failure(t))
    Sink.fromSubscriber(subscriber).mapMaterializedValue(_ => promise.future)
  }
}
