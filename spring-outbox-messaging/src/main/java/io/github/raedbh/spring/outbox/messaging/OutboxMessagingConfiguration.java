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

import java.io.Serializable;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.Deserializer;

/**
 * Auto-configures messaging components for Spring Outbox pattern.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class OutboxMessagingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Deserializer<Serializable> outboxDeserializer() {
        return new OutboxDefaultDeserializer();
    }
}
