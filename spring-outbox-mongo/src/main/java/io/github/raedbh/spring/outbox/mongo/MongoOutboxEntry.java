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

package io.github.raedbh.spring.outbox.mongo;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

/**
 * A MongoDB document representing an outbox entry.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@Document(collection = "outbox")
final class MongoOutboxEntry implements Serializable {

    @Id final UUID id;

    final String type;
    final byte[] payload;

    @Nullable
    private final Map<String, String> metadata;


    MongoOutboxEntry(UUID id, String type, byte[] payload, @Nullable Map<String, String> metadata) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.metadata = metadata;
    }
}
