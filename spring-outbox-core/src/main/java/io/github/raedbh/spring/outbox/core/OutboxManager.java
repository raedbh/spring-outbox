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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.serializer.Serializer;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.EVENT_ENTITY_ID;
import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.EVENT_ENTITY_TYPE;
import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.EVENT_OCCURRED_AT;
import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.OPERATION;

/**
 * Manages outbox entries for domain events and post-event publication commands.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxManager.class);

    private final OutboxRepository outboxRepository;
    private final Serializer<Serializable> outboxSerializer;
    private final TransactionTemplate transactionTemplate;
    private final PlatformTransactionManager transactionManager;
    private final SerializableTargetConverterRegistry converterRegistry;


    public OutboxManager(OutboxRepository outboxRepository, Serializer<Serializable> outboxSerializer,
      PlatformTransactionManager transactionManager, SerializableTargetConverterRegistry converterRegistry) {

        this.outboxRepository = outboxRepository;
        this.outboxSerializer = outboxSerializer;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionManager = transactionManager;
        this.converterRegistry = converterRegistry;
    }


    @Nullable
    public Object proceedInvocationAndSaveOutboxEntries(RootEntity rootEntity, Supplier<Object> proceed) {
        List<OutboxEntry> entries = outboxEntriesFor(rootEntity);
        return transactionTemplate.execute(status -> {

            Object result = proceed.get();

            LOGGER.info("Saving outbox entries..");

            entries.forEach(outboxRepository::save);

            return result;
        });
    }

    private List<OutboxEntry> outboxEntriesFor(RootEntity rootEntity) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

        TransactionStatus status = transactionManager.getTransaction(def); // suspend current transaction

        List<OutboxEntry> entries = new ArrayList<>();

        try {
            LOGGER.info("Building outbox entries for {}", rootEntity.getClass());

            OutboxEntry eventOutboxEntry = outboxEntryFor(rootEntity);
            entries.add(eventOutboxEntry);

            List<CommandOutboxed> commands = rootEntity.event().getCommands();

            if (commands.isEmpty()) {
                return entries;
            }

            for (CommandOutboxed command : commands) {

                byte[] commandMessagePayload = convertAndSerialize(command);

                OutboxEntry commandOutboxEntry = new OutboxEntry(command.getName(), commandMessagePayload);
                commandOutboxEntry.setRelatedTo(eventOutboxEntry.getId());

                entries.add(commandOutboxEntry);
            }

            return entries;
        } finally {
            transactionManager.commit(status);
        }
    }

    private OutboxEntry outboxEntryFor(RootEntity rootEntity) {
        EventOutboxed<? extends RootEntity> event = rootEntity.event();
        byte[] outboxPayload = convertAndSerialize(rootEntity);

        Map<String, String> metadata = Map.of(
          EVENT_ENTITY_TYPE, rootEntity.getClass().getSimpleName(),
          EVENT_ENTITY_ID, rootEntity.getId().toString(),
          EVENT_OCCURRED_AT, String.valueOf(event.getOccurredAt()),
          OPERATION, event.getOperation());

        return new OutboxEntry(event.getName(), outboxPayload, metadata);
    }

    private byte[] convertAndSerialize(Object object) {
        var target = converterRegistry.getConverter(object.getClass()).<Object>map(objectSerializableConverter ->
          objectSerializableConverter.convert(object)).orElse(object);
        try {
            return outboxSerializer.serializeToByteArray((Serializable) target);
        } catch (IOException e) {
            LOGGER.error("Serialization failed", e);
            throw new RuntimeException(e);
        }
    }
}
