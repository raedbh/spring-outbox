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

import org.apache.camel.builder.RouteBuilder;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.debezium.data.Envelope.Operation.CREATE;
import static io.debezium.data.Envelope.Operation.READ;
import static org.apache.camel.component.debezium.DebeziumConstants.HEADER_KEY;
import static org.apache.camel.component.debezium.DebeziumConstants.HEADER_OPERATION;

/**
 * Configures a Camel-Debezium route to capture database changes and publish
 * them as {@link OutboxData} messages to RabbitMQ.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class DebeziumRabbitRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumRabbitRouteBuilder.class);

    private final String camelComponentUri;
    private final OutboxMessageProducer messageProducer;


    public DebeziumRabbitRouteBuilder(OutboxMessageProducer messageProducer, String camelComponentUri) {

        if (messageProducer == null) {
            throw new IllegalArgumentException("messageProducer cannot be null");
        }
        if (camelComponentUri == null) {
            throw new IllegalArgumentException("camelComponentUri cannot be null");
        }

        this.messageProducer = messageProducer;
        this.camelComponentUri = camelComponentUri;
    }


    @Override
    public void configure() {
        from(camelComponentUri).choice().when(header(HEADER_OPERATION).in(READ.code(), CREATE.code()))
          .process(camelExchange -> {

              String operation = camelExchange.getIn().getHeader(HEADER_OPERATION, String.class);
              Object body = camelExchange.getIn().getBody();

              LOGGER.info("Change processing [operation: {}] [body: {}]", operation, body);

              if (!(body instanceof Struct struct)) {
                  throw new IllegalArgumentException(
                    "Unsupported type for OutboxData instantiation: " + body.getClass().getTypeName());
              }

              messageProducer.produceMessage(OutboxDataMapper.toOutboxData(struct));
          })
          .otherwise() // delete -> "d" or update -> "u"
          .log("${header." + HEADER_OPERATION + "} operation detected."
            + " No action required. Key: ${header." + HEADER_KEY + "} | Body: ${body}")
          .end();
    }
}
