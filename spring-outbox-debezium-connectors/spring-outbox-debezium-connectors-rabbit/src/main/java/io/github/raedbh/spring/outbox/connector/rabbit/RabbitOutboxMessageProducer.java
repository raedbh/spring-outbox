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

package io.github.raedbh.spring.outbox.connector.rabbit;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.core.env.Environment;

import io.github.raedbh.spring.outbox.connector.core.OutboxData;
import io.github.raedbh.spring.outbox.connector.core.OutboxMessageProducer;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class RabbitOutboxMessageProducer implements OutboxMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitOutboxMessageProducer.class);

    private final RabbitOperations rabbitOperations;
    private final RabbitProducerConfigProvider configProvider;


    public RabbitOutboxMessageProducer(RabbitOperations rabbitOperations, Environment environment) {
        this.rabbitOperations = rabbitOperations;
        this.configProvider = new RabbitProducerConfigProvider(environment);
    }


    @Override
    public void produceMessage(OutboxData outboxData) throws Exception {

        RabbitProducerConfig config = configProvider.getConfig(outboxData.getType());
        if (config.routingKey() == null) {
            LOGGER.warn("No key found for {}", outboxData.getType());
            LOGGER.info("To use a specific routing key, make sure the config follows: "
              + "spring.outbox.connector.rabbit.messages.order-placed.routing-key for type=OrderPlaced");
        }

        if (config.exchange() == null) {
            LOGGER.warn("No exchange found for {}", outboxData.getType());
        }

        MessageProperties messageProperties = new MessageProperties();
        Map<String, Object> metadata = outboxData.getMetadata();
        if (isNotNullOrEmptyMap(metadata)) {
            messageProperties.setHeaders(metadata);
        }
        Message message = new Message(outboxData.getPayload(), messageProperties);

        rabbitOperations.send(config.exchange(), config.routingKey(), message);
        LOGGER.info("Message sent to exchange '{}' with routing key '{}'.", config.exchange(), config.routingKey());
    }

    private boolean isNotNullOrEmptyMap(Map<String, Object> metadata) {
        return metadata != null && !metadata.isEmpty();
    }
}
