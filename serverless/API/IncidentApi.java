// camel-k:  dependency=camel-openapi-java trait=prometheus.enabled=true open-api=incidentapi.yaml secret=kafka-credential
package org.acme;

import org.apache.camel.builder.RouteBuilder;

public class IncidentApi extends RouteBuilder {



    @Override
    public void configure() throws Exception {
        // Invokes a simple greeting endpoint every 10 seconds
        from("direct:sendtoall")
            .log("SENT!!! -> ${body}")
            .to("kafka:incident-all")
        ;
    }
}