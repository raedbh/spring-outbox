/*
 *  Copyright 2025 the original authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link KafkaMessageConfigProvider}.
 *
 * @author Raed
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class KafkaMessageConfigProviderTests {

    KafkaMessageConfigProvider configProvider;

    @Mock Environment environment;

    @BeforeEach
    void setUp() {
        configProvider = new KafkaMessageConfigProvider(environment);
    }

    @Test
    void loadConfig() {

        String topic = "topic";
        Integer partition = 1;
        String key = "key";

        given(environment.getProperty("spring.outbox.connector.kafka.messages.order-created.topic"))
          .willReturn(topic);
        given(environment.getProperty("spring.outbox.connector.kafka.messages.order-created.partition", Integer.class))
          .willReturn(partition);
        given(environment.getProperty("spring.outbox.connector.kafka.messages.order-created.key"))
          .willReturn(key);

        KafkaMessageConfig kafkaMessageConfig = configProvider.loadConfig("OrderCreated");

        assertThat(kafkaMessageConfig).isNotNull();
        assertThat(kafkaMessageConfig.topic()).isEqualTo(topic);
        assertThat(kafkaMessageConfig.partition()).isEqualTo(partition);
        assertThat(kafkaMessageConfig.key()).isEqualTo(key);
    }

    @Test
    void returnNullableValuesForMissingConfig() {

        given(environment.getProperty(any())).willReturn(null);

        KafkaMessageConfig messageConfig = configProvider.loadConfig("OrderCancelled");

        assertThat(messageConfig).isNotNull();
        assertThat(messageConfig.topic()).isNull();
        assertThat(messageConfig.partition()).isNull();
        assertThat(messageConfig.key()).isNull();
    }
}
