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

import org.springframework.core.env.Environment;

import io.github.raedbh.spring.outbox.connector.core.AbstractMessageConfigProvider;
import io.github.raedbh.spring.outbox.connector.core.OutboxMessageConfigKeyResolver;

/**
 * Provides configuration for Kafka integration based on outbox type.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class KafkaMessageConfigProvider extends AbstractMessageConfigProvider<KafkaMessageConfig> {

    private final Environment environment;

    KafkaMessageConfigProvider(Environment environment) {
        super("kafka");
        this.environment = environment;
    }

    protected KafkaMessageConfig loadConfig(String outboxType) {
        OutboxMessageConfigKeyResolver keyResolver = newKeyResolver(outboxType);

        String topic = environment.getProperty(keyResolver.resolve("topic"));
        Integer partition = environment.getProperty(keyResolver.resolve("partition"), Integer.class);
        String key = environment.getProperty(keyResolver.resolve("key"));

        return new KafkaMessageConfig(topic, partition, key);
    }
}
