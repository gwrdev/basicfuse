package org.example.fuseworkshop;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import static org.apache.camel.Exchange.FILE_NAME;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class SimpleRoute extends RouteBuilder {

    private String name;

    @Override public void configure() throws Exception {
        from("timer://foo?period=5000").routeId("simpleRoute")
                .setBody().constant("Goodbye "+ name)
                .log(">>> ${body}")
                ;
        from("file://input?include=.*\\.txt").routeId("fileProc")
                .log(">>> ${body}")
                .to("activemq:queue:fileQueue")
                ;

        restConfiguration()
                .component("restlet")
                .host("localhost")
                .port("9095")
                .bindingMode(RestBindingMode.off);

        rest("/submission")
            .post("/").consumes("text/plain")
            .route().routeId("restIn")
                .to("activemq:queue:submission")
                .setExchangePattern(ExchangePattern.InOnly)
                .setHeader(HTTP_RESPONSE_CODE, constant(200));

        from("activemq:queue:submission").routeId("restProc")
                .log("PDF Taken from Queue")
                .convertBodyTo(String.class)
                .to("pdf:create")
                .setHeader(FILE_NAME, constant ("test-${date:now:yyyyMMdd-HHmmss}.pdf"))
                .to("file://output")
          ;
    }

    public void setName(String name) {
        this.name = name;
    }
}
