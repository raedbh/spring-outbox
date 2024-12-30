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

import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import io.github.raedbh.spring.outbox.connector.core.DebeziumRabbitRouteBuilder;
import io.github.raedbh.spring.outbox.connector.core.OutboxMessageProducer;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@AutoConfiguration
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@ConditionalOnClass(RabbitTemplate.class)
public class OutboxRabbitAutoConfiguration {

    @Bean
    OutboxMessageProducer outboxMessageProducer(RabbitOperations rabbitOperations, Environment environment) {
        return new RabbitOutboxMessageProducer(rabbitOperations, environment);
    }

    @Bean
    DebeziumRabbitRouteBuilder debeziumRabbitRouteBuilder(OutboxMessageProducer outboxMessageProducer,
      @Value("${camel.component.uri}") String camelComponentUri) {

        return new DebeziumRabbitRouteBuilder(outboxMessageProducer, camelComponentUri);
    }
}
