
# Use Your On-Demand Service
pushd kafka-example-app
cf push --no-start -b go_buildpack
cf bind-service kafka-example-app k1
cf start kafka-example-app
popd

# To write data, run
curl -XPOST http://kafka-example-app.local.pcfdev.io/queues/my-queue -d SOME-DATA
# To read data, run
curl http://kafka-example-app.local.pcfdev.io/queues/my-queue
