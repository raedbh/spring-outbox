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

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.data.Struct;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Maps {@link Struct} and {@link BsonDocument} record data to {@link OutboxData}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public final class OutboxDataMapper {

    private OutboxDataMapper() {}

    public static OutboxData toOutboxData(Object recordData) {
        if (recordData == null) {
            throw new IllegalArgumentException("Struct must not be null");
        }

        OutboxData outboxData;
        if (recordData instanceof Struct structData) {
            outboxData = fromStruct(structData);
        } else {
            // try to parse as bson
            BsonDocument bsonData = BsonDocument.parse(String.valueOf(recordData));
            outboxData = fromBson(bsonData);
        }

        return outboxData;
    }

    private static OutboxData fromStruct(Struct recordData) {
        if (recordData == null) {
            throw new IllegalArgumentException("Struct must not be null");
        }

        String id = idFromStruct(recordData);
        String type = typeFromStruct(recordData);
        byte[] payload = payloadFromStruct(recordData);
        Map<String, Object> metadata = metadataFromStruct(recordData);

        return new OutboxData(id, type, payload, metadata);
    }

    public static OutboxData fromBson(BsonDocument recordData) {
        if (recordData == null) {
            throw new IllegalArgumentException("BsonDocument must not be null");
        }

        String id = idFromBsonDocument(recordData);
        String type = typeFromBsonDocument(recordData);
        byte[] payload = payloadFromBsonDocument(recordData);
        Map<String, Object> metadata = metadataFromBsonDocument(recordData);

        return new OutboxData(id, type, payload, metadata);
    }

    private static String idFromStruct(Struct struct) {
        String fieldName = "id";

        assertFieldExists(struct, fieldName);
        Object id = struct.get(fieldName);
        if (id == null) {
            return null;
        }

        return id.toString();
    }

    private static String idFromBsonDocument(BsonDocument bsonDocument) {
        BsonValue idValue = bsonDocument.get("_id");
        if (idValue == null || !idValue.isBinary()) {
            return null;
        }
        BsonBinary idBinary = idValue.asBinary();
        return Base64.getEncoder().encodeToString(idBinary.getData());
    }

    private static String typeFromStruct(Struct struct) {
        String fieldName = "type";

        assertFieldExists(struct, fieldName);
        return struct.getString(fieldName);
    }

    private static String typeFromBsonDocument(BsonDocument bsonDocument) {
        BsonValue typeValue = bsonDocument.get("type");
        if (typeValue == null) {
            return null;
        }
        return typeValue.asString().getValue();
    }

    private static byte[] payloadFromStruct(Struct struct) {
        String fieldName = "payload";
        assertFieldExists(struct, fieldName);
        return struct.getBytes(fieldName);
    }

    private static byte[] payloadFromBsonDocument(BsonDocument bsonDocument) {
        BsonValue payloadValue = bsonDocument.get("payload");
        if (payloadValue == null || !payloadValue.isBinary()) {
            return new byte[0];
        }
        return payloadValue.asBinary().getData();
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

    private static Map<String, Object> metadataFromBsonDocument(BsonDocument bsonDocument) {
        BsonValue metadataValue = bsonDocument.get("metadata");
        if (metadataValue == null || !metadataValue.isDocument()) {
            return Collections.emptyMap();
        }
        BsonDocument metadataDoc = metadataValue.asDocument();
        Map<String, Object> metadataMap = new HashMap<>();
        for (Map.Entry<String, BsonValue> entry : metadataDoc.entrySet()) {
            metadataMap.put(entry.getKey(), entry.getValue().asString().getValue());
        }
        return metadataMap;
    }

}
