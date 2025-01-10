/*
 *  Copyright 2024-2025 the original authors.
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

package io.github.raedbh.spring.outbox.connector.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaOperations;

import io.github.raedbh.spring.outbox.connector.core.OutboxData;
import io.github.raedbh.spring.outbox.connector.core.OutboxMessageProducer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An {@link OutboxMessageProducer} for sending outbox messages to Kafka topics.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class KafkaOutboxMessageProducer implements OutboxMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOutboxMessageProducer.class);

    private final KafkaOperations<String, byte[]> kafkaOperations;
    private final KafkaMessageConfigProvider configProvider;


    public KafkaOutboxMessageProducer(KafkaOperations<String, byte[]> kafkaOperations, Environment environment) {
        this.kafkaOperations = kafkaOperations;
        this.configProvider = new KafkaMessageConfigProvider(environment);
    }


    @Override
    public void produceMessage(OutboxData outboxData) {
        KafkaMessageConfig config = configProvider.getConfig(outboxData.getType());

        if (config.topic() == null) {
            LOGGER.warn("No topic configured for {}", outboxData.getType());
            LOGGER.info("To specify a topic, make sure the config follows: "
              + "spring.outbox.connector.kafka.messages.<kebab-case-type>.topic "
              + "e.g. spring.outbox.connector.kafka.messages.order-placed.topic for type=OrderPlaced");

            LOGGER.warn("Message delivery skipped for type: {}", outboxData.getType());
            return;
        }

        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
          config.topic(),
          config.partition(),
          config.key(),
          outboxData.getPayload());

        outboxData
          .getMetadata()
          .forEach((key, value) -> {
              if (value != null) {
                  producerRecord.headers().add(key, value.toString().getBytes(UTF_8));
              }
          });

        kafkaOperations.send(producerRecord)
          .whenComplete((sendResult, exception) -> {
              if (exception == null) {
                  LOGGER.info("Message successfully sent to topic '{}' with key '{}'. Offset: {}, Partition: {}",
                    config.topic(),
                    config.key(),
                    sendResult.getRecordMetadata().offset(),
                    sendResult.getRecordMetadata().partition());
              } else {
                  LOGGER.error("Failed to send message to topic '{}' with key '{}'. Cause: {}",
                    config.topic(),
                    config.key(),
                    exception.getMessage(),
                    exception);
              }
          });
    }
}
