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

package io.github.raedbh.spring.outbox.rabbit.config;

import java.io.Serializable;

import org.springframework.core.MethodParameter;
import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;

import io.github.raedbh.spring.outbox.core.MessageWith;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String OPERATION_HEADER = "operation";

    private final Deserializer<Serializable> deserializer;

    public OutboxMethodArgumentResolver(Deserializer<Serializable> deserializer) {this.deserializer = deserializer;}

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        MessageWith annotation = parameter.getParameterAnnotation(MessageWith.class);
        return Serializable.class.isAssignableFrom(parameter.getParameterType()) &&
          annotation != null && annotation.operation() != null;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        MessageWith annotation = parameter.getParameterAnnotation(MessageWith.class);
        String operationHeader = message.getHeaders().get(OPERATION_HEADER, String.class);
        if (annotation != null && annotation.operation().equals(operationHeader)) {
            return deserializer.deserializeFromByteArray((byte[]) message.getPayload());
        }
        return null;
    }
}
