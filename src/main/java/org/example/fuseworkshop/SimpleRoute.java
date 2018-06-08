package org.example.fuseworkshop;

import org.apache.camel.builder.RouteBuilder;

public class SimpleRoute extends RouteBuilder {

    private String name;

    @Override public void configure() throws Exception {
        from("timer://foo?period=5000").routeId("simpleRoute")
                .setBody().constant("Hello "+ name)
                .log(">>> ${body}")
                ;
    }

    public void setName(String name) {
        this.name = name;
    }
}

