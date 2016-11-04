
# Use Your On-Demand Service
pushd kafka-example-app
cf push --no-start
cf bind-service kafka-example-app k1
cf start kafka-example-app
popd

## Deprecated
# To write data, run
curl http://kafka-example-app.local.pcfdev.io/hi/SOME-NAME
# To read data, run
curl http://kafka-example-app.local.pcfdev.io/bye
