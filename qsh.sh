#!/bin/bash

NS=quarkus-super-heroes
kubectl create ns ${NS}

# Install Postgresql and Kafka Operator

# https://github.com/zalando/postgres-operator/blob/master/docs/quickstart.md
helm repo add postgres-operator-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator
helm install postgres-operator postgres-operator-charts/postgres-operator -n quarkus-super-heroes

# https://github.com/strimzi/strimzi-kafka-operator/tree/main/helm-charts/helm3/strimzi-kafka-operator
helm repo add strimzi https://strimzi.io/charts/
helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n quarkus-super-heroes

kubectl apply -n ${NS} -f super-heroes/kubernetes
