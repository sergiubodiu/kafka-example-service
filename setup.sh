# Target your BOSH Lite installation:
boshlite
# Upload the BOSH Lite stemcell:
bosh upload stemcell https://bosh.io/d/stemcells/bosh-warden-boshlite-ubuntu-trusty-go_agent?v=3262.2
# bosh upload stemcell ~/Downloads/bosh-stemcell-3262.2-warden-boshlite-ubuntu-trusty-go_agent.tgz --skip-if-exists

# Set Up the Kafka Example Service
pushd kafka-example-service-release
bosh create release --name kafka-example-service
bosh upload release
popd

# Set Up the Kafka Example Service Adapter
pushd kafka-example-service-adapter-release
bosh create release --name kafka-example-service-adapter
bosh upload release
popd

# Set Up the On-Demand Service Broker
bosh upload release ~/Downloads/on-demand-service-broker-0.11.0.tgz

# Create a BOSH Deployment
bosh update cloud-config cloud_config.yml
pushd bosh-lite
bosh deployment deployment_manifest.yml
echo yes | bosh deploy
popd

# Record the IP address of the broker. You will use this in the next step to create a service broker.
bosh instances
export BROKER_IP=10.244.0.2
export PCF_DEV_DOMAIN=local.pcfdev.io

# Create a Service Broker on PCF Dev
cf create-service-broker kafka-broker broker password http://$BROKER_IP:8080
cf enable-service-access kafka-service-with-odb
cf marketplace
cf create-service kafka-service-with-odb small k1

# Verify Your BOSH Deployment and On-Demand Service
echo 'Verify your bosh deployments by running $ bosh deployments'
watch -n 1 cf service k1
