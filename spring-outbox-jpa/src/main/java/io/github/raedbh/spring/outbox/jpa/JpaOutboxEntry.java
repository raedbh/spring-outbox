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

package io.github.raedbh.spring.outbox.jpa;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.lang.Nullable;

/**
 * A JPA entity representing an outbox entry, designed for persistence in a relational database.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@Entity
@Table(name = "outbox")
final class JpaOutboxEntry implements Serializable {

    @Id
    @Column(length = 16) final UUID id;

    final String type;

    final byte[] payload;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON) private final Map<String, String> metadata;


    JpaOutboxEntry(UUID id, String type, byte[] payload, @Nullable Map<String, String> metadata) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.metadata = metadata;
    }

    JpaOutboxEntry() {
        this.id = null;
        this.type = null;
        this.payload = null;
        this.metadata = null;
    }
}
