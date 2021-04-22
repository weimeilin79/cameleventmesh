# cameleventmesh

## INSTALL STEPS
## Install Operators
Install Camel K Operator
Install Kafka Operator
Install Serverless Operator

Create Kafka Cluster
Create KafkaTopic incident-all
Create KafkaTopic gcp-result

## Create the namespace for demo
```shell script
oc new-project demo
```

## Setup Cluster Monitoring for Indiviual namespaces & add monitoring to all camel-app (app-with-metrics)


```shell script
cd monitoring
oc apply -f cluster-monitoring-config.yaml -n openshift-monitoring
oc apply -f service-monitor.yaml
oc patch ip camel-k --type=merge -p '{"spec":{"traits":{"prometheus":{"configuration":{"enabled":true}}}}}'
```

Need to restart the monitoring operator
(For more info https://access.redhat.com/documentation/en-us/openshift_container_platform/4.6/html-single/monitoring/index)

Check of user monitoring is turned on by using 

```shell script
oc -n openshift-user-workload-monitoring get pod
```

## Setup Credentials for service now and kafka
```shell script
oc create -f servicenow-credentials.yaml
oc create -f kafka-config.yaml
```

## Deploying to OpenShift for Getting incidents ServiceNow 
```shell script
cd simple-servicenow
mvn clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.openshift.expose=true -Dquarkus.openshift.labels.app-with-metrics=camel-app
```

## Connecting to Google Cloud

Obtain your google cloud service account key
https://cloud.google.com/iam/docs/creating-managing-service-account-keys

Create a *gcp-topic* under Google pub/sub and make sure adding permissions to your service account

_*OPTIONAL*_
Create a google cloud function that triggered by the *gcp-topic* and does whatever you want it to do, in my case, just does some simple logging

```shell script
cd simple-gcp
oc create configmap gcp-configmap --from-file=google-service-acc-key.json
mvn clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.openshift.expose=true -Dquarkus.openshift.labels.app-with-metrics=camel-app

cd  camelk
kamel run Gcpreader.java
oc create -f kafka-source.kamelet.yaml
```

You can setup the binding by GUI as follows 

Kafka to Knative events (gcp-kafka-source)
    ```
    kind: Channel
    name: notify
    brokers: 'my-cluster-kafka-bootstrap.test.svc:9092'
    topic: gcp-result
    ```

or 

```shell script
oc crerate -f kamelet-kafka-binding.yaml
```

## Setup notification to Telegram

```shell script

cd serverless
kamel run --property-file=telebot-notify.properties --trait prometheus.enabled=true telebot-notify.yaml
```


## Connecting to Azure

Set up your Access control (IAM), with appropriate role and access
https://docs.microsoft.com/en-us/azure/role-based-access-control/role-assignments-portal

Go to Service Bus, and create a queue called *azure-bus*
Obtain the SAS Policy Connection String (Primary or Secondary)

_*OPTIONAL*_
Create a Azure cloud function that triggered by the *azure-bus* and does whatever you want it to do, in my case, just does some simple logging

```shell script
cd simple-azure
oc create -f azure-credentials.yaml
mvn clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.openshift.expose=true -Dquarkus.openshift.labels.app-with-metrics=camel-azure

cd camelk
kamel run Azurereader.java 
```


## Connecting to AWS

Setup your user in IAM, grant the permissions and setup the policies accrodingly

Under SNS, create a topic name *sns-topic* and setup the access policy for your user in IAM
Under SQS, create a topic name *sqs-queue* and setup the access policy for your user in IAM, and subscribe to the *sns-topic* we have created earlier on.

_*OPTIONAL*_
Create a lambda that subscribe to the *sns-topic* and does whatever you want it to do, in my case, just does some simple logging

```shell script
cd simple-aws
cd camelk

kamel run AwsCamel.java

cd ..
oc create -f aws-sqs-source.kamelet.yaml
```

AWS SQS (aws-sqs-source)
```

    kind: Channel
      name: notify
    source:
     properties:
      accessKey: REPLACE_ME
      autoCreateQueue: false
      deleteAfterRead: true
      queueNameOrArn: sqs-queue
      region: us-east-1
      secretKey: REPLACE_ME
    ref:
      apiVersion: camel.apache.org/v1alpha1
      kind: Kamelet
      name: aws-sqs-source
```
or 
```shell script
oc create -f kamelet-aws-sqs-binding.yaml
```


## Setup Camel Monitoring 
Create the grapha 

```shell script
oc create -f grafana.yaml
oc adm policy add-cluster-role-to-user cluster-monitoring-view -z grafana-serviceaccount
oc serviceaccounts get-token grafana-serviceaccount
sed "s/REPLACEME/$(oc serviceaccounts get-token grafana-serviceaccount)/" grafana-datasource.yaml.bak > grafana-datasource.yaml
oc create -f grafana-datasource.yaml
#oc create -f grafana-dashboard.yaml
```
IMPORT the grafana.json in Grafana

## INSTALL API


```shell script

cd serverless/api
oc create secret generic kafka-credential --from-file=incidentapi.properties
kamel run IncidentApi.java
```