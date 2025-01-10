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

package io.github.raedbh.spring.outbox.connector.core;

/**
 * Resolves the property keys for outbox connector message configuration.
 * The keys follow the format: <b>configPrefix.typeKey.configName</b>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * spring.outbox.connector.kafka.messages.email-notification.topic=emails
 * }
 * </pre>
 * <p>In this example:</p>
 * <ul>
 *     <li><b>OutboxType</b> is <code>EmailNotification</code></li>
 *     <li><b>configPrefix</b> is <code>spring.outbox.connector.kafka.messages</code></li>
 *     <li><b>typeKey</b> is <code>email-notification</code></li>
 *     <li><b>configName</b> is <code>topic</code></li>
 * </ul>
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxMessageConfigKeyResolver {

    private final String configPrefix;
    private final String typeKey;


    public OutboxMessageConfigKeyResolver(String configPrefix, String outboxType) {

        if (configPrefix == null) {
            throw new IllegalArgumentException("configPrefix must not be null");
        }
        if (outboxType == null) {
            throw new IllegalArgumentException("outboxType must not be null");
        }

        this.configPrefix = configPrefix;
        this.typeKey = outboxType.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }


    public String resolve(String configName) {
        return "%s.%s.%s".formatted(configPrefix, typeKey, configName);
    }
}
