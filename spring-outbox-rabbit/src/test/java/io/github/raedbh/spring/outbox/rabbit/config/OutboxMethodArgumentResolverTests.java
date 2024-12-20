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

import java.io.Serializable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.serializer.Deserializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import io.github.raedbh.spring.outbox.core.MessageWith;
import io.github.raedbh.spring.outbox.core.OutboxDefaultDeserializer;
import io.github.raedbh.spring.outbox.core.OutboxDefaultSerializer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OutboxMethodArgumentResolver}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxMethodArgumentResolverTests {

		private static final String OPERATION_HEADER = "operation";
		private static final String TEST_OPERATION = "TEST_OPERATION";

		private Deserializer<Serializable> deserializer;
		private OutboxMethodArgumentResolver resolver;

		@BeforeEach
		void setUp() {
				deserializer = new OutboxDefaultDeserializer();
				resolver = new OutboxMethodArgumentResolver(deserializer);
		}

		@Test
		void supportParameter() throws NoSuchMethodException {

				MethodParameter paramNotAnnotated = new MethodParameter(
						TestHandler.class.getMethod("handleWithoutAnnotation", String.class), 0);

				assertThat(resolver.supportsParameter(paramAnnotated())).isTrue();
				assertThat(resolver.supportsParameter(paramNotAnnotated)).isFalse();
		}

		@Test
		void resolvesArgumentWhenOperationMatches() throws Exception {

				Message<byte[]> message = MessageBuilder
						.withPayload(new OutboxDefaultSerializer().serializeToByteArray("The Payload"))
						.setHeader(OPERATION_HEADER, TEST_OPERATION)
						.build();

				Object result = resolver.resolveArgument(paramAnnotated(), message);

				assertThat(result).isEqualTo("The Payload");
		}

		@Test
		void returnsNullWhenOperationDoesNotMatch() throws Exception {

				Message<byte[]> message = MessageBuilder.withPayload(new byte[]{1, 2, 3})
						.setHeader(OPERATION_HEADER, "WRONG_OPERATION")
						.build();

				Object result = resolver.resolveArgument(paramAnnotated(), message);

				assertThat(result).isNull();
		}

		private MethodParameter paramAnnotated() throws NoSuchMethodException {
				return new MethodParameter(TestHandler.class.getMethod("handleWithAnnotation", Serializable.class), 0);
		}

		static class TestHandler {

				public void handleWithAnnotation(@MessageWith(operation = TEST_OPERATION) Serializable entity) {}

				public void handleWithoutAnnotation(String message) {}
		}
}
