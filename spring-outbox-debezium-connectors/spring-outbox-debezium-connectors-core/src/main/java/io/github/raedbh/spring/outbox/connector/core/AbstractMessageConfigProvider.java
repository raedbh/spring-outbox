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

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for providing and caching message configurations based on outbox types.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public abstract class AbstractMessageConfigProvider<T> {

    private final String configPrefix;
    private final Map<String, T> configCache;


    protected AbstractMessageConfigProvider(String broker) {
        if (broker == null) {
            throw new IllegalArgumentException("broker must not be null");
        }
        this.configPrefix = "spring.outbox.connector." + broker + ".messages";
        this.configCache = new HashMap<>();
    }


    protected OutboxMessageConfigKeyResolver newKeyResolver(String outboxType) {
        return new OutboxMessageConfigKeyResolver(configPrefix, outboxType);
    }

    public T getConfig(String outboxType) {
        return configCache.computeIfAbsent(outboxType, this::loadConfig);
    }

    protected abstract T loadConfig(String outboxType);
}
