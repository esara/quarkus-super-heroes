#!/bin/bash

# https://sdk.operatorframework.io/docs/installation/
bash -c "$(curl -fsSL https://github.com/operator-framework/operator-lifecycle-manager/releases/download/v0.22.0/install.sh)" -- v0.22.0
NS=quarkus-super-heroes
kubectl create ns ${NS}

# Install Postgresql and Kafka Operator
cat << EOF | kubectl -n ${NS} apply -f -
apiVersion: operators.coreos.com/v1
kind: OperatorGroup
metadata:
  annotations:
    olm.providedAPIs: Backup.v1alpha1.postgresql.dev4devs.com,Database.v1alpha1.postgresql.dev4devs.com,Kafka.v1beta2.kafka.strimzi.io,KafkaBridge.v1beta2.kafka.strimzi.io,KafkaConnect.v1beta2.kafka.strimzi.io,KafkaConnector.v1beta2.kafka.strimzi.io,KafkaMirrorMaker.v1beta2.kafka.strimzi.io,KafkaMirrorMaker2.v1beta2.kafka.strimzi.io,KafkaRebalance.v1beta2.kafka.strimzi.io,KafkaTopic.v1beta2.kafka.strimzi.io,KafkaUser.v1beta2.kafka.strimzi.io,StrimziPodSet.v1beta2.core.strimzi.io
  name: quarkus-super-heroes-dtf5v
  namespace: ${NS}
spec:
  targetNamespaces:
    - quarkus-super-heroes
EOF

cat << EOF | kubectl -n ${NS} apply -f -
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  labels:
    operators.coreos.com/postgresql-operator-dev4devs-com.quarkus-super-heroes: ""
  name: postgresql-operator-dev4devs-com
  namespace: ${NS}
spec:
  name: postgresql-operator-dev4devs-com
  source: operatorhubio-catalog
  sourceNamespace: olm
EOF
until kubectl get crd databases.postgresql.dev4devs.com >> /dev/null 2>&1; do sleep 5; done

cat << EOF | kubectl apply -f -
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  labels:
    operators.coreos.com/strimzi-kafka-operator.quarkus-super-heroes: ''
  name: strimzi-kafka-operator
  namespace: ${NS}
spec:
  name: strimzi-kafka-operator
  source: operatorhubio-catalog
  sourceNamespace: olm
EOF
until kubectl get crd kafkas.kafka.strimzi.io >> /dev/null 2>&1; do sleep 5; done

kubectl apply -n ${NS} -f super-heroes/kubernetes
