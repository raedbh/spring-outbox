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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.acme.eshop.Order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RootEntity}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class RootEntityTests {

		private RootEntity order;

		@BeforeEach
		void setUp() {
				order = new Order();
		}

		@Test
		void assignEventToRootEntity() {

				assertThat(order.withNoEventAssigned()).isTrue();

				EventOutboxed<RootEntity> event = event(order, "op");

				order.assignEvent(event);

				assertThat(order.withNoEventAssigned()).isFalse();
				assertThat(order.event()).isEqualTo(event);
		}

		@Test
		void rejectNullEvent() {
				assertThatThrownBy(() -> order.assignEvent(null))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("Event");
		}

		@Test
		void rejectAssigningEventTwice() {

				order.assignEvent(event(order, "op1"));

				assertThatThrownBy(() -> order.assignEvent(event(order, "op2")))
						.isInstanceOf(IllegalStateException.class)
						.hasMessage("An event has already been assigned for this entity");
		}

		private EventOutboxed<RootEntity> event(RootEntity rootEntity, String operation) {
				return new EventOutboxed<>(rootEntity) {
						@Override
						public String getOperation() {
								return operation;
						}
				};
		}
}
