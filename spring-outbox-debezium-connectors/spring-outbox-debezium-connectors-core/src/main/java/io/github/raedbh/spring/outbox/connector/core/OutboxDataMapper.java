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

package io.github.raedbh.spring.outbox.connector.core;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.connect.data.Struct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Maps {@link Struct} to {@link OutboxData}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
final class OutboxDataMapper {

    private OutboxDataMapper() {
        /* prevent instantiation */
    }

    public static OutboxData toOutboxData(Struct struct) {

        Objects.requireNonNull(struct, "Struct must not be null");

        String id = idFromStruct(struct, "id");
        String type = typeFromStruct(struct);
        byte[] payload = payloadFromStruct(struct);
        String relatedTo = idFromStruct(struct, "related_to");
        Map<String, Object> metadata = metadataFromStruct(struct);

        return new OutboxData(id, type, payload, relatedTo, metadata);
    }

    private static String idFromStruct(Struct struct, String fieldName) {
        assertFieldExists(struct, fieldName);
        Object id = struct.get(fieldName);
        if (id == null) {
            return null;
        }

        return id.toString();
    }

    private static String typeFromStruct(Struct struct) {
        String fieldName = "type";
        assertFieldExists(struct, fieldName);
        return struct.getString(fieldName);
    }

    private static byte[] payloadFromStruct(Struct struct) {
        String fieldName = "payload";
        assertFieldExists(struct, fieldName);
        return struct.getBytes(fieldName);
    }

    private static void assertFieldExists(Struct struct, String fieldName) {
        if (struct.schema().field(fieldName) == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' does not exist in the struct.");
        }
    }

    private static Map<String, Object> metadataFromStruct(Struct struct) {
        assertFieldExists(struct, "metadata");

        String metadata = struct.getString("metadata");
        if (metadata == null) {
            return Collections.emptyMap();
        }
        try {
            return JacksonMapperProvider.getInstance().readValue(metadata, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
