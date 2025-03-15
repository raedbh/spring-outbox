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

package common;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.kafka.KafkaContainer;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class CommonTestConfigurations {

    public interface NetworkConfiguration {

        @Bean
        default Network network() {
            return Network.newNetwork();
        }
    }

    public interface RabbitConfiguration {

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

    public interface KafkaConfiguration {

        @Bean
        @ServiceConnection
        default KafkaContainer kafkaContainer(Network network) {
            return new KafkaContainer("apache/kafka:3.7.0")
              .withNetwork(network)
              .withNetworkAliases("kafka");
        }
    }
}
