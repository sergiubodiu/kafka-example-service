# Example Service on [Kafka open source messaging](http://kafka.apache.org/)
This guide describes how to create and manage an on-demand service using PCF Dev and BOSH lite https://docs.pivotal.io/on-demand-service-broker/0-11-0/getting-started.html

## Update the service dependencies:

    $ git submodule update --init --recursive

Setup on-demand service-broker

    $ setup.sh

    To be continued

## Run demo application

    # Use Your On-Demand Service
    pushd kafka-example-app
    mvn clean package -DskipTests
    cf push --no-start
    cf bind-service kafka-example-app k1
    cf start kafka-example-app
    popd

    ## Deprecated
    # To write data, run
    curl http://kafka-example-app.local.pcfdev.io/hi/SOME-NAME
    # To read data, run
    curl http://kafka-example-app.local.pcfdev.io/bye

## Backlog

    - [X] Demo application with ([Spring Kafka](http://docs.spring.io/spring-kafka/reference/htmlsingle/))
    - [ ] Recording
    - [x] PCF Dev
    - [x] bosh-lite
    - [x] Service Broker
