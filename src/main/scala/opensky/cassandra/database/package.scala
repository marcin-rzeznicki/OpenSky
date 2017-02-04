package opensky.cassandra

import opensky.domain._

package object database {
  type FlightStateWithTime = (Long, FlightState)
}
