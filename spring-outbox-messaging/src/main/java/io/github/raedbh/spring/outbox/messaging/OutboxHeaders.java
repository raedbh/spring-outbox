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

package io.github.raedbh.spring.outbox.messaging;

/**
 * Constants for outbox message headers.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public final class OutboxHeaders {

    public static final String EVENT_ENTITY_TYPE = "event_entity_type";
    public static final String EVENT_ENTITY_ID = "event_entity_id";
    public static final String EVENT_OCCURRED_AT = "event_occurred_at";
    public static final String OPERATION = "operation";

    private OutboxHeaders() {
        /* prevent instantiation */
    }
}