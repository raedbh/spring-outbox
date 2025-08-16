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

package io.github.raedbh.spring.outbox.kafka.config;

import java.io.Serializable;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.Deserializer;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;

import io.github.raedbh.spring.outbox.messaging.OutboxMethodArgumentResolver;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class OutboxKafkaListenerConfiguration implements KafkaListenerConfigurer {

    private final Deserializer<Serializable> deserializer;


    public OutboxKafkaListenerConfiguration(Deserializer<Serializable> deserializer) {
        this.deserializer = deserializer;
    }


    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setCustomMethodArgumentResolvers(new OutboxMethodArgumentResolver(deserializer));
    }
}
