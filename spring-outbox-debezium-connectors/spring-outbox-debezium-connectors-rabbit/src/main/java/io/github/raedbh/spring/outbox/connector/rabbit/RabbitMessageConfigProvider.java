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

package io.github.raedbh.spring.outbox.connector.rabbit;

import org.springframework.core.env.Environment;

import io.github.raedbh.spring.outbox.connector.AbstractMessageConfigProvider;
import io.github.raedbh.spring.outbox.connector.OutboxMessageConfigKeyResolver;

/**
 * Provides configuration for RabbitMQ integration based on outbox type.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class RabbitMessageConfigProvider extends AbstractMessageConfigProvider<RabbitMessageConfig> {

    private final Environment environment;

    RabbitMessageConfigProvider(Environment environment) {
        super("rabbit");
        this.environment = environment;
    }

    @Override
    protected RabbitMessageConfig loadConfig(String outboxType) {
        OutboxMessageConfigKeyResolver keyResolver = newKeyResolver(outboxType);

        String routingKey = environment.getProperty(keyResolver.resolve("routing-key"));
        String exchange = environment.getProperty(keyResolver.resolve("exchange"));

        return new RabbitMessageConfig(routingKey, exchange);
    }
}
