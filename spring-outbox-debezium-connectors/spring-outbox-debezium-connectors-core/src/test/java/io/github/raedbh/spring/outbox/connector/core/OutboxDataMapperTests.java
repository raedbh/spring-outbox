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

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OutboxDataMapper}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxDataMapperTests {

    private OutboxData outboxData;

    @Test
    void fromStructToOutboxData() {

        Struct struct = struct();
        struct.put("metadata", "{\"key\": \"value\"}");

        outboxData = OutboxDataMapper.toOutboxData(struct);

        assertThat(
          new Object[]{outboxData.getId(), outboxData.getType(), outboxData.getPayload(), outboxData.getMetadata()})
          .containsExactly("12345", "Type", "The Payload".getBytes(), singletonMap("key", "value"));
    }

    @Test
    void preventNPEWhenMetadataIsNull() {

        Struct struct = struct();
        struct.put("metadata", null);

        outboxData = OutboxDataMapper.toOutboxData(struct);

        assertThat(outboxData.getMetadata()).isEmpty();
    }

    private Struct struct() {
        var struct = new Struct(schema());
        struct.put("id", "12345");
        struct.put("type", "Type");
        struct.put("payload", "The Payload".getBytes());
        return struct;
    }

    private Schema schema() {
        return SchemaBuilder.struct().name("test")
          .field("id", Schema.STRING_SCHEMA)
          .field("type", Schema.STRING_SCHEMA)
          .field("payload", Schema.BYTES_SCHEMA)
          .field("related_to", Schema.OPTIONAL_STRING_SCHEMA)
          .field("metadata", Schema.OPTIONAL_STRING_SCHEMA)
          .build();
    }
}
