package org.example.fuseworkshop;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.ExchangePattern;

import static org.apache.camel.Exchange.FILE_NAME;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class SimpleRoute extends RouteBuilder {

    private String name;

    @Override public void configure() throws Exception {
        from("timer://foo?period=15000").routeId("simpleRoute")
                .setBody().constant("Goodbye AAA "+ name)
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
            .post("/test").consumes("text/plain")
            .to("direct:restTest")
            .post("/async").consumes("text/plain")
            .to("direct:restAsync")
            .post("/sync").consumes("text/plain")
            .to("direct:restSync")
            ;

	from("direct:restTest").routeId("restTest")
		.log("Rest call to Test")
		.setBody().simple("Test Success")
		.setHeader(HTTP_RESPONSE_CODE, constant(200));

	from("direct:restAsync").routeId("restAsync")
		.log("Rest call to Async-In")
                .to(ExchangePattern.InOnly, "activemq:queue:async")
		.log("Rest call to Async-Out")
		.setBody().simple("File Submitted")
                .setHeader(HTTP_RESPONSE_CODE, constant(200));


	from("direct:restSync").routeId("restSync")
		.log("Rest call to Sync-In")
                .to(ExchangePattern.InOut, "activemq:queue:sync")
		.log("Rest call to Sync-Out")
		//.setBody().simple("File Submitted")
                .setHeader(HTTP_RESPONSE_CODE, constant(200));


        from("activemq:queue:async?disableReplyTo=true").routeId("restAsyncQueue")
                .log("PDF Taken from Async Queue-In")
                .convertBodyTo(String.class)
                //.to("pdf:create")
		.log("PDF Taken from Async Queue-After PDF")
                //.setHeader(FILE_NAME, constant ("test-${date:now:yyyyMMdd-HHmmss}.pdf"))
		.setHeader(FILE_NAME, constant ("test.pdf"))
                .to("file://output")
		.log("PDF Taken from Async Queue-After File");

        from("activemq:queue:sync").routeId("restSyncQueue")
                .log("PDF Taken from Sync Queue-In")
                .convertBodyTo(String.class)
                //.to("pdf:create")
		.log("PDF Taken from Sync Queue-After PDF")
                //.setHeader(FILE_NAME, constant ("test-${date:now:yyyyMMdd-HHmmss}.pdf"))
		.setHeader(FILE_NAME, constant ("test.pdf"))
                .to("file://output")
		.setBody().simple("File Saved")
		.log("PDF Taken from Sync Queue-After File");
    }

    public void setName(String name) {
        this.name = name;
    }
}
