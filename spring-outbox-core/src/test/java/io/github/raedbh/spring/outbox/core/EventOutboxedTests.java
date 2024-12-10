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

import org.junit.jupiter.api.Test;

import com.acme.eshop.EmailNotification;
import com.acme.eshop.Order;
import com.acme.eshop.OrderPaid;
import com.acme.eshop.SmsNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link EventOutboxed}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class EventOutboxedTests {

    @Test
    void rejectNullSource() {
        assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> new OrderPaid(null))
          .withMessageContaining("source");
    }

    @Test
    void rejectNullCommand() {

        assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> new OrderPaid(new Order(), null))
          .withMessageContaining("Command");

        assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> new OrderPaid(new Order()).addCommand(null))
          .withMessageContaining("Command");

        assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> new OrderPaid(new Order()).addCommands(null, null))
          .withMessageContaining("Command");
    }

    @Test
    void withCommands() {

        SmsNotification smsNotification = new SmsNotification();
        EmailNotification emailNotification = new EmailNotification();

        OrderPaid event = new OrderPaid(new Order());
        event.addCommands(smsNotification, emailNotification);

        assertThat(event.getCommands())
          .hasSize(2)
          .containsExactly(smsNotification, emailNotification);
    }
}
