/*
 *  Copyright 2024 the original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.raedbh.spring.outbox.connector.core;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.debezium.data.Envelope.Operation.CREATE;
import static io.debezium.data.Envelope.Operation.DELETE;
import static io.debezium.data.Envelope.Operation.READ;
import static io.debezium.data.Envelope.Operation.UPDATE;
import static org.apache.camel.component.debezium.DebeziumConstants.HEADER_OPERATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DebeziumRabbitRouteBuilder}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class DebeziumRabbitRouteBuilderTests extends CamelTestSupport {

    static final String ENDPOINT_URI = "direct:start";

    ProducerTemplate producerTemplate;
    OutboxMessageProducer messageProducer;

    @BeforeEach
    public void setupContext() throws Exception {

        messageProducer = mock(OutboxMessageProducer.class);

        CamelContext camelContext = context();
        camelContext.addRoutes(new DebeziumRabbitRouteBuilder(messageProducer, ENDPOINT_URI));

        producerTemplate = camelContext.createProducerTemplate();
    }

    @Test
    void rejectNullMessageProducer() {
        assertThat(catchThrowable(() -> new DebeziumRabbitRouteBuilder(null, ENDPOINT_URI)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("messageProducer");
    }

    @Test
    void rejectNullCamelComponentUri() {
        assertThat(catchThrowable(() -> new DebeziumRabbitRouteBuilder(messageProducer, null)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("camelComponentUri");
    }

    @Test
    void produceMessageForRead() throws Exception {
        producerTemplate.sendBodyAndHeader(ENDPOINT_URI, struct(), HEADER_OPERATION, READ.code());
        verify(messageProducer).produceMessage(any());
    }

    @Test
    void produceMessageForCreate() throws Exception {
        producerTemplate.sendBodyAndHeader(ENDPOINT_URI, struct(), HEADER_OPERATION, CREATE.code());
        verify(messageProducer).produceMessage(any());
    }

    @Test
    void noMessageProducedForDelete() throws Exception {
        producerTemplate.sendBodyAndHeader(ENDPOINT_URI, struct(), HEADER_OPERATION, DELETE.code());
        verify(messageProducer, times(0)).produceMessage(any());
    }

    @Test
    void noMessageProducedForUpdate() throws Exception {
        producerTemplate.sendBodyAndHeader(ENDPOINT_URI, struct(), HEADER_OPERATION, UPDATE.code());
        verify(messageProducer, times(0)).produceMessage(any());
    }

    @Test
    void rejectUnsupportedBodyType() {
        var exception = catchThrowable(() ->
          producerTemplate.sendBodyAndHeader(ENDPOINT_URI, "UnsupportedBodyType", HEADER_OPERATION, CREATE.code())
        );

        assertThat(exception).isInstanceOf(CamelExecutionException.class);
        assertThat(exception.getCause())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unsupported type");
    }

    private Struct struct() {
        var struct = new Struct(schema());
        struct.put("id", "12345");
        struct.put("type", "Type");
        struct.put("payload", "The Payload".getBytes());
        return struct;
    }

    private Schema schema() {
        return SchemaBuilder.struct().name("test")
          .field("id", Schema.STRING_SCHEMA)
          .field("type", Schema.STRING_SCHEMA)
          .field("payload", Schema.BYTES_SCHEMA)
          .field("related_to", Schema.OPTIONAL_STRING_SCHEMA)
          .field("metadata", Schema.OPTIONAL_STRING_SCHEMA)
          .build();
    }
}
