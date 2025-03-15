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

package io.github.raedbh.spring.outbox.connector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link OutboxMessageConfigKeyResolver}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxMessageConfigKeyResolverTests {

    @Test
    void resolveKey() {

        String configPrefix = "spring.outbox.connector.kafka.messages";
        String outboxType = "EmailNotification";
        String configName = "topic";

        OutboxMessageConfigKeyResolver keyResolver = new OutboxMessageConfigKeyResolver(configPrefix, outboxType);

        String generatedKey = keyResolver.resolve(configName);

        assertThat(generatedKey)
          .isEqualTo("spring.outbox.connector.kafka.messages.email-notification.topic");
    }

    @Test
    void rejectNullConfigPrefix() {

        assertThatThrownBy(() -> new OutboxMessageConfigKeyResolver(null, "EmailNotification"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("configPrefix must not be null");
    }

    @Test
    void rejectNullOutboxType() {

        assertThatThrownBy(() -> new OutboxMessageConfigKeyResolver("spring.outbox.connector.kafka.messages", null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("outboxType must not be null");
    }
}
