package org.example.fuseworkshop;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.ExchangePattern;

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
            .post("/new").consumes("text/plain")
            .to("direct:restNew")
            .post("/test").consumes("text/plain")
            .to("direct:restTest");

	from("direct:restNew").routeId("restNew")
		.log("Rest call to New")
                .to(ExchangePattern.InOnly, "activemq:queue:submission");
	from("direct:restTest").routeId("restTest")
		.log("Rest call to Test")
		.setBody().simple("Returning ${body}");

        from("activemq:queue:submission").routeId("restFromQueue")
                .log("PDF Taken from Queue")
                .convertBodyTo(String.class)
                .to("pdf:create")
                //.setHeader(FILE_NAME, constant ("test-${date:now:yyyyMMdd-HHmmss}.pdf"))
		.setHeader(FILE_NAME, constant ("test.pdf"))
                .to("file://output")
          ;
    }

    public void setName(String name) {
        this.name = name;
    }
}
