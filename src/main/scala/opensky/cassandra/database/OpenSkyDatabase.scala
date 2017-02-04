package opensky.cassandra.database

import com.outworkers.phantom.dsl._

class OpenSkyDatabase(override val connector: KeySpaceDef) extends Database[OpenSkyDatabase](connector) {
  object flightStates extends ConcreteFlightStates with connector.Connector
}
