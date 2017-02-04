## OpenSky project

### Why?

To demonstrate how to use `kafka` and `akka-stream` to do time-window processing

### Architecture

There are two components: `collector` and `aggregator`.

`Collector` is very simple - it just periodically pulls the data from `OpenSky` endpoint and pushes it to Kafka

`Aggregator` is a bit more complicated. It pulls the data out of Kafka, parses it and does aggregation in a configurable time-window.
It does this by employing backpressure. One (custom) element called `Pulse` provides constant backpressure. It is preceded by `conflateWithSeed` (the standard one),
which aggregates data from Kafka while there is backpressure.
Once every `n` minutes `Pulse` signals demand, thus flushing `conflate` and conveying aggregated data downstream.
The main benefit of this solution is that processing from Kafka can go at its own pace and there is no danger of overflowing or dropping elements.
Also, only aggregated data needs to be kept in memory during processing, which should lessen the GC pressure.

One additional component is `cassandra-feed` which demonstrates how to pull the data out of Kafka and store it in Cassandra.
It does it independenty of `aggregator` (by virtue of different `groupId`). It uses `phantom-dsl` and `phantom-streams` to make feeding Cassandra cluster totally reactive
(automatic concurrent batching implemented on top of Cassandra `subscriber`). In these types of system shape of data has to vary based on querying needs, so an arbitrary representation had to be chosen.
The data is structured as if you manually did:

```
CREATE TABLE opensky.flightstates (
    icao24 text,
    time timestamp,
    altitude float,
    callsign text,
    heading float,
    latitude float,
    longitude float,
    onground boolean,
    origincountry text,
    sensors set<bigint>,
    timeposition float,
    timevelocity float,
    velocity float,
    verticalrate float,
    PRIMARY KEY (icao24, time)
)
```

This form makes it easy to follow a plane history through time (partitioned by plane identifier, clustered on time dimension)
Example output:

| icao24 | time                            | altitude    | callsign | heading   | latitude | longitude | onground | origincountry | sensors | timeposition | timevelocity | velocity  | verticalrate |
| ------ | ------------------------------- | ----------- | -------- | --------- | -------- | --------- | -------- | ------------- | ------- | ------------ | ------------ | --------- | ------------ |
| a1589d | 2017-02-03 14:18:50.000000+0000 |  2529.84009 |     null | 330.67001 |  37.7304 | -122.0352 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 | 148.10001 |        22.43 |
| a1589d | 2017-02-03 14:19:00.000000+0000 |  2773.67993 |     null | 325.23999 |  37.7429 |  -122.045 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |    145.27 |        18.21 |
| a1589d | 2017-02-03 14:19:10.000000+0000 |  2910.84009 |     null |    323.94 |  37.7514 | -122.0527 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |    145.09 |        16.91 |
| a1589d | 2017-02-03 14:19:20.000000+0000 |   3086.1001 |     null |    322.97 |   37.763 | -122.0635 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 | 146.92999 |        15.61 |
| a1589d | 2017-02-03 14:19:30.000000+0000 |  3223.26001 |     null | 323.60999 |  37.7724 | -122.0722 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |    148.27 |        14.63 |
| a1589d | 2017-02-03 14:19:40.000000+0000 |  3352.80005 |     null |    323.66 |  37.7851 | -122.0839 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |    154.55 |         6.83 |
| a1589d | 2017-02-03 14:19:50.000000+0000 |  3474.71997 |     null | 324.85001 |   37.798 | -122.0957 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 | 157.28999 |        17.56 |
| a1589d | 2017-02-03 14:20:00.000000+0000 |  3611.87988 |     null | 327.79999 |  37.8071 | -122.1037 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 | 159.28999 |        16.26 |
| a1589d | 2017-02-03 14:20:10.000000+0000 |  3703.32007 |     null | 337.70999 |  37.8177 | -122.1111 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |     166.8 |          5.2 |
| a1589d | 2017-02-03 14:20:20.000000+0000 |  3779.52002 |     null | 351.35001 |  37.8356 | -122.1179 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 | 177.96001 |         7.15 |
| a1589d | 2017-02-03 14:20:30.000000+0000 |   3848.1001 |     null |    359.84 |  37.8491 | -122.1196 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |    183.66 |         11.7 |
| a1589d | 2017-02-03 14:20:40.000000+0000 |  3977.63989 |     null |      1.11 |  37.8668 | -122.1194 |    False | United States |    null |   1.4861e+09 |   1.4861e+09 |    185.75 |        14.31 |

### How to run

Quck and dirty version:

1. Run `sbt`
1. Run `stage`
1. `cd target/universal/stage`
1. change configs in `conf/` to your liking
1. run `bin/opensky-collector` and/or `bin/opensky-aggregator` and/or `bin/opensky-cassandra-feed`

Longer version

1. Run `sbt`
1. Run `universal:packageZipTarball` (or any other packaging you like)
1. Extract `target/universal/opensky-xx.tgz` somewhere and `cd` there
1. change configs in `conf/` to your liking
1. run `bin/opensky-collector` and/or `bin/opensky-aggregator` and/or `bin/opensky-cassandra-feed`

### DISCLAIMER
For review only. All rights reserverd Iterators sp z o.o