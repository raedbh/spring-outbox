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

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;

import io.github.raedbh.spring.outbox.core.OutboxMessageBody;

/**
 * Resolves method arguments annotated with {@link OutboxMessageBody} by deserializing
 * the message payload into the specified parameter type.
 *
 * <p>Note: the parameter type, or its nested type if the parameter is {@link Optional},
 * must implement {@link Serializable}.</p>
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String OPERATION_HEADER = "operation";

    private final Deserializer<Serializable> deserializer;

    public OutboxMethodArgumentResolver(Deserializer<Serializable> deserializer) {this.deserializer = deserializer;}

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OutboxMessageBody.class);
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        OutboxMessageBody annotation = parameter.getParameterAnnotation(OutboxMessageBody.class);
        if (annotation == null) {
            throw new IllegalStateException(
              "Parameter is not annotated with @OutboxMessageBody. This should not happen!");
        }

        Class<?> targetClass = parameter.nestedIfOptional().getNestedParameterType();
        if (!Serializable.class.isAssignableFrom(targetClass)) {
            throw new IllegalStateException(
              "The parameter type '" + targetClass.getName() + "' must implement Serializable.");
        }

        String operationFromAnnotation = annotation.operation();
        String operationFromHeader = message.getHeaders().get(OPERATION_HEADER, String.class);
        boolean isOptionalTargetClass = (parameter.getParameterType() == Optional.class);

        if (operationFromAnnotation.isEmpty() || operationFromAnnotation.equals(operationFromHeader)) {
            Serializable payload = deserializePayload(message.getPayload());
            return (isOptionalTargetClass ? Optional.ofNullable(payload) : payload);
        }
        return (isOptionalTargetClass ? Optional.empty() : null);
    }

    @Nullable
    private Serializable deserializePayload(Object payload) throws IOException {

        if (!(payload instanceof byte[] bytes)) {
            throw new IllegalArgumentException("Payload must be of type byte[] for deserialization.");
        }

        if (bytes.length == 0) {
            return null;
        }
        return deserializer.deserializeFromByteArray(bytes);
    }
}
