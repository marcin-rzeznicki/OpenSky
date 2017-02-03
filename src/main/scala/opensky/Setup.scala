package opensky

import akka.actor.ActorSystem
import akka.event.Logging
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}

trait Setup {
  val config        = ConfigFactory.load()
  val openskyConfig = config.getConfig("opensky")

  def appName: String
  implicit val executionContext  = ExecutionContext.global
  implicit lazy val system       = ActorSystem(appName, config)
  implicit lazy val materializer = ActorMaterializer()
  implicit lazy val logger       = Logging(system, appName)

  lazy val consumerSettings =
    ConsumerSettings(system, keyDeserializer = None, valueDeserializer = Some(new StringDeserializer)).withGroupId(appName)
  lazy val producerSettings =
    ProducerSettings(system, keySerializer = None, valueSerializer = Some(new StringSerializer))
      .withProperty(ProducerConfig.CLIENT_ID_CONFIG, appName)

  protected def Kafka = Producer.plainSink[Nothing, String](producerSettings)
  protected def KafkaSource(topics: String*) =
    Consumer.atMostOnceSource[Nothing, String](consumerSettings, Subscriptions.topics(topics.toSet))
}

case class ThrottleControl(elements: Int, per: FiniteDuration, maximumBurst: Int)

object ThrottleControl {
  def forConfig(config: Config): ThrottleControl = {
    val throttleControlConfig = config.getConfig("throttle-control")
    ThrottleControl(throttleControlConfig.getInt("elements"),
                    Duration.fromNanos(throttleControlConfig.getDuration("per").toNanos),
                    throttleControlConfig.getInt("maximum-burst"))
  }
}
