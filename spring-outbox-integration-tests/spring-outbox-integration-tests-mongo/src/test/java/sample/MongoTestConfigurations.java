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

package sample;

import org.bson.UuidRepresentation;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class MongoTestConfigurations {

    static String replicaSetUrl = "mongodb://mongo:27017/?replicaSet=rs0&retryWrites=false";

    interface MongoConfiguration {

        @Bean
        @ServiceConnection
        default MongoDBContainer mongoDBContainer(Network network) {
            return new MongoDBContainer("mongo:6.0.3")
              .withNetwork(network)
              .withNetworkAliases("mongo")
              .withCommand("--replSet", "rs0")
              .withExposedPorts(27017);
        }

        @Bean
        default MongoClient mongoClient(MongoDBContainer mongoDBContainer) {

            var settings = MongoClientSettings.builder()
              .uuidRepresentation(UuidRepresentation.STANDARD)
              .applyConnectionString(new ConnectionString(mongoDBContainer.getReplicaSetUrl()))
              .build();

            return MongoClients.create(settings);
        }
    }
}
