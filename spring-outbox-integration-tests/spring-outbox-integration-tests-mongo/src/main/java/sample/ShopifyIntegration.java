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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.github.raedbh.spring.outbox.core.OutboxMessageBody;

/**
 * Listens for payment events and synchronizes order details with Shopify.
 *
 * <p>In a real-world scenario, this would likely be part of a dedicated (µ)Service
 * responsible for handling integrations with Shopify and possibly other e-commerce platforms.</p>
 *
 * @author Raed Ben Hamouda
 */
@Component
class ShopifyIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopifyIntegration.class);

    @Component
    @Profile("rabbit")
    static class RabbitOrderPaidListener {

        private final ShopifyOrderSynchronizer shopifyOrderSynchronizer;

        RabbitOrderPaidListener(ShopifyOrderSynchronizer shopifyOrderSynchronizer) {
            this.shopifyOrderSynchronizer = shopifyOrderSynchronizer;
        }

        @RabbitListener(queues = "shopify.orders")
        void onOrderPaid(@OutboxMessageBody(operation = "payment") Optional<OrderMessageBody> messageBody) {
            messageBody.ifPresent(shopifyOrderSynchronizer::syncOrder);
        }
    }

    @Component
    @Profile("kafka")
    static class KafkaOrderPaidListener {

        private final ShopifyOrderSynchronizer shopifyOrderSynchronizer;

        KafkaOrderPaidListener(ShopifyOrderSynchronizer shopifyOrderSynchronizer) {
            this.shopifyOrderSynchronizer = shopifyOrderSynchronizer;
        }

        @KafkaListener(groupId = "order-paid-consumer", topics = "orders", containerFactory = "outboxContainerFactory")
        void onOrderPaid(@OutboxMessageBody(operation = "payment") Optional<OrderMessageBody> messageBody) {
            messageBody.ifPresent(shopifyOrderSynchronizer::syncOrder);
        }
    }

    @Component
    static class ShopifyOrderSynchronizer {

        void syncOrder(OrderMessageBody messageBody) {
            if (messageBody == null) {
                return;
            }

            LOGGER.info("Synchronizing order #{} with Shopify", messageBody.orderId);
            // transform and load to Shopify
        }
    }
}
