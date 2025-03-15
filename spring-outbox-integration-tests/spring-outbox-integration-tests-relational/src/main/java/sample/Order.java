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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.github.raedbh.spring.outbox.core.RootEntity;

/**
 * @author Raed Ben Hamouda
 */
@Table(name = "orders")
@Entity
public class Order extends RootEntity {

    @Id
    private final UUID id;
    private final BigDecimal totalAmount;
    private final LocalDateTime orderedAt;
    private final UUID customerId;
    private Status status;


    Order() {
        this.id = UUID.randomUUID();
        this.totalAmount = BigDecimal.TEN; // Default value for the sake of simplicity
        this.status = Status.PENDING_PAYMENT;
        this.orderedAt = LocalDateTime.now();
        this.customerId = UUID.randomUUID(); // Simplified; should be passed explicitly in a real scenario
    }

    Order markPaid() {
        if (isPaid()) {
            throw new IllegalStateException("Order #" + id + " is already paid!");
        }
        this.status = Status.PAID;

        assignEvent(new OrderPaid(this));

        return this;
    }


    public boolean isPaid() {
        return this.status.equals(Status.PAID);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public enum Status {

        PENDING_PAYMENT,

        PAID
    }
}
