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

package com.acme.eshop;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Raed Ben Hamouda
 */
public interface OrderRepository extends CrudRepository<Order, UUID> {

    @Transactional
    default Order markPaid(Order order, EmailNotification emailNotification) {
        order.markPaid(emailNotification);
        return save(order);
    }

    @Transactional
    default Order markPaidThrowsException(Order order, EmailNotification emailNotification) {
        markPaid(order, emailNotification);
        throw new RuntimeException("Simulated failure after saving order");
    }
}
