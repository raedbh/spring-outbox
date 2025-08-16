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

package io.github.raedbh.spring.outbox.kafka.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.converter.MessagingMessageConverter;

import static io.github.raedbh.spring.outbox.messaging.OutboxHeaders.EVENT_ENTITY_ID;
import static io.github.raedbh.spring.outbox.messaging.OutboxHeaders.EVENT_ENTITY_TYPE;
import static io.github.raedbh.spring.outbox.messaging.OutboxHeaders.EVENT_OCCURRED_AT;
import static io.github.raedbh.spring.outbox.messaging.OutboxHeaders.OPERATION;

/**
 * Auto-configuration for consuming outbox messages from Kafka.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@AutoConfiguration
@Import(OutboxKafkaListenerConfiguration.class)
public class OutboxKafkaAutoConfiguration {

    @Bean
    public MessagingMessageConverter outboxMessagingMessageConverter() {

        Map<String, Boolean> rawMappedHeaders = new HashMap<>();
        rawMappedHeaders.put(EVENT_ENTITY_TYPE, true);
        rawMappedHeaders.put(EVENT_ENTITY_ID, true);
        rawMappedHeaders.put(EVENT_OCCURRED_AT, true);
        rawMappedHeaders.put(OPERATION, true);

        DefaultKafkaHeaderMapper mapper = new DefaultKafkaHeaderMapper();
        mapper.setRawMappedHeaders(rawMappedHeaders);
        mapper.setEncodeStrings(true);

        MessagingMessageConverter converter = new MessagingMessageConverter();
        converter.setHeaderMapper(mapper);
        return converter;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> outboxContainerFactory(
      ConsumerFactory<Object, Object> consumerFactory,
      MessagingMessageConverter outboxMessagingMessageConverter) {

        Map<String, Object> configs = new HashMap<>(consumerFactory.getConfigurationProperties());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(Collections.unmodifiableMap(configs)));
        factory.setRecordMessageConverter(outboxMessagingMessageConverter);

        return factory;
    }
}
