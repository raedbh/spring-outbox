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

package io.github.raedbh.spring.outbox.core;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link OutboxEntry}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxEntryTests {

		@Test
		void rejectNullId() {
				assertThatExceptionOfType(IllegalArgumentException.class)
						.isThrownBy(() -> new OutboxEntry(null, "SmsNotification", new byte[]{}, null, Map.of()))
						.withMessageContaining("Identifier");
		}

		@Test
		void rejectNullType() {
				assertThatExceptionOfType(IllegalArgumentException.class)
						.isThrownBy(() -> new OutboxEntry(new OutboxEntry.Identifier(UUID.randomUUID()),
								null, new byte[]{}, null, Map.of()))
						.withMessageContaining("Type");
		}

		@Test
		void rejectNullPayload() {
				assertThatExceptionOfType(IllegalArgumentException.class)
						.isThrownBy(() -> new OutboxEntry(new OutboxEntry.Identifier(UUID.randomUUID()),
								"SmsNotification", null, null, Map.of()))
						.withMessageContaining("Payload");
		}
}
