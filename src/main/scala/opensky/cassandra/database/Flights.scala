package opensky.cassandra.database

import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams.RequestBuilder
import opensky.domain._

sealed class FlightStates extends CassandraTable[ConcreteFlightStates, (Long, FlightState)] {
  object icao24        extends StringColumn(this) with PartitionKey
  object time          extends DateTimeColumn(this) with PrimaryKey
  object callsign      extends OptionalStringColumn(this)
  object originCountry extends StringColumn(this)
  object timePosition  extends OptionalFloatColumn(this)
  object timeVelocity  extends OptionalFloatColumn(this)
  object longitude     extends OptionalFloatColumn(this)
  object latitude      extends OptionalFloatColumn(this)
  object altitude      extends OptionalFloatColumn(this)
  object onGround      extends BooleanColumn(this)
  object velocity      extends OptionalFloatColumn(this)
  object heading       extends OptionalFloatColumn(this)
  object verticalRate  extends OptionalFloatColumn(this)
  object sensors       extends SetColumn[Long](this)

  override def fromRow(r: Row): FlightStateWithTime =
    (time(r).getMillis / 1000) ->
      FlightState(icao24(r),
                  callsign(r),
                  originCountry(r),
                  timePosition(r),
                  timeVelocity(r),
                  longitude(r),
                  latitude(r),
                  altitude(r),
                  onGround(r),
                  velocity(r),
                  heading(r),
                  verticalRate(r),
                  sensors(r).toVector)
}

abstract class ConcreteFlightStates extends FlightStates with RootConnector

object ConcreteFlightStates {
  implicit object QueryBuilder extends RequestBuilder[ConcreteFlightStates, FlightStateWithTime] {
    override def request(ct: ConcreteFlightStates, t: FlightStateWithTime)(implicit session: Session,
                                                                           keySpace: KeySpace): ExecutableStatement with Batchable = {
      val (time, flightState) = t
      ct.insert
        .value(_.time, new DateTime(time * 1000))
        .value(_.icao24, flightState.icao24)
        .value(_.callsign, flightState.callsign)
        .value(_.originCountry, flightState.originCountry)
        .value(_.timePosition, flightState.timePosition)
        .value(_.timeVelocity, flightState.timeVelocity)
        .value(_.longitude, flightState.longitude)
        .value(_.latitude, flightState.latitude)
        .value(_.altitude, flightState.altitude)
        .value(_.onGround, flightState.onGround)
        .value(_.velocity, flightState.velocity)
        .value(_.heading, flightState.heading)
        .value(_.verticalRate, flightState.verticalRate)
        .value(_.sensors, flightState.sensors.toSet)
    }
  }
}
