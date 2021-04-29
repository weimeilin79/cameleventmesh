// camel-k: dependency=camel-quarkus-jsonpath trait=prometheus.enabled=true property-file=aws.properties
package org.acme;

import org.apache.camel.builder.RouteBuilder;

public class AwsCamel extends RouteBuilder {

    private final static String KAFKA_ADDRESS = "my-cluster-kafka-bootstrap.test.svc:9092";

    @Override
    public void configure() throws Exception {
        // Invokes a simple greeting endpoint every 10 seconds
        from("kafka:incident-all?groupId=awsreader")
            .log("Got -> ${body}")
            .filter().jsonpath("$[?(@.platform == 'aws')]" )
            .log("SENT -> ${body}")
            .to("aws2-sns://sns-topic?accessKey={{accessKey}}&secretKey={{secretKey}}&region={{region}}")
        ;
    }
}
