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

package sample;

import java.nio.file.Path;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import sample.TestUtils.EnvVars;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static sample.TestUtils.resolveDockerfilePath;

/**
 * Complete E2E integration tests based on Testcontainers.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@EnabledIf(expression = "#{environment.acceptsProfiles('testcontainers')}")
class IntegrationTests {

    @SpringBootTest
    abstract static class TestBase {

        @Autowired Orders orders;
        @Autowired GenericContainer<?> connectorContainer;
        @MockitoBean ShopifyIntegration.ShopifyOrderSynchronizer shopifyOrderSynchronizer;

        @BeforeEach
        void setUp() {
            connectorContainer.start();
        }

        @Test
        void placeOrder() {

            Order order = orders.save(new Order());
            orders.markPaid(order);

            Awaitility.await().untilAsserted(() ->
              verify(shopifyOrderSynchronizer, times(1)).syncOrder(argThat(messageBody ->
                messageBody != null &&
                  order.getId().toString().equals(messageBody.orderId)
              ))
            );
        }
    }

    static class RabbitConfiguration extends CommonConfiguration {

        @Bean
        @ServiceConnection
        RabbitMQContainer rabbitMqContainer(Network network) {
            return new RabbitMQContainer("rabbitmq:4")
              .withNetwork(network)
              .withNetworkAliases("rabbit");
        }

        @Bean
        public Queue queue() {
            return QueueBuilder.nonDurable("shopify.orders").autoDelete().exclusive().build();
        }
    }

    static class CommonConfiguration {

        @Bean
        Network network() {
            return Network.newNetwork();
        }
    }

    @Nested
    class MysqlRabbit extends TestBase {

        @TestConfiguration
        static class MysqlRabbitConfiguration extends RabbitConfiguration {

            @Bean
            @ServiceConnection
            MySQLContainer<?> mysqlContainer(Network network) {
                return new MySQLContainer<>("mysql:8.2")
                  .withNetwork(network)
                  .withNetworkAliases("mysql")
                  .withDatabaseName("data")
                  .withUsername("root")
                  .withPassword("secret");
            }

            @Bean
            GenericContainer<?> connectorContainer(Network network, MySQLContainer<?> mysqlContainer,
              RabbitMQContainer rabbitMqContainer) {

                Path dockerfilePath = resolveDockerfilePath("spring-outbox-debezium-connector-mysql-rabbit");
                ImageFromDockerfile connectorImage = new ImageFromDockerfile()
                  .withDockerfile(dockerfilePath);

                return new GenericContainer<>(connectorImage)
                  .withNetwork(network)
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME, "mysql")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME, "data")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_USER, "root")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD, "secret")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_OFFSETSTORAGE_FILEPATH, "/tmp/outbox-offset.dat")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_SCHEMAHISTORY_FILEPATH, "/tmp/outbox-schema-history.dat")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_ORDERPAID_ROUTINGKEY, "shopify.orders")
                  .withEnv(EnvVars.SPRING_RABBITMQ_HOST, "rabbit")
                  .withEnv(EnvVars.SPRING_RABBITMQ_USERNAME, "guest")
                  .withEnv(EnvVars.SPRING_RABBITMQ_PASSWORD, "guest")
                  .dependsOn(mysqlContainer, rabbitMqContainer)
                  .withExposedPorts(8080)
                  .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MysqlRabbitConnector Container")));
            }
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.outbox.rdbms.schema=common")
    class PostgresRabbit extends TestBase {

        @TestConfiguration
        static class PostgresRabbitConfiguration extends RabbitConfiguration {

            @Bean
            @ServiceConnection
            PostgreSQLContainer<?> postgresContainer(Network network) {
                return new PostgreSQLContainer<>(DockerImageName.parse("quay.io/debezium/postgres:15")
                  .asCompatibleSubstituteFor("postgres"))
                  .withNetwork(network)
                  .withNetworkAliases("postgres")
                  .withDatabaseName("data")
                  .withUsername("root")
                  .withPassword("secret");
            }

            @Bean
            GenericContainer<?> connectorContainer(Network network, PostgreSQLContainer<?> postgresContainer,
              RabbitMQContainer rabbitMqContainer) {

                Path dockerfilePath = resolveDockerfilePath("spring-outbox-debezium-connector-postgres-rabbit");
                ImageFromDockerfile connectorImage = new ImageFromDockerfile()
                  .withDockerfile(dockerfilePath);

                return new GenericContainer<>(connectorImage)
                  .withNetwork(network)
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME, "postgres")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME, "data")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_SCHEMA, "common")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_USER, "root")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD, "secret")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_OFFSETSTORAGE_FILEPATH, "/tmp/outbox-offset.dat")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_SCHEMAHISTORY_FILEPATH, "/tmp/outbox-schema-history.dat")
                  .withEnv(EnvVars.SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_ORDERPAID_ROUTINGKEY, "shopify.orders")
                  .withEnv(EnvVars.SPRING_RABBITMQ_HOST, "rabbit")
                  .withEnv(EnvVars.SPRING_RABBITMQ_USERNAME, "guest")
                  .withEnv(EnvVars.SPRING_RABBITMQ_PASSWORD, "guest")
                  .dependsOn(postgresContainer, rabbitMqContainer)
                  .withExposedPorts(8080)
                  .withLogConsumer(
                    new Slf4jLogConsumer(LoggerFactory.getLogger("PostgresRabbitConnector Container")));
            }
        }
    }
}
