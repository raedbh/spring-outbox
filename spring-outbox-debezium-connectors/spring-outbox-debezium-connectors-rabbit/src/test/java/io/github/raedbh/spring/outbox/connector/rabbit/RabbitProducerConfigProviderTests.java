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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link RabbitProducerConfigProvider}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class RabbitProducerConfigProviderTests {

    RabbitProducerConfigProvider configProvider;
    @Mock Environment environment;

    @BeforeEach
    void setUp() {
        configProvider = new RabbitProducerConfigProvider(environment);
    }

    @Test
    void retrieveRabbitProducerConfigForOutboxData() {

        String routingKey = "order-routing-key";
        String exchange = "order-exchange";

        given(environment.getProperty("spring.outbox.connector.rabbit.messages.order-created.routing-key"))
          .willReturn(routingKey);
        given(environment.getProperty("spring.outbox.connector.rabbit.messages.order-created.exchange"))
          .willReturn(exchange);

        RabbitProducerConfig rabbitProducerConfig = configProvider.getConfig("OrderCreated");

        assertThat(rabbitProducerConfig).isNotNull();
        assertThat(rabbitProducerConfig.routingKey()).isEqualTo(routingKey);
        assertThat(rabbitProducerConfig.exchange()).isEqualTo(exchange);
    }

    @Test
    void returnNullForMissingConfig() {

        given(environment.getProperty(anyString())).willReturn(null);

        RabbitProducerConfig rabbitProducerConfig = configProvider.getConfig("NonExistingType");

        assertThat(rabbitProducerConfig).isNotNull();
        assertThat(rabbitProducerConfig.routingKey()).isNull();
        assertThat(rabbitProducerConfig.exchange()).isNull();
    }
}
