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

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;

import io.github.raedbh.spring.outbox.connector.core.OutboxData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link RabbitOutboxMessageProducer}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class RabbitOutboxMessageProducerTests {

		@Mock RabbitTemplate rabbitTemplate;
		@Mock Environment env;

		@InjectMocks RabbitOutboxMessageProducer producer;

		OutboxData outboxData;

		@BeforeEach
		void setUp() {
				outboxData = new OutboxData("1a2b3c", "OrderPlaced", "TestPayload".getBytes(),
						null, Map.of("key1", "value1", "key2", "value2"));
		}

		@Test
		void messageProduced() throws Exception {

				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.exchange")).willReturn("ex");
				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.routing-key")).willReturn("rk");

				producer.produceMessage(outboxData);

				verify(rabbitTemplate).send(eq("ex"), eq("rk"), any(Message.class));
		}

		@Test
		void produceMessageDespiteMissingExchange() throws Exception {

				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.exchange")).willReturn(null);
				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.routing-key")).willReturn("rk");

				producer.produceMessage(outboxData);

				verify(rabbitTemplate).send(eq(null), eq("rk"), any(Message.class));
		}

		@Test
		void produceMessageDespiteMissingRoutingKey() throws Exception {

				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.exchange")).willReturn("ex");
				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.routing-key")).willReturn(null);

				producer.produceMessage(outboxData);

				verify(rabbitTemplate).send(eq("ex"), eq(null), any(Message.class));
		}

		@Test
		void messageSentWithMetadataAsHeaders() throws Exception {

				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.exchange")).willReturn("ex");
				given(env.getProperty("spring.outbox.connector.rabbit.messages.order-placed.routing-key")).willReturn("rk");

				producer.produceMessage(outboxData); // contains metadata

				ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
				verify(rabbitTemplate).send(eq("ex"), eq("rk"), messageCaptor.capture());

				Message capturedMessage = messageCaptor.getValue();
				assertThat(capturedMessage.getMessageProperties().getHeaders()).isEqualTo(outboxData.getMetadata());
		}
}
