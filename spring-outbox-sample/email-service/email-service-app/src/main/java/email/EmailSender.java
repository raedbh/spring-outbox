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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import email.contracts.EmailMessageBody;

/**
 * Service for sending emails. Currently, mocks email sending.
 *
 * @author Raed Ben Hamouda
 */
@Service
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    /**
     * Sends an email notification. Currently, logs the email instead of actually sending it.
     *
     * @param emailMessage the email message to send
     */
    public void send(EmailMessageBody emailMessage) {
        logger.info("📧 Sending email of type '{}' to {} recipients",
          emailMessage.getType(),
          emailMessage.getTo().size());

        emailMessage.getTo().forEach(contact ->
          logger.info("To: {} ({})", contact.getName(), contact.getEmail()));

        if (emailMessage.getTemplateParams() != null && !emailMessage.getTemplateParams().isEmpty()) {
            logger.info("📋 Template parameters: {}", emailMessage.getTemplateParams());
        }

        logger.info("✅ Email '{}' sent successfully", emailMessage.getType());
    }
}
