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

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A base model for storing events or commands for later processing.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxEntry {

    private final String type;
    private final byte[] payload;

    @Nullable
    private Map<String, String> metadata;


    /**
     * Creates an {@link OutboxEntry} for a given {@link RootEntity}.
     *
     * @param type the name of the event class; must not be {@code null}.
     * @param payload serialized data typically obtained after converting a {@link RootEntity}
     * 	              into transmission format; must not be {@code null}.
     * @param metadata metadata a key-value list contains useful metadata such as the root Entity type, ID, etc.
     */
    public OutboxEntry(String type, byte[] payload, Map<String, String> metadata) {
        Assert.notNull(type, "Type must not be null");
        Assert.notNull(payload, "Payload must not be null");

        this.type = type;
        this.payload = payload;
        this.metadata = metadata;
    }

    /**
     * Creates an {@link OutboxEntry} for a given {@link RootEntity}.
     *
     * @param type the name of the event class; must not be {@code null}.
     * @param payload serialized data typically obtained after converting a {@link RootEntity}
     *                into transmission format; must not be {@code null}.
     */
    public OutboxEntry(String type, byte[] payload) {
        this(type, payload, null);
    }


    public String getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }

}
