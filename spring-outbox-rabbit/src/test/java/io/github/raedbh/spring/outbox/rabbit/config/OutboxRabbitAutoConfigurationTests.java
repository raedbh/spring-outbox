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

package io.github.raedbh.spring.outbox.rabbit.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.core.serializer.Deserializer;

import io.github.raedbh.spring.outbox.core.OutboxMethodArgumentResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link OutboxRabbitAutoConfiguration}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxRabbitAutoConfigurationTests {

    @Test
    void registerOutboxMethodArgumentResolver() {

        OutboxRabbitAutoConfiguration configuration = new OutboxRabbitAutoConfiguration(mock(Deserializer.class));
        RabbitListenerEndpointRegistrar registrar = new RabbitListenerEndpointRegistrar();

        configuration.configureRabbitListeners(registrar);

        assertThat(registrar.getCustomMethodArgumentResolvers())
          .hasSize(1)
          .allSatisfy(resolver -> assertThat(resolver).isInstanceOf(OutboxMethodArgumentResolver.class));
    }
}
