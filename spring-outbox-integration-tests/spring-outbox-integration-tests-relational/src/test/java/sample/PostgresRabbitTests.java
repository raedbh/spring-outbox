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
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import common.CommonTestConfigurations.NetworkConfiguration;
import common.CommonTestConfigurations.RabbitConfiguration;
import common.TestUtils;
import common.TestUtils.EnvVars;
import sample.RelationalTestConfigurations.PostgresConfiguration;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ActiveProfiles("rabbit")
@TestPropertySource(properties = "spring.outbox.rdbms.schema=common")
class PostgresRabbitTests extends AbstractIntegrationTests {

    @TestConfiguration
    static class PostgresRabbitConfiguration implements NetworkConfiguration, PostgresConfiguration,
      RabbitConfiguration {

        @Bean
        GenericContainer<?> connectorContainer(Network network, PostgreSQLContainer<?> postgresContainer,
          RabbitMQContainer rabbitContainer) {

            Path dockerfilePath = TestUtils.resolveDockerfilePath("spring-outbox-debezium-connector-postgres-rabbit");
            ImageFromDockerfile connectorImage = new ImageFromDockerfile()
              .withDockerfile(dockerfilePath);

            return new GenericContainer<>(connectorImage)
              .withNetwork(network)
              .withEnv(EnvVars.POSTGRES_CONNECTION_PARAMS)
              .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_SNAPSHOTMODE, "always")
              .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_ORDERPAID_ROUTINGKEY, "shopify.orders")
              .withEnv(EnvVars.RABBIT_CONNECTION_PARAMS)
              .dependsOn(postgresContainer, rabbitContainer)
              .withExposedPorts(8080)
              .withLogConsumer(
                new Slf4jLogConsumer(LoggerFactory.getLogger("PostgresRabbitConnector")));
        }
    }
}
