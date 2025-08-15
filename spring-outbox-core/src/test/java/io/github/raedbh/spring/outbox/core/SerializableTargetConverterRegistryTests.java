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
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import com.acme.eshop.Order;
import com.acme.eshop.OrderMessageBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SerializableTargetConverterRegistry}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class SerializableTargetConverterRegistryTests {

    SerializableTargetConverterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SerializableTargetConverterRegistry(Set.of(
          new OrderToMessageBodyConverter(),
          new BigDecimalToDoubleConverter(),
          new StringToIntegerConverter()
        ));
    }

    @Test
    void returnConverterForSerializableTarget() {

        Optional<Converter<Object, Serializable>> converter = registry.getConverter(Order.class);

        assertThat(converter).isPresent();

        Order order = new Order();
        Serializable result = converter.get().convert(order);

        assertThat(result).isInstanceOf(OrderMessageBody.class);
        assertThat(((OrderMessageBody) result).orderId).isEqualTo(order.getId().toString());
    }

    @Test
    void returnEmptyWhenNoConverterIsRegisteredForGivenSourceType() {
        assertThat(registry.getConverter(Target.class)).isEmpty();
    }

    @Test
    void rejectMultipleConvertersForSameSourceType() {

        Set<Converter<?, ?>> converters = Set.of(
          new BigDecimalToDoubleConverter(),
          new BigDecimalToStringConverter(),
          new StringToIntegerConverter()
        );

        assertThatThrownBy(() -> new SerializableTargetConverterRegistry(converters))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Multiple converters found");
    }

    @Test
    void detectConverterInterfaceInComplexInheritanceHierarchy() {
        // test for MapStruct-like generated classes with complex inheritance
        SerializableTargetConverterRegistry registryWithComplexConverter =
          new SerializableTargetConverterRegistry(Set.of(new ComplexInheritanceConverter()));

        Optional<Converter<Object, Serializable>> converter = registryWithComplexConverter.getConverter(Source.class);

        assertThat(converter).isPresent();

        Source source = new Source("test");
        Serializable result = converter.get().convert(source);

        assertThat(result).isInstanceOf(SerializableTarget.class);
        assertThat(((SerializableTarget) result).value()).isEqualTo("converted-test");
    }

    static class OrderToMessageBodyConverter implements Converter<Order, OrderMessageBody> {

        @Override
        public OrderMessageBody convert(Order order) {
            return new OrderMessageBody(order.getId().toString());
        }
    }

    static class BigDecimalToStringConverter implements Converter<BigDecimal, String> {

        @Override
        public String convert(BigDecimal source) {
            return source.toString();
        }
    }

    static class BigDecimalToDoubleConverter implements Converter<BigDecimal, Double> {

        @Override
        public Double convert(BigDecimal source) {
            return source.doubleValue();
        }
    }

    static class StringToIntegerConverter implements Converter<String, Integer> {

        @Override
        public Integer convert(String source) {
            return 1234;
        }
    }

    static class Target implements Serializable {}

    // test classes for complex inheritance hierarchy scenario (like MapStruct)
    record Source(String value) {}

    record SerializableTarget(String value) implements Serializable {}

    // simulates MapStruct-generated converter with complex inheritance
    abstract static class AbstractConverter {
        // abstract base class that doesn't implement Converter
    }

    static class ComplexInheritanceConverter extends AbstractConverter implements
      Converter<Source, SerializableTarget> {

        @Override
        public SerializableTarget convert(Source source) {
            return new SerializableTarget("converted-" + source.value());
        }
    }
}
