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

package io.github.raedbh.spring.outbox.mongo;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.acme.eshop.Application;
import com.acme.eshop.EmailNotification;
import com.acme.eshop.NonRootEntity;
import com.acme.eshop.NonRootEntityRepository;
import com.acme.eshop.Order;
import com.acme.eshop.OrderRepository;

import io.github.raedbh.spring.outbox.core.StateChangingMethodInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Mongo module, mainly testing {@link OutboxMongoRepositoryFactoryBean}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@SpringBootTest(classes = Application.class)
class OutboxMongoTests {

    @Autowired MongoTemplate mongoTemplate;

    @Autowired OrderRepository aRootEntityRepository;
    @Autowired NonRootEntityRepository nonRootEntityRepository;

    @BeforeEach
    void clearDatabase() {
        mongoTemplate.dropCollection("outbox");
        mongoTemplate.dropCollection("orders");
    }

    @Test
    void noOutboxAdviceRegisteredForNonRootEntity() {

        Advisor[] advisors = ((Advised) nonRootEntityRepository).getAdvisors();

        assertThat(advisors)
          .noneMatch(advisor ->
            advisor.getAdvice() instanceof StateChangingMethodInterceptor);
    }

    @Test
    void registerOutboxAdviceForRootEntity() {

        Advisor[] advisors = ((Advised) aRootEntityRepository).getAdvisors();

        assertThat(advisors)
          .anyMatch(advisor ->
            advisor.getAdvice() instanceof StateChangingMethodInterceptor);
    }

    @Test
    void noOutboxEntriesWhenSavingNonRootEntity() {
        nonRootEntityRepository.save(new NonRootEntity("123"));
        assertThat(outboxCount()).isNotNull().isEqualTo(0);
    }

    @Test
    void saveRootEntityWithNoEventAssigned() {

        aRootEntityRepository.save(new Order());

        assertThat(ordersCount()).isNotNull().isEqualTo(1);
        assertThat(outboxCount()).isNotNull().isEqualTo(0);
    }

    @Test
    void saveRootEntityWithAssignedEventCreatesOutboxEntries() {

        Order order = aRootEntityRepository.save(new Order());
        aRootEntityRepository.markPaid(order, new EmailNotification("cust@test.com", "Order Placed", "Body"));

        assertThat(ordersCount()).isNotNull().isEqualTo(1);

        assertThat(outboxCount()).isNotNull().isEqualTo(2);

        List<MongoOutboxEntry> outboxEntries = mongoTemplate.findAll(MongoOutboxEntry.class);
        assertThat(outboxEntries)
          .extracting(entry -> entry.type)
          .containsExactlyInAnyOrder("OrderPaid", "EmailNotification");
    }

    private Long outboxCount() {
        return mongoTemplate.count(new Query(), "outbox");
    }

    private Long ordersCount() {
        return mongoTemplate.count(new Query(), "orders");
    }

}
