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

package io.github.raedbh.spring.outbox.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OutboxCoreConfiguration}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxCoreConfigurationTests {

    @Test
    void autoConfigureWithDefaults() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(OutboxManager.class);
            assertThat(context).hasSingleBean(OutboxDefaultSerializer.class);
            assertThat(context).hasSingleBean(OutboxDefaultSerializer.class);
            assertThat(context).hasSingleBean(SerializableTargetConverterRegistry.class);
        });
    }

    @Test
    void useCustomSerializerWhenProvided() {
        contextRunner().withUserConfiguration(CustomSerializerConfiguration.class)
          .run(context -> {
              Serializer<?> serializer = context.getBean(Serializer.class);
              assertThat(serializer).isInstanceOf(CustomSerializer.class);
          });
    }

    @Test
    void useCustomDeserializerWhenProvided() {
        contextRunner().withUserConfiguration(CustomDeserializerConfiguration.class)
          .run(context -> {
              Deserializer<?> deserializer = context.getBean(Deserializer.class);
              assertThat(deserializer).isInstanceOf(CustomDeserializer.class);
          });
    }

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(OutboxCoreConfiguration.class))
          .withBean(OutboxRepository.class, () -> Mockito.mock(OutboxRepository.class))
          .withBean(PlatformTransactionManager.class, () -> Mockito.mock(PlatformTransactionManager.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomSerializerConfiguration {

        @Bean
        Serializer<Serializable> outboxSerializer() {
            return new CustomSerializer();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomDeserializerConfiguration {

        @Bean
        Deserializer<Serializable> outboxDeserializer() {
            return new CustomDeserializer();
        }
    }

    static class CustomSerializer implements Serializer<Serializable> {

        @Override
        public void serialize(Serializable object, OutputStream outputStream) {
            // Custom implementation
        }
    }

    static class CustomDeserializer implements Deserializer<Serializable> {

        @Override
        public Serializable deserialize(InputStream inputStream) {
            // Custom implementation
            return null;
        }
    }
}
