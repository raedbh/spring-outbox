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

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.GenericContainer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Base class for integration tests.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@SpringBootTest
@EnabledIf(expression = "#{environment.acceptsProfiles('testcontainers')}")
abstract class AbstractIntegrationTest {

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
