package org.acme;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.component.servicenow.ServiceNowParams;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

    
        // Invokes a simple greeting endpoint every 10 seconds
        from("timer:incident?period=45000")
        .setHeader(ServiceNowConstants.RESOURCE, constant("table"))
        .setHeader(ServiceNowConstants.ACTION, constant(ServiceNowConstants.ACTION_RETRIEVE))
        .setHeader("CamelServiceNowLimit", constant("10"))
        .setHeader("CamelServiceNowExcludeReferenceLink", constant("false"))
        .setHeader("CamelServiceNowTable", constant("incident"))
        .setHeader("CamelServiceNowQuery", constant("state=1"))
        .log("incident coming------>")
        .to("servicenow:{{SERVICENOW_INSTANCE}}"
               + "?userName={{SERVICENOW_USERNAME}}"
               + "&password={{SERVICENOW_PASSWORD}}"
               + "&oauthClientId={{SERVICENOW_OAUTH2_CLIENT_ID}}"
               + "&oauthClientSecret={{SERVICENOW_OAUTH2_CLIENT_SECRET}}")
        .split().body()
            .marshal().json()
            .log("Before ${body}")
            .to("atlasmap:servicenow.adm")
            .to("kafka:incident-all?brokers={{kafka.brokers}}");


        //from("kafka:incident-all?brokers={{kafka.brokers}}&groupId=testreader")
        //    .log("After ${body}");

    }
}