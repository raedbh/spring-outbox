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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope.FieldName;
import io.debezium.data.Envelope.Operation;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import io.github.raedbh.spring.outbox.connector.OutboxData;
import io.github.raedbh.spring.outbox.connector.OutboxDataMapper;
import io.github.raedbh.spring.outbox.connector.OutboxMessageProducer;

/**
 * A Debezium engine that captures database changes and produces outbox messages.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxDebeziumEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxDebeziumEngine.class);

    private final ExecutorService executorService;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private final OutboxMessageProducer messageProducer;

    public OutboxDebeziumEngine(Configuration configuration, OutboxMessageProducer messageProducer) {

        this.executorService = Executors.newSingleThreadExecutor();
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
          .using(configuration.asProperties())
          .notifying(this::onRecordChanged)
          .build();
        this.messageProducer = messageProducer;
    }

    private void onRecordChanged(RecordChangeEvent<SourceRecord> changeEvent) {

        SourceRecord changeEventRecord = changeEvent.record();

        Struct struct = (Struct) changeEventRecord.value();
        if (struct == null) {
            throw new IllegalArgumentException("Struct cannot be null for the change event.");
        }

        Operation operation = extractOperation(struct);
        if (operation == null) {
            LOGGER.warn("Operation field not found in struct: {}, skipping...", struct);
            return;
        }

        if (operation == Operation.READ || operation == Operation.CREATE) {

            LOGGER.info("Processing change event [operation: {}] [struct: {}]", operation, struct);

            Object recordData = struct.get(FieldName.AFTER);
            if (recordData == null) {
                LOGGER.warn("Missing 'after' field for operation: {}, skipping...", operation);
                return;
            }

            OutboxData outboxData = OutboxDataMapper.toOutboxData(recordData);
            messageProducer.produceMessage(outboxData);

        } else {
            LOGGER.info("{} operation detected. No action required. Key: {} | Struct: {}",
              operation, changeEventRecord.key(), struct);
        }
    }

    private Operation extractOperation(Struct struct) {

        if (struct.schema().field(FieldName.OPERATION) == null) {
            return null;
        }

        String code = struct.getString(FieldName.OPERATION);
        return Operation.forCode(code);
    }

    @PostConstruct
    private void start() {
        executorService.submit(() -> {
            try {
                debeziumEngine.run();
            } catch (Exception e) {
                LOGGER.error("Debezium Engine failed: {}", e.getMessage(), e);
            }
        });
    }

    @PreDestroy
    private void stop() throws IOException {

        LOGGER.info("Stopping Debezium Engine and executor service...");

        // Close the Debezium engine
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }

        shutdownGracefully();

        LOGGER.info("Debezium Engine and Executor Service stopped successfully.");
    }

    private void shutdownGracefully() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.warn("Executor shutdown timeout, forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted during executor shutdown, forcing immediate shutdown...", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
