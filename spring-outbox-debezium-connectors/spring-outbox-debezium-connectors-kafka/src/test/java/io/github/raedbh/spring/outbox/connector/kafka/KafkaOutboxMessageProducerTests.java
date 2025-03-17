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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import io.github.raedbh.spring.outbox.connector.OutboxData;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link KafkaOutboxMessageProducer}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class KafkaOutboxMessageProducerTests {

    @Mock KafkaTemplate<String, byte[]> kafkaTemplate;
    @Mock Environment env;

    @InjectMocks KafkaOutboxMessageProducer producer;

    OutboxData outboxData;

    @BeforeEach
    void setUp() {
        outboxData = new OutboxData("1a2b3c", "OrderPlaced", "TestPayload".getBytes(),
          Map.of("key1", "value1", "key2", "value2"));
    }

    @Test
    void messageProduced() {

        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.topic"))
          .willReturn("topic");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.key"))
          .willReturn("key");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.partition", Integer.class))
          .willReturn(0);
        givenSuccessfulKafkaTemplateSend();

        producer.produceMessage(outboxData);

        ArgumentCaptor<ProducerRecord<String, byte[]>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, byte[]> capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.topic()).isEqualTo("topic");
        assertThat(capturedRecord.key()).isEqualTo("key");
        assertThat(capturedRecord.partition()).isZero();
        assertThat(capturedRecord.value()).isEqualTo(outboxData.getPayload());
    }

    @Test
    void produceMessageDespiteMissingKey() {

        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.topic"))
          .willReturn("topic");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.key"))
          .willReturn(null);
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.partition", Integer.class))
          .willReturn(0);
        givenSuccessfulKafkaTemplateSend();

        producer.produceMessage(outboxData);

        ArgumentCaptor<ProducerRecord<String, byte[]>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, byte[]> capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.topic()).isEqualTo("topic");
        assertThat(capturedRecord.key()).isNull();
        assertThat(capturedRecord.partition()).isZero();
        assertThat(capturedRecord.value()).isEqualTo(outboxData.getPayload());
    }

    @Test
    void produceMessageDespiteMissingPartition() {

        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.topic"))
          .willReturn("topic");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.key"))
          .willReturn("key");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.partition", Integer.class))
          .willReturn(null);
        givenSuccessfulKafkaTemplateSend();

        producer.produceMessage(outboxData);

        ArgumentCaptor<ProducerRecord<String, byte[]>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, byte[]> capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.topic()).isEqualTo("topic");
        assertThat(capturedRecord.key()).isEqualTo("key");
        assertThat(capturedRecord.partition()).isNull();
        assertThat(capturedRecord.value()).isEqualTo(outboxData.getPayload());
    }

    @Test
    void messageSentWithMetadataAsHeaders() {

        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.topic"))
          .willReturn("topic");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.key"))
          .willReturn("key");
        given(env.getProperty("spring.outbox.connector.kafka.messages.order-placed.partition", Integer.class))
          .willReturn(0);
        givenSuccessfulKafkaTemplateSend();

        producer.produceMessage(outboxData); // contains metadata

        ArgumentCaptor<ProducerRecord<String, byte[]>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, byte[]> capturedRecord = recordCaptor.getValue();

        assertThat(capturedRecord.headers().toArray()).hasSize(3);
        Map.of("key1", "value1",
            "key2", "value2",
            "outbox_id", "1a2b3c")
          .forEach((key, value) -> {
              Header header = capturedRecord.headers().lastHeader(key);
              assertThat(header).isNotNull();
              assertThat(new String(header.value(), UTF_8)).isEqualTo(value);
          });
    }

    private void givenSuccessfulKafkaTemplateSend() {

        RecordMetadata metadata = new RecordMetadata(
          new TopicPartition("topic", 0),
          123L,
          0,
          System.currentTimeMillis(),
          10,
          20
        );

        SendResult<String, byte[]> sendResult = mock(SendResult.class);
        given(sendResult.getRecordMetadata()).willReturn(metadata);

        CompletableFuture<SendResult<String, byte[]>> future = new CompletableFuture<>();

        // Simulate successful send
        future.complete(sendResult);
        given(kafkaTemplate.send(any(ProducerRecord.class))).willReturn(future);
    }
}

