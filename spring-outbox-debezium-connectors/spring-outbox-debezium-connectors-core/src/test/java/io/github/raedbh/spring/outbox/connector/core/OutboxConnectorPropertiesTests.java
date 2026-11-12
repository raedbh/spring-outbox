/*
 *  Copyright 2026 the original authors.
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

import java.io.File;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.raedbh.spring.outbox.connector.core.OutboxConnectorProperties.OffsetStorageProperties;
import io.github.raedbh.spring.outbox.connector.core.OutboxConnectorProperties.SchemaHistoryProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Raed Ben Hamouda
 */
class OutboxConnectorPropertiesTests {

    @Test
    void offsetStorageUsesConfiguredFilePathWithoutCreatingTempFile() {

        // given an offset storage configured with a file path
        OffsetStorageProperties offsetStorage = new OffsetStorageProperties();
        offsetStorage.setFilePath("/var/outbox/offset.dat");
        String tempFilePrefix = uniqueTempFilePrefix();

        // when
        String filePath = offsetStorage.filePathOrTempFile(tempFilePrefix);

        // then the configured path is used and no temporary file is left behind
        assertThat(filePath).isEqualTo("/var/outbox/offset.dat");
        assertThat(tempFilesStartingWith(tempFilePrefix)).isEmpty();
    }

    @Test
    void offsetStorageFallsBackToTempFileWithoutConfiguredFilePath() {

        // given an offset storage without file path
        OffsetStorageProperties offsetStorage = new OffsetStorageProperties();
        String tempFilePrefix = uniqueTempFilePrefix();

        // when
        String filePath = offsetStorage.filePathOrTempFile(tempFilePrefix);

        // then a temporary file named with the prefix is created
        File tempFile = new File(filePath);
        tempFile.deleteOnExit();
        assertThat(tempFile).exists();
        assertThat(tempFile.getName()).startsWith(tempFilePrefix);
    }

    @Test
    void schemaHistoryUsesConfiguredFilePathWithoutCreatingTempFile() {

        // given a schema history configured with a file path
        SchemaHistoryProperties schemaHistory = new SchemaHistoryProperties();
        schemaHistory.setFilePath("/var/outbox/schema-history.dat");
        String tempFilePrefix = uniqueTempFilePrefix();

        // when
        String filePath = schemaHistory.filePathOrTempFile(tempFilePrefix);

        // then the configured path is used and no temporary file is left behind
        assertThat(filePath).isEqualTo("/var/outbox/schema-history.dat");
        assertThat(tempFilesStartingWith(tempFilePrefix)).isEmpty();
    }

    @Test
    void schemaHistoryFallsBackToTempFileWithoutConfiguredFilePath() {

        // given a schema history without file path
        SchemaHistoryProperties schemaHistory = new SchemaHistoryProperties();
        String tempFilePrefix = uniqueTempFilePrefix();

        // when
        String filePath = schemaHistory.filePathOrTempFile(tempFilePrefix);

        // then a temporary file named with the prefix is created
        File tempFile = new File(filePath);
        tempFile.deleteOnExit();
        assertThat(tempFile).exists();
        assertThat(tempFile.getName()).startsWith(tempFilePrefix);
    }

    private static String uniqueTempFilePrefix() {
        return "outbox-test-" + UUID.randomUUID() + "-";
    }

    private static File[] tempFilesStartingWith(String prefix) {
        return new File(System.getProperty("java.io.tmpdir"))
          .listFiles((dir, name) -> name.startsWith(prefix));
    }
}
