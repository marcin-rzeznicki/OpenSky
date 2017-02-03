package opensky.aggregator

import scala.concurrent.duration.Duration

trait Setup extends opensky.Setup {
  val aggreagatorConfig = openskyConfig.getConfig("aggregator")
  val source            = aggreagatorConfig.getString("source")
  val windowTime        = Duration.fromNanos(aggreagatorConfig.getDuration("window-time").toNanos)

  override val appName: String = "opensky-aggregator"
}
