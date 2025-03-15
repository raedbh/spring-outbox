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

import org.springframework.data.mongodb.core.MongoTemplate;

import io.github.raedbh.spring.outbox.core.OutboxEntry;
import io.github.raedbh.spring.outbox.core.OutboxRepository;

public class OutboxMongoRepository implements OutboxRepository {

    private final MongoTemplate mongoTemplate;

    public OutboxMongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(OutboxEntry entry) {

        MongoOutboxEntry mongoOutboxEntry = new MongoOutboxEntry(entry.getId().value(),
          entry.getType(),
          entry.getPayload(),
          entry.getMetadata());

        mongoTemplate.save(mongoOutboxEntry);
    }
}
