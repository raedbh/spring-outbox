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

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A base model for storing events or commands for later processing.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxEntry {

    private final Identifier id;
    private final String type;
    private final byte[] payload;

    @Nullable
    private Identifier relatedTo;

    @Nullable
    private Map<String, String> metadata;


    /**
     * Creates an {@link OutboxEntry}.
     *
     * @param id the identifier of the entry; must not be {@code null}.
     * @param type the name of the event or command class, e.g. OrderCreated, EmailNotification, etc.;
     *             must not be {@code null}.
     * @param payload serialized data typically obtained after converting a {@link RootEntity} or
     *                {@link CommandOutboxed} into a suitable format for storage and transmission;
     *                must not be {@code null}.
     * @param relatedTo if the record represents an event, this is set to {@code null}.
     *                  For {@link CommandOutboxed}s related to this event, it is set to the outbox id of the event.
     * @param metadata a key-value list contains useful metadata such as the root Entity type, ID, etc.
     */
    public OutboxEntry(Identifier id, String type, byte[] payload,
      @Nullable Identifier relatedTo, @Nullable Map<String, String> metadata) {

        Assert.notNull(id, "Identifier must not be null");
        Assert.notNull(type, "Type must not be null");
        Assert.notNull(payload, "Payload must not be null");

        this.id = id;
        this.type = type;
        this.payload = payload;

        this.relatedTo = relatedTo;
        this.metadata = metadata;
    }

    /**
     * Creates an {@link OutboxEntry} for a given {@link RootEntity}.
     *
     * @param type the name of the event class; must not be {@code null}.
     * @param payload serialized data typically obtained after converting a {@link RootEntity}
     * 	              into transmission format; must not be {@code null}.
     * @param metadata metadata a key-value list contains useful metadata such as the root Entity type, ID, etc.
     */
    public OutboxEntry(String type, byte[] payload, Map<String, String> metadata) {
        this(new Identifier(UUID.randomUUID()), type, payload, null, metadata);
    }

    /**
     * Creates an {@link OutboxEntry} for a given {@link RootEntity}.
     *
     * @param type the name of the event class; must not be {@code null}.
     * @param payload serialized data typically obtained after converting a {@link RootEntity}
     *                into transmission format; must not be {@code null}.
     */
    public OutboxEntry(String type, byte[] payload) {
        this(new Identifier(UUID.randomUUID()), type, payload, null, null);
    }


    public Identifier getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Nullable
    public Identifier getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(Identifier relatedTo) {
        this.relatedTo = relatedTo;
    }

    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public record Identifier(UUID id) {

        public UUID value() {
            return id;
        }
    }
}
