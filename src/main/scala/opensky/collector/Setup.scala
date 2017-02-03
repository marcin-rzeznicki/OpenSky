package opensky.collector

import opensky.ThrottleControl

trait Setup extends opensky.Setup {
  val collectorConfig = openskyConfig.getConfig("collector")
  val endpoint        = collectorConfig.getString("endpoint")
  val sink            = collectorConfig.getString("sink")
  val throttleControl = ThrottleControl.forConfig(collectorConfig)

  override val appName: String = "opensky-collector"
}
