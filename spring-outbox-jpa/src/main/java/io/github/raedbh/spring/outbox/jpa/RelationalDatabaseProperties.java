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

package io.github.raedbh.spring.outbox.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

/**
 * Configuration properties for the relational databases (RDBMS) based outbox.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ConfigurationProperties(prefix = "spring.outbox.relational")
class RelationalDatabaseProperties {

    private final boolean autoCreate;

    @Nullable
    private final String schema;


    /**
     * Creates a new {@link RelationalDatabaseProperties} instance.
     *
     * @param autoCreate whether to create outbox tables on startup. Defaults to {@code false}.
     * @param schema optional schema name for the outbox table.
     */
    @ConstructorBinding
    RelationalDatabaseProperties(@DefaultValue("false") boolean autoCreate, @Nullable String schema) {
        this.autoCreate = autoCreate;
        this.schema = schema;
    }


    /**
     * Specifies whether outbox tables should be created automatically.
     */
    boolean isAutoCreate() {
        return autoCreate;
    }

    /**
     * The name of the schema where the outbox table resides, or {@code null} if not specified.
     */
    @Nullable
    String getSchema() {
        return schema;
    }
}
