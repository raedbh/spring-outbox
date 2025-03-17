/*
 *  Copyright 2024-2025 the original authors.
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

package io.github.raedbh.spring.outbox.connector;

import java.util.Map;

/**
 * The data model for transmitting outbox data, representing either an event or a command.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public final class OutboxData {

    public static final String OUTBOX_ID = "outbox_id";

    private final String id;
    private final String type;
    private final byte[] payload;

    private Map<String, Object> metadata;


    public OutboxData(String id, String type, byte[] payload) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be null or empty");
        }
        if (payload == null || payload.length == 0) {
            throw new IllegalArgumentException("payload must not be null or empty");
        }

        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    public OutboxData(String id, String type, byte[] payload, Map<String, Object> metadata) {
        this(id, type, payload);

        if (metadata == null) {
            throw new IllegalArgumentException("metadata must not be empty");
        }
        this.metadata = metadata;
    }


    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
