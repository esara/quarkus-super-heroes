#!/bin/bash

NS=quarkus-super-heroes
kubectl create ns ${NS}

if [ "$1" = "crossplane-aws" ]; then
# Install crossplane operator

# https://docs.crossplane.io/master/getting-started/provider-aws/
helm repo add crossplane-stable https://charts.crossplane.io/stable
helm upgrade --install crossplane crossplane-stable/crossplane --namespace crossplane-system --create-namespace

# Create crossplane aws provider
cat <<EOF | kubectl apply -f -
apiVersion: pkg.crossplane.io/v1
kind: Provider
metadata:
  name: provider-aws
spec:
  package: xpkg.upbound.io/crossplane-contrib/provider-aws:v0.56.0
EOF

# kubectl create secret generic aws-secret -n crossplane-system --from-file=creds=./crossplane/aws-credentials.txt

# Create crossplane aws provider configuration
cat <<EOF | kubectl apply -f -
apiVersion: aws.crossplane.io/v1beta1
kind: ProviderConfig
metadata:
  name: default
spec:
  credentials:
    source: Secret
    secretRef:
      namespace: crossplane-system
      name: aws-secret
      key: creds
EOF

kubectl apply -n ${NS} -f deploy/crossplane-aws

else
# Install Postgresql and Kafka Operator

# https://github.com/zalando/postgres-operator/blob/master/docs/quickstart.md
helm repo add postgres-operator-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator
helm upgrade --install postgres-operator postgres-operator-charts/postgres-operator -n ${NS} --set configGeneral.kubernetes_use_configmaps=true --set configKubernetes.enable_readiness_probe=true
helm upgrade --install prometheus-postgres-exporter prometheus-community/prometheus-postgres-exporter --namespace quarkus-super-heroes --values deploy/kubernetes/exporters/prometheus_postgres_values.yaml

# https://github.com/strimzi/strimzi-kafka-operator/tree/main/helm-charts/helm3/strimzi-kafka-operator
helm repo add strimzi https://strimzi.io/charts/
# first version to support KRaft
#helm upgrade --install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n ${NS} --version 0.29.0 --set featureGates=+UseKRaft
# last version to support zookeeper
#helm upgrade --install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n ${NS} --version 0.45.0
# starting with 0.46.0 only KRaft
helm upgrade --install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n ${NS}
helm upgrade --install kafka-exporter prometheus-community/prometheus-kafka-exporter --namespace=quarkus-super-heroes --values deploy/kubernetes/exporters/prometheus_kafka_values.yaml

kubectl apply -n ${NS} -f deploy/kubernetes
fi
