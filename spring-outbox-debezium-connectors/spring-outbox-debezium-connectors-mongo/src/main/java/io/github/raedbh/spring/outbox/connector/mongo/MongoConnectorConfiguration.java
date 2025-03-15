
/*
 *  Copyright 2025 the original authors.
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

package io.github.raedbh.spring.outbox.connector.mongo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.raedbh.spring.outbox.connector.core.OutboxConnectorProperties;

import static java.util.Objects.requireNonNullElse;

@Configuration(proxyBeanMethods = false)
public class MongoConnectorConfiguration {

    @Bean
    public io.debezium.config.Configuration mongoConfiguration(OutboxConnectorProperties props) {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("name", "outbox-mongo-connector");
        configMap.put("connector.class", "io.debezium.connector.mongodb.MongoDbConnector");
        configMap.put("mongodb.connection.string", props.getDatabase().getUrl());
        configMap.put("mongodb.user", props.getDatabase().getUser());
        configMap.put("mongodb.password", props.getDatabase().getPassword());
        configMap.put("database.include.list", props.getDatabase().getDbname());
        configMap.put("collection.include.list", props.getDatabase().getDbname() + ".outbox");

        configMap.put("offset.storage", props.getOffsetStorage().getClassName());

        try {
            configMap.put("offset.storage.file.filename",
              requireNonNullElse(props.getOffsetStorage().getFilePath(),
                File.createTempFile("outbox-mongo-offset-", ".dat").getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        configMap.put("topic.prefix", props.getTopicPrefix());

        configMap.put("snapshot.mode", props.getSnapshotMode());
        configMap.put("snapshot.locking.mode", props.getSnapshotLockingMode());

        configMap.putAll(props.getAdditionalProperties());

        return io.debezium.config.Configuration.from(configMap);
    }
}
