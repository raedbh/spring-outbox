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

package io.github.raedbh.spring.outbox.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import com.acme.eshop.EmailNotification;
import com.acme.eshop.Order;
import com.acme.eshop.OrderMessageBody;
import com.acme.eshop.OrderPaid;
import com.acme.eshop.SmsNotification;

import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.EVENT_ENTITY_ID;
import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.EVENT_ENTITY_TYPE;
import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.EVENT_OCCURRED_AT;
import static io.github.raedbh.spring.outbox.core.PredefinedMetadataKeys.OPERATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

/**
 * Tests for {@link OutboxManager}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class OutboxManagerTests {

    OutboxManager outboxManager;

    @Mock OutboxRepository outboxRepository;
    @Spy OutboxDefaultSerializer outboxSerializer;
    @Mock PlatformTransactionManager transactionManager;
    @Mock SerializableTargetConverterRegistry converterRegistry;

    @Mock Supplier<Object> proceedSave;

    @BeforeEach
    void setUp() {
        outboxManager = new OutboxManager(outboxRepository, outboxSerializer, transactionManager, converterRegistry);
    }

    @Test
    void proceedInvocationThenSaveOutboxEntriesForEvent() throws Exception {

        var order = new Order();
        var orderPaid = new OrderPaid(order);

        order.assignEvent(orderPaid);

        InOrder inOrder = inOrder(outboxSerializer, proceedSave, outboxRepository);

        outboxManager.proceedInvocationAndSaveOutboxEntries(order, proceedSave);

        ArgumentCaptor<OutboxEntry> entryCaptor = forClass(OutboxEntry.class);

        inOrder.verify(outboxSerializer).serializeToByteArray(order); // called first
        inOrder.verify(proceedSave).get(); // next
        inOrder.verify(outboxRepository).save(entryCaptor.capture()); // last

        List<OutboxEntry> savedEntries = entryCaptor.getAllValues();
        assertThat(savedEntries).hasSize(1);

        // validate the main event outbox entry
        OutboxEntry mainEntry = savedEntries.get(0);
        assertThat(mainEntry.getType()).isEqualTo(orderPaid.getName());
        assertThat(mainEntry.getMetadata())
          .containsEntry(EVENT_ENTITY_ID, order.getId().toString())
          .containsEntry(EVENT_ENTITY_TYPE, "Order")
          .containsEntry(OPERATION, orderPaid.getOperation())
          .containsKey(EVENT_OCCURRED_AT);
    }

    @Test
    void proceedInvocationThenSaveOutboxEntriesForEventAndCommands() throws Exception {

        var order = new Order();

        var orderPaid = new OrderPaid(order);
        var smsNotification = new SmsNotification();
        var emailNotification = new EmailNotification();

        orderPaid.addCommands(smsNotification, emailNotification);

        order.assignEvent(orderPaid);

        InOrder inOrder = inOrder(outboxSerializer, proceedSave, outboxRepository);

        outboxManager.proceedInvocationAndSaveOutboxEntries(order, proceedSave);

        ArgumentCaptor<OutboxEntry> entryCaptor = forClass(OutboxEntry.class);

        inOrder.verify(outboxSerializer).serializeToByteArray(order); // called first
        inOrder.verify(outboxSerializer).serializeToByteArray(smsNotification);
        inOrder.verify(outboxSerializer).serializeToByteArray(emailNotification);
        inOrder.verify(proceedSave).get();
        inOrder.verify(outboxRepository, times(3)).save(entryCaptor.capture()); // last

        List<OutboxEntry> savedEntries = entryCaptor.getAllValues();
        assertThat(savedEntries).hasSize(3);

        // validate the main event outbox entry
        OutboxEntry mainEntry = savedEntries.get(0);
        assertThat(mainEntry.getType()).isEqualTo(orderPaid.getName());
        assertThat(mainEntry.getMetadata())
          .containsEntry(EVENT_ENTITY_ID, order.getId().toString())
          .containsEntry(EVENT_ENTITY_TYPE, "Order")
          .containsEntry(OPERATION, orderPaid.getOperation())
          .containsKey(EVENT_OCCURRED_AT);

        // validate the 1st command entry - SmsNotification
        OutboxEntry smsEntry = savedEntries.get(1);
        assertThat(smsEntry.getType()).isEqualTo(smsNotification.getName());

        // validate the 2nd command entry - EmailNotification
        OutboxEntry emailEntry = savedEntries.get(2);
        assertThat(emailEntry.getType()).isEqualTo(emailNotification.getName());
    }

    @Test
    void proceedInvocationThenSaveOutboxEntriesForEventWithConversionToMessageBody() throws Exception {

        var order = new Order();
        var orderPaid = new OrderPaid(order);

        order.assignEvent(orderPaid);
        given(converterRegistry.getConverter(Order.class))
          .willReturn(Optional.of(source -> new OrderMessageBody(((Order) source).getId().toString())));

        InOrder inOrder = inOrder(outboxSerializer, proceedSave, outboxRepository);

        outboxManager.proceedInvocationAndSaveOutboxEntries(order, proceedSave);

        ArgumentCaptor<OutboxEntry> entryCaptor = forClass(OutboxEntry.class);

        inOrder.verify(outboxSerializer).serializeToByteArray(argThat(argument -> {
            if (argument instanceof OrderMessageBody messageBody) {
                return messageBody.orderId.equals(order.getId().toString());
            }
            return false;
        }));
        inOrder.verify(proceedSave).get();
        inOrder.verify(outboxRepository).save(entryCaptor.capture());

        List<OutboxEntry> savedEntries = entryCaptor.getAllValues();
        assertThat(savedEntries).hasSize(1);

        // validate the main event outbox entry
        OutboxEntry mainEntry = savedEntries.get(0);
        assertThat(mainEntry.getType()).isEqualTo(orderPaid.getName());
        assertThat(mainEntry.getMetadata())
          .containsEntry(EVENT_ENTITY_ID, order.getId().toString())
          .containsEntry(EVENT_ENTITY_TYPE, "Order")
          .containsEntry(OPERATION, orderPaid.getOperation())
          .containsKey(EVENT_OCCURRED_AT);
    }
}
