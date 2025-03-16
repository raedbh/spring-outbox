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

package io.github.raedbh.spring.outbox.core;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.serializer.Deserializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OutboxMethodArgumentResolver}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxMethodArgumentResolverTests {

    private static final String OPERATION_HEADER = "operation";
    private static final String TEST_OPERATION = "TEST_OPERATION";

    private OutboxMethodArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        Deserializer<Serializable> deserializer = new OutboxDefaultDeserializer();
        resolver = new OutboxMethodArgumentResolver(deserializer);
    }

    @Test
    void supportParameter() throws NoSuchMethodException {

        assertThat(resolver.supportsParameter(paramAnnotated(true, false))).isTrue();
        assertThat(resolver.supportsParameter(paramAnnotated(false, false))).isTrue();

        assertThat(resolver.supportsParameter(paramAnnotated(true, true))).isTrue();
        assertThat(resolver.supportsParameter(paramAnnotated(false, true))).isTrue();

        MethodParameter paramNotAnnotated = new MethodParameter(
          TestHandler.class.getMethod("notAnOutboxMessageHandler", String.class), 0);
        assertThat(resolver.supportsParameter(paramNotAnnotated)).isFalse();
    }

    @Test
    void resolveArgumentWhenOperationMatches() throws Exception {

        Message<byte[]> message = MessageBuilder
          .withPayload(new OutboxDefaultSerializer().serializeToByteArray("The Payload"))
          .setHeader(OPERATION_HEADER, TEST_OPERATION)
          .build();

        Object result = resolver.resolveArgument(paramAnnotated(true, false), message);

        assertThat(result).isEqualTo("The Payload");
    }

    @Test
    void returnNullWhenOperationDoesNotMatch() throws Exception {

        Message<byte[]> message = MessageBuilder.withPayload(new byte[]{1, 2, 3})
          .setHeader(OPERATION_HEADER, "WRONG_OPERATION")
          .build();

        Object result = resolver.resolveArgument(paramAnnotated(true, false), message);

        assertThat(result).isNull();
    }

    @Test
    void resolveArgumentWhenNoOperationDefined() throws Exception {

        Message<byte[]> message = MessageBuilder
          .withPayload(new OutboxDefaultSerializer().serializeToByteArray("The Payload"))
          .build();

        Object result = resolver.resolveArgument(paramAnnotated(false, false), message);

        assertThat(result).isEqualTo("The Payload");
    }

    @Test
    void resolveArgumentToNullWhenPayloadIsEmpty() throws Exception {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();

        Object result = resolver.resolveArgument(paramAnnotated(false, false), message);

        assertThat(result).isNull();
    }

    @Test
    void resolveArgumentToOptional() throws Exception {
        Message<byte[]> message = MessageBuilder
          .withPayload(new OutboxDefaultSerializer().serializeToByteArray("The Payload"))
          .build();

        Object result = resolver.resolveArgument(paramAnnotated(false, true), message);

        assertThat(result).isEqualTo(Optional.of("The Payload"));
    }

    @Test
    void resolveArgumentToOptionalEmpty() throws Exception {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();

        Object result = resolver.resolveArgument(paramAnnotated(false, true), message);

        assertThat(result).isEqualTo(Optional.empty());
    }

    private MethodParameter paramAnnotated(boolean withOperationFilter, boolean paramIsOptional)
      throws NoSuchMethodException {

        Class<?> type = (paramIsOptional ? Optional.class : SerializableEntity.class);

        String methodHandlerName = (withOperationFilter ?
          "annotated" + type.getSimpleName() + "ParamWithOperationDefinedHandler" :
          "annotated" + type.getSimpleName() + "ParamWithoutOperationDefinedHandler");
        Method methodHandler = TestHandler.class.getMethod(methodHandlerName, type);

        return new MethodParameter(methodHandler, 0);
    }

    static class TestHandler {

        public void annotatedSerializableEntityParamWithOperationDefinedHandler(
          @OutboxMessageBody(operation = TEST_OPERATION) SerializableEntity entity) {}

        public void annotatedSerializableEntityParamWithoutOperationDefinedHandler(
          @OutboxMessageBody SerializableEntity entity) {}

        public void annotatedOptionalParamWithOperationDefinedHandler(
          @OutboxMessageBody(operation = TEST_OPERATION) Optional<SerializableEntity> entity) {}

        public void annotatedOptionalParamWithoutOperationDefinedHandler(
          @OutboxMessageBody Optional<SerializableEntity> entity) {}

        public void notAnOutboxMessageHandler(String message) {}
    }

    static class SerializableEntity implements Serializable {}
}
