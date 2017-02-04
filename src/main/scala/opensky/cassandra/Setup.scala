package opensky.cassandra

import com.outworkers.phantom.connectors.ContactPoints
import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try

trait Setup extends opensky.Setup {
  val cassandraConfig = openskyConfig.getConfig("cassandra")
  val connector = {
    import scala.collection.JavaConverters._
    val hosts      = cassandraConfig.getStringList("seeds").asScala
    val port       = cassandraConfig.getInt("port")
    val noHearbeat = cassandraConfig.getBoolean("no-heartbeat")
    val keySpace   = cassandraConfig.getString("key-space")

    val builder = if (noHearbeat) ContactPoints(hosts, port).noHeartbeat() else ContactPoints(hosts, port)
    builder.keySpace(keySpace)
  }
  val subscriberConfig  = SubscriberConfig.fromConfig(cassandraConfig)
  val kafkaSource       = cassandraConfig.getString("kafka-source")
  val autocreateTimeout = Duration.fromNanos(cassandraConfig.getDuration("autocreate-timeout").toNanos)

  override val appName = "opensky-cassandra-feed"
}

case class SubscriberConfig(batchSize: Int, concurrentRequests: Int, flushInterval: Option[FiniteDuration])
object SubscriberConfig {
  def fromConfig(config: Config): SubscriberConfig = {
    val subscriberConfig = config.getConfig("subscriber")
    SubscriberConfig(subscriberConfig.getInt("batch-size"),
                     subscriberConfig.getInt("concurrent-requests"),
                     Try(Duration.fromNanos(subscriberConfig.getDuration("flush-interval").toNanos)).toOption)
  }
}
