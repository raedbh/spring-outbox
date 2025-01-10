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

package sample;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class TestUtils {

    /**
     * Resolves the path to a Dockerfile dynamically based on the resource directory of the test runtime.
     *
     * @param dockerfileDir connector project directory, where the Dockerfile is located.
     * @return resolved absolute path to the Dockerfile.
     */
    public static Path resolveDockerfilePath(String dockerfileDir) {
        return Paths.get(TestUtils.class.getResource("/").getPath())
          .getParent().getParent().getParent() // navigate to project root
          .resolve("spring-outbox-debezium-connectors")
          .resolve(dockerfileDir)
          .resolve("Dockerfile")
          .toAbsolutePath()
          .normalize();
    }

    /**
     * Constants for environment variables used in container setup for integration tests.
     */
    static class EnvVars {

        static final String SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME = "SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME";

        static final String SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME = "SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME";
        static final String SPRING_OUTBOX_CONNECTOR_DATABASE_SCHEMA = "SPRING_OUTBOX_CONNECTOR_DATABASE_SCHEMA";
        static final String SPRING_OUTBOX_CONNECTOR_DATABASE_USER = "SPRING_OUTBOX_CONNECTOR_DATABASE_USER";
        static final String SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD = "SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD";
        static final String SPRING_OUTBOX_CONNECTOR_OFFSETSTORAGE_FILEPATH = "SPRING_OUTBOX_CONNECTOR_OFFSETSTORAGE_FILEPATH";
        static final String SPRING_OUTBOX_CONNECTOR_SCHEMAHISTORY_FILEPATH = "SPRING_OUTBOX_CONNECTOR_SCHEMAHISTORY_FILEPATH";
        static final String SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_ORDERPAID_ROUTINGKEY = "SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_ORDERPAID_ROUTINGKEY";
        static final String SPRING_RABBITMQ_HOST = "SPRING_RABBITMQ_HOST";
        static final String SPRING_RABBITMQ_USERNAME = "SPRING_RABBITMQ_USERNAME";
        static final String SPRING_RABBITMQ_PASSWORD = "SPRING_RABBITMQ_PASSWORD";
        static final String SPRING_OUTBOX_CONNECTOR_KAFKA_MESSAGES_ORDERPAID_TOPIC = "SPRING_OUTBOX_CONNECTOR_KAFKA_MESSAGES_ORDERPAID_TOPIC";
        static final String SPRING_OUTBOX_CONNECTOR_SNAPSHOTMODE = "SPRING_OUTBOX_CONNECTOR_SNAPSHOTMODE";
        static final String SPRING_KAFKA_BOOTSTRAPSERVERS = "SPRING_KAFKA_BOOTSTRAPSERVERS";

        static final Map<String, String> POSTGRES_CONNECTION_PARAMS = Map.of(
          SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME, "postgres",
          SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME, "data",
          SPRING_OUTBOX_CONNECTOR_DATABASE_SCHEMA, "common",
          SPRING_OUTBOX_CONNECTOR_DATABASE_USER, "root",
          SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD, "secret");

        static final Map<String, String> MYSQL_CONNECTION_PARAMS = Map.of(
          SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME, "mysql",
          SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME, "data",
          SPRING_OUTBOX_CONNECTOR_DATABASE_USER, "root",
          SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD, "secret");

        static final Map<String, String> RABBIT_CONNECTION_PARAMS = Map.of(
          SPRING_RABBITMQ_HOST, "rabbit",
          SPRING_RABBITMQ_USERNAME, "guest",
          SPRING_RABBITMQ_PASSWORD, "guest");

    }
}
