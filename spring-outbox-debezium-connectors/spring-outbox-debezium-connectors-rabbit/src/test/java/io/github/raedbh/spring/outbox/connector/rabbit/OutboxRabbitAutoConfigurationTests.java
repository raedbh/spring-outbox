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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OutboxRabbitAutoConfiguration}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxRabbitAutoConfigurationTests {

    // TODO
//    @Test
//    void autoConfigure() {
//        new ApplicationContextRunner()
//          .withConfiguration(AutoConfigurations.of(OutboxRabbitAutoConfiguration.class))
//          .withPropertyValues("camel.component.uri=direct:camel")
//          .withBean(RabbitTemplate.class, () -> Mockito.mock(RabbitTemplate.class)).run(context -> {
//
//              assertThat(context).hasSingleBean(RabbitOutboxMessageProducer.class);
//              assertThat(context).hasSingleBean(DebeziumOutboxRouteBuilder.class);
//          });
//    }
}
