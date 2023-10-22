# ML gRPC Services (DEPRECATED)
 
sbt 1.3.8 required to run this example

To run service with model:
1. generate scala code from proto: `sbt compile`
2. start service:  `sbt "Compile / runMain app.StartServer"`
3. start client for generating test messages/models:  `sbt "Compile / runMain client.Client"`

parameters can be changed in `src/main/resources/reference.conf`

`sbt test` to run unit tests

# Typelevel-stack service

Same purpose, better approach (uses kafka instead of direct grpc connection)
1) start kafka: `docker compose up -d`
2) start prediction service: `sbt "typelevelService / runMain app.PredictorApp"` (or in IDE)
3) start emitting records: `sbt "typelevelService / runMain emitters.StreamEmitterApp"` (or in IDE)
