/*
 *  Copyright 2025-2026 the original authors.
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

package io.github.raedbh.spring.outbox.connector.mysql;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.raedbh.spring.outbox.connector.core.OutboxConnectorProperties;

import static java.util.Objects.requireNonNullElse;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class MysqlConnectorConfiguration {

    @Bean
    public io.debezium.config.Configuration mysqlConfiguration(OutboxConnectorProperties props) {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("name", "outbox-mysql-connector");
        configMap.put("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        configMap.put("database.server.id", "1");
        configMap.put("database.hostname", props.getDatabase().getHostname());
        configMap.put("database.port", requireNonNullElse(props.getDatabase().getPort(), "3306"));
        configMap.put("database.user", props.getDatabase().getUser());
        configMap.put("database.password", props.getDatabase().getPassword());
        configMap.put("database.include.list", props.getDatabase().getDbname());
        configMap.put("table.include.list", props.getDatabase().getDbname() + ".outbox");

        configMap.put("offset.storage", props.getOffsetStorage().getClassName());

        configMap.put("offset.storage.file.filename",
          props.getOffsetStorage().filePathOrTempFile("outbox-mysql-offset-"));

        configMap.put("topic.prefix", "outbox");

        configMap.put("schema.history.internal", props.getSchemaHistory().getClassName());

        configMap.put("schema.history.internal.file.filename",
          props.getSchemaHistory().filePathOrTempFile("outbox-mysql-schema-history-"));

        configMap.put("snapshot.mode", props.getSnapshotMode());
        configMap.put("snapshot.locking.mode", props.getSnapshotLockingMode());

        configMap.putAll(props.getAdditionalProperties());

        return io.debezium.config.Configuration.from(configMap);
    }
}
