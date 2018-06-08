package org.example.fuseworkshop;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;

public class SimpleRouteTest extends CamelBlueprintTestSupport {

    @Override protected String getBlueprintDescriptor() {
        return "/blueprint/blueprint-test.xml";
    }

    @Test
    public void testCamelRoute() throws Exception {

        context.getRouteDefinition("simpleRoute").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast()
                        .to("mock:to");
            }
        });
        getMockEndpoint("mock:to").expectedMinimumMessageCount(1);
        getMockEndpoint("mock:to").expectedBodiesReceived("Hello Test");

        assertMockEndpointsSatisfied();
    }
}
