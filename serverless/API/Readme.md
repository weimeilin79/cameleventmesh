API input

{
    "request": "Request File From Azure",
    "apino": "API0010001",
    "request owner": "Christina",
    "platform": "azure"
}


Kafka Input

{
    "content": "Request File From Azure",
    "incidentno": "API0010001",
    "owner": "Christina",
    "platform": "azure"
}


{
    "content": "Request Service from AWS",
    "incidentno": "API0010002",
    "owner": "Christina",
    "platform": "aws"
}

{
    "content": "Shutdown application in Google Cloud",
    "incidentno": "API0010003",
    "owner": "Christina",
    "platform": "gcp"
}



oc create secret generic kafka-credential --from-file=incidentapi.properties
kamel run IncidentApi.java --dev


curl --location --request POST 'http://localhost/request:8080' \
--header 'Content-Type: application/json' \
--data-raw '{
    "request": "Request File From Azure",
    "apino": "API0010001",
    "request owner": "Christina",
    "platform": "gcp"
}'