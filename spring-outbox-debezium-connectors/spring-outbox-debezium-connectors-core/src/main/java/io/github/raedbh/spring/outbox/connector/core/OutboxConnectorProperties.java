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

package io.github.raedbh.spring.outbox.connector.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the outbox connector.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ConfigurationProperties("spring.outbox.connector")
public class OutboxConnectorProperties {

    private DatabaseProperties database = new DatabaseProperties();
    private OffsetStorageProperties offsetStorage = new OffsetStorageProperties();
    private SchemaHistoryProperties schemaHistory = new SchemaHistoryProperties();

    private String topicPrefix = "outbox";
    private String snapshotMode = "initial";
    private String snapshotLockingMode = "none";

    private Map<String, String> additionalProperties = new HashMap<>();

    public DatabaseProperties getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseProperties database) {
        this.database = database;
    }

    public OffsetStorageProperties getOffsetStorage() {
        return offsetStorage;
    }

    public void setOffsetStorage(OffsetStorageProperties offsetStorage) {
        this.offsetStorage = offsetStorage;
    }

    public SchemaHistoryProperties getSchemaHistory() {
        return schemaHistory;
    }

    public void setSchemaHistory(SchemaHistoryProperties schemaHistory) {
        this.schemaHistory = schemaHistory;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public String getSnapshotMode() {
        return snapshotMode;
    }

    public void setSnapshotMode(String snapshotMode) {
        this.snapshotMode = snapshotMode;
    }

    public String getSnapshotLockingMode() {
        return snapshotLockingMode;
    }

    public void setSnapshotLockingMode(String snapshotLockingMode) {
        this.snapshotLockingMode = snapshotLockingMode;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public static class DatabaseProperties {

        private Integer clientId = 0;
        private String url;
        private String hostname;
        private Integer port;
        private String dbname;
        private String user;
        private String password;
        private String schema;

        public Integer getClientId() {
            return clientId;
        }

        public void setClientId(Integer clientId) {
            this.clientId = clientId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getDbname() {
            return dbname;
        }

        public void setDbname(String dbname) {
            this.dbname = dbname;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

    }

    public static class OffsetStorageProperties {

        private String className = "org.apache.kafka.connect.storage.FileOffsetBackingStore";
        private String filePath;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

    public static class SchemaHistoryProperties {

        private String className = "io.debezium.storage.file.history.FileSchemaHistory";
        private String filePath;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
