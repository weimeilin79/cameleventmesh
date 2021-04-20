// camel-k: env=GOOGLE_APPLICATION_CREDENTIALS=/etc/camel/conf.d/_configmaps/gcp-configmap/google-service-acc-key.json configmap=gcp-configmap configmap=kafka-config trait=prometheus.enabled=true property=application.properties

import org.apache.camel.builder.RouteBuilder;


public class Gcpreader extends RouteBuilder {

    //private final static String KAFKA_ADDRESS = "my-cluster-kafka-bootstrap.test.svc:9092";

    @Override
    public void configure() throws Exception {
        
        from("google-pubsub://camel-k-metrics:gcp-topic-sub")
        .log("${body} Incident [] request for GCP resource update COMPLETED!!")
        .to("kafka:gcp-result");
        
        
    }
    
}
