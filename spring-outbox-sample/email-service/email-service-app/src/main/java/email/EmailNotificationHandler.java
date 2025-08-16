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

package email;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import email.contracts.EmailMessageBody;
import io.github.raedbh.spring.outbox.messaging.OutboxMessageBody;

/**
 * Handles email notification messages from the message queue.
 *
 * @author Raed Ben Hamouda
 */
@Component
public class EmailNotificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationHandler.class);

    private final EmailSender emailSender;

    public EmailNotificationHandler(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = "emails")
    public void handleEmailNotification(@OutboxMessageBody Optional<EmailMessageBody> messageBody) {
        messageBody.ifPresent(email -> {
            logger.info("Received email notification for type: {} to: {}",
              email.getType(), email.getTo());

            emailSender.send(email);
        });
    }
}
