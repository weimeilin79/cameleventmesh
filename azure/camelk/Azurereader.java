// camel-k: property-file=azurereader.properties trait=prometheus.enabled=true
package org.acme;

import org.apache.camel.builder.RouteBuilder;

public class Azurereader extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        // Invokes a simple greeting endpoint every 10 seconds
        from("amqp:queue:azure-bus")
            .log("Got -> ${body}")
            .filter().jsonpath("$[?(@.platform == 'azure')]" )
            .log("SENT -> ${body}")
            .to("knative:channel/notify")
        ;
    }
}