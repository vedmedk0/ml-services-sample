# Code Challenge

To run service with model:
1. generate scala code from proto: `sbt compile`
2. start service:  `sbt "Compile / runMain app.StartServer"`
3. start client for generating test messages/models:  `sbt "Compile / runMain client.Client"`

parameters can be changed in reference.conf

`sbt test` to run unit tests 
