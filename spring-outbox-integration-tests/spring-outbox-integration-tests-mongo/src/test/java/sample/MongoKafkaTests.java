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

import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.kafka.KafkaContainer;

import common.CommonTestConfigurations.KafkaConfiguration;
import common.CommonTestConfigurations.NetworkConfiguration;
import common.TestUtils.EnvVars;
import sample.MongoTestConfigurations.MongoConfiguration;

import static common.TestUtils.resolveDockerfilePath;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ActiveProfiles("kafka")
class MongoKafkaTests extends AbstractIntegrationTests {

    @TestConfiguration
    static class MongoKafkaConfiguration implements NetworkConfiguration, MongoConfiguration, KafkaConfiguration {

        @Bean
        GenericContainer<?> connectorContainer(Network network, MongoDBContainer mongoContainer,
          KafkaContainer kafkaContainer) {

            mongoContainer.start();

            Path dockerfilePath = resolveDockerfilePath("spring-outbox-debezium-connector-mongo-kafka");
            ImageFromDockerfile connectorImage = new ImageFromDockerfile()
              .withDockerfile(dockerfilePath);

            String replicaSetUrl = "mongodb://mongo:27017/?replicaSet=rs0&retryWrites=false";

            return new GenericContainer<>(connectorImage)
              .withNetwork(network)
              .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_URL, replicaSetUrl)
              .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME, "test")
              .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_SNAPSHOTMODE, "always")
              .withEnv("SPRING_DATA_MONGODB_URI", replicaSetUrl)

              .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_KAFKA_MESSAGES_ORDERPAID_TOPIC, "orders")
              .withEnv(EnvVars.SPRING_KAFKA_BOOTSTRAPSERVERS, "kafka:9093")
              .dependsOn(mongoContainer, kafkaContainer)
              .withExposedPorts(8080)
              .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MongoKafkaConnector")));
        }
    }
}
