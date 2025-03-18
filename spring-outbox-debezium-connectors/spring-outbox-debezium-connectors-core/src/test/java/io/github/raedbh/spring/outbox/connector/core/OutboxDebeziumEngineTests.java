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

package io.github.raedbh.spring.outbox.connector.core;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope.Operation;
import io.debezium.engine.RecordChangeEvent;
import io.github.raedbh.spring.outbox.connector.OutboxData;
import io.github.raedbh.spring.outbox.connector.OutboxMessageProducer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class OutboxDebeziumEngineTests {

    @Mock private OutboxMessageProducer messageProducer;
    @Mock private Configuration configuration;

    @InjectMocks private OutboxDebeziumEngine outboxDebeziumEngine;

    @ParameterizedTest
    @EnumSource(value = Operation.class, names = {"CREATE", "READ"})
    void produceMessageForReadAndCreateOperations(Operation operation) {

        outboxDebeziumEngine.onRecordChanged(() -> sourceRecord(operation));
        verify(messageProducer).produceMessage(any(OutboxData.class));
    }

    @Test
    void noMessageProducedForDelete() {
        outboxDebeziumEngine.onRecordChanged(() -> sourceRecord(Operation.DELETE));
        verify(messageProducer, times(0)).produceMessage(any());
    }

    @Test
    void skipSilentlyForNullStruct() {
        assertThatCode(() -> {
            SourceRecord sourceRecord = new SourceRecord(null, null, "dummy", null, null, null);
            outboxDebeziumEngine.onRecordChanged(() -> sourceRecord);
        }).doesNotThrowAnyException();
    }

    @Test
    void skipProcessingWhenOperationFieldIsMissing() {

        Struct struct = new Struct(SchemaBuilder.struct().build());
        SourceRecord sourceRecord = new SourceRecord(null, null, "dummy", null, null, struct);
        RecordChangeEvent<SourceRecord> event = () -> sourceRecord;

        outboxDebeziumEngine.onRecordChanged(event);

        verify(messageProducer, times(0)).produceMessage(any());
    }

    @Test
    void skipProcessingForUnsupportedOperations() {
        outboxDebeziumEngine.onRecordChanged(() -> sourceRecord(Operation.UPDATE));
        verify(messageProducer, times(0)).produceMessage(any());
    }

    @Test
    void skipProcessingWhenAfterFieldIsNull() {
        Schema valueSchema = SchemaBuilder.struct()
          .name("outbox.common.outbox.Envelope")
          .field("op", Schema.STRING_SCHEMA)
          .field("after", SchemaBuilder.struct().optional().build())
          .build();

        Struct struct = new Struct(valueSchema)
          .put("op", Operation.CREATE.code())
          .put("after", null);

        SourceRecord sourceRecord = new SourceRecord(
          Map.of("server", "outbox"),
          Map.of("lsn_proc", 27388008),
          "outbox.common.outbox",
          null,
          null,
          struct
        );

        outboxDebeziumEngine.onRecordChanged(() -> sourceRecord);

        verify(messageProducer, times(0)).produceMessage(any(OutboxData.class));
    }

    private SourceRecord sourceRecord(Operation operation) {

        Schema keySchema = SchemaBuilder.struct()
          .name("outbox.common.outbox.Key")
          .field("id", Schema.STRING_SCHEMA)
          .build();

        Schema sourceSchema = SchemaBuilder.struct()
          .name("source")
          .field("version", Schema.STRING_SCHEMA)
          .field("connector", Schema.STRING_SCHEMA)
          .field("name", Schema.STRING_SCHEMA)
          .field("ts_ms", Schema.INT64_SCHEMA)
          .field("db", Schema.STRING_SCHEMA)
          .field("schema", Schema.STRING_SCHEMA)
          .field("table", Schema.STRING_SCHEMA)
          .field("txId", Schema.INT32_SCHEMA)
          .field("lsn", Schema.INT64_SCHEMA)
          .field("ts_us", Schema.INT64_SCHEMA)
          .field("ts_ns", Schema.INT64_SCHEMA)
          .build();

        Schema afterSchema = SchemaBuilder.struct()
          .name("outbox.common.outbox.After")
          .field("id", Schema.STRING_SCHEMA)
          .field("type", Schema.STRING_SCHEMA)
          .field("payload", Schema.BYTES_SCHEMA)
          .field("metadata", Schema.STRING_SCHEMA)
          .build();

        Schema valueSchema = SchemaBuilder.struct()
          .name("outbox.common.outbox.Envelope")
          .field("after", afterSchema)
          .field("source", sourceSchema)
          .field("op", Schema.STRING_SCHEMA)
          .field("ts_ms", Schema.INT64_SCHEMA)
          .field("ts_us", Schema.INT64_SCHEMA)
          .field("ts_ns", Schema.INT64_SCHEMA)
          .build();

        Struct keyStruct = new Struct(keySchema)
          .put("id", "3466c7dd-5880-4f91-886b-822cba015d4d");

        Struct sourceStruct = new Struct(sourceSchema)
          .put("version", "2.7.4.Final")
          .put("connector", "postgresql")
          .put("name", "outbox")
          .put("ts_ms", 1742088376822L)
          .put("db", "data")
          .put("schema", "common")
          .put("table", "outbox")
          .put("txId", 731)
          .put("lsn", 27388008L)
          .put("ts_us", 1742088376822223L)
          .put("ts_ns", 1742088376822223000L);

        Struct afterStruct = new Struct(afterSchema)
          .put("id", "3466c7dd-5880-4f91-886b-822cba015d4d")
          .put("type", "OrderPaid")
          .put("payload", ByteBuffer.allocate(497))  // Simulated ByteBuffer payload
          .put("metadata",
            "{\"operation\": \"payment\","
              + " \"event_entity_id\": \"5a4f5a11-e367-46cc-945e-ef93f5c9b903\","
              + " \"event_entity_type\": \"Order\","
              + " \"event_occurred_at\": \"1742088376692\"}");

        Struct valueStruct = new Struct(valueSchema)
          .put("after", afterStruct)
          .put("source", sourceStruct)
          .put("op", operation.code())
          .put("ts_ms", 1742088377228L)
          .put("ts_us", 1742088377228275L)
          .put("ts_ns", 1742088377228275204L);

        return new SourceRecord(
          Map.of("server", "outbox"),
          Map.of("lsn_proc", 27388008,
            "messageType", "INSERT",
            "lsn_commit", 27387888,
            "lsn", 27388008,
            "txId", 731,
            "ts_usec", 1742088376822223L),
          "outbox.common.outbox",
          null,
          keySchema,
          keyStruct,
          valueSchema,
          valueStruct
        );
    }

}
