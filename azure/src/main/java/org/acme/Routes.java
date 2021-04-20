package org.acme;

import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {


        from("kafka:incident-all?brokers={{kafka.brokers}}&groupId=azurereader")
                //.unmarshal().json()
                .filter().jsonpath("$[?(@.platform == 'azure')]")
                .multicast()
                .parallelProcessing().to(
                        "amqp:queue:azure-bus",
                        "azure-eventhubs:/camelazure/azure-eventhub?connectionString=RAW(Endpoint={{eventhub.endpoint}})"
                );

    }
}
