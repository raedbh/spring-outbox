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

package sample.sourcing.rfp;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.github.raedbh.spring.outbox.messaging.OutboxMessageBody;
import sample.common.EntityIdentifier;
import sample.sourcing.common.ProposalMessageBody;

/**
 * @author Raed Ben Hamouda
 */
class RfpEventHandlers {

    @Component
    static class ProposalAwardHandler {

        private final RfpManagement rfpManagement;

        ProposalAwardHandler(RfpManagement rfpManagement) {
            this.rfpManagement = rfpManagement;
        }

        @RabbitListener(queues = "rfp.proposals")
        void onProposalAwarded(@OutboxMessageBody(operation = "award") Optional<ProposalMessageBody> messageBody) {
            messageBody.ifPresent(body ->
              rfpManagement.close(EntityIdentifier.fromString(body.rfpId)));
        }

    }
}
