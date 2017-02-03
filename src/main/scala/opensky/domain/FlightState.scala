package opensky.domain

case class FlightState(icao24: Icao24,
                       callsign: Option[String],
                       originCountry: String,
                       timePosition: Option[Float],
                       timeVelocity: Option[Float],
                       longitude: Option[Float],
                       latitude: Option[Float],
                       altitude: Option[Float],
                       onGround: Boolean,
                       velocity: Option[Float],
                       heading: Option[Float],
                       verticalRate: Option[Float],
                       sensors: Vector[Long])
