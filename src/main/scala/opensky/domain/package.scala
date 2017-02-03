package opensky

package object domain {
  type Icao24           = String
  type Flights          = Vector[FlightState]
  type FlightsByCountry = Map[String, Set[Icao24]]

  implicit class FlightsByCountryOps(val self: FlightsByCountry) extends AnyVal {
    def updatedWith(flightData: FlightData): FlightsByCountry =
      (self /: flightData.states) { (result, newFlight) =>
        val country = newFlight.originCountry
        val newIcao = newFlight.icao24
        result.get(country) match {
          case Some(flights) if flights contains newIcao => result
          case Some(flights)                             => result.updated(country, flights + newIcao)
          case None                                      => result.updated(country, Set(newIcao))
        }
      }
  }
}
