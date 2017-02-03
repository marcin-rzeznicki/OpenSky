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


### How to run

Quck and dirty version:

1. Run `sbt`
1. Run `stage`
1. `cd target/universal/stage`
1. change configs in `conf/` to your liking
1. run `bin/opensky-collector` and/or `bin/opensky-aggregator`

Longer version

1. Run `sbt`
1. Run `universal:packageZipTarball` (or any other packaging you like)
1. Extract `target/universal/opensky-xx.tgz` somewhere and `cd` there
1. change configs in `conf/` to your liking
1. run `bin/opensky-collector` and/or `bin/opensky-aggregator`

### DISCLAIMER
For review only. All rights reserverd Iterators sp z o.o