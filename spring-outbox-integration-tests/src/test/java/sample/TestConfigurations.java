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

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class TestConfigurations {

    interface NetworkConfiguration {

        @Bean
        default Network network() {
            return Network.newNetwork();
        }
    }

    interface RabbitConfiguration {

        @Bean
        @ServiceConnection
        default RabbitMQContainer rabbitContainer(Network network) {
            return new RabbitMQContainer("rabbitmq:4")
              .withNetwork(network)
              .withNetworkAliases("rabbit");
        }

        @Bean
        default Queue queue() {
            return QueueBuilder.nonDurable("shopify.orders").autoDelete().exclusive().build();
        }
    }

    interface KafkaConfiguration {

        @Bean
        @ServiceConnection
        default KafkaContainer kafkaContainer(Network network) {
            return new KafkaContainer("apache/kafka:3.7.0")
              .withNetwork(network)
              .withNetworkAliases("kafka");
        }
    }

    interface MySQLConfiguration {

        @Bean
        @ServiceConnection
        default MySQLContainer<?> mysqlContainer(Network network) {
            return new MySQLContainer<>("mysql:8.3")
              .withNetwork(network)
              .withNetworkAliases("mysql")
              .withDatabaseName("data")
              .withUsername("root")
              .withPassword("secret");
        }
    }

    interface PostgresConfiguration {

        @Bean
        @ServiceConnection
        default PostgreSQLContainer<?> postgresContainer(Network network) {
            return new PostgreSQLContainer<>(DockerImageName.parse("quay.io/debezium/postgres:15")
              .asCompatibleSubstituteFor("postgres"))
              .withNetwork(network)
              .withNetworkAliases("postgres")
              .withDatabaseName("data")
              .withUsername("root")
              .withPassword("secret");
        }
    }
}
