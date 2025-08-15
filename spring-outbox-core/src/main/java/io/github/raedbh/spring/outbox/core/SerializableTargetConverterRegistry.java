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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.converter.Converter;

/**
 * Registry for converters with {@link Serializable} target.
 *
 * <p>This class registers converters that transform an object to a {@link Serializable} target.
 * Only one converter is allowed for each source type to avoid ambiguity. If multiple converters
 * for the same source are registered, an {@link IllegalStateException} is thrown.</p>
 *
 * <p>The registry provides a method to retrieve a converter for a given source type if one exists. Provided
 * converters are primarily used to transform root entities and command objects into transmission structures.</p>
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class SerializableTargetConverterRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializableTargetConverterRegistry.class);

    private final Map<Class<?>, Converter<Object, Serializable>> serializableTargetConverters = new HashMap<>();

    @SuppressWarnings("unchecked")
    public SerializableTargetConverterRegistry(Set<Converter<?, ?>> converters) {
        for (Converter<?, ?> converter : converters) {
            ResolvableType converterType = ResolvableType.forClass(converter.getClass()).as(Converter.class);
            
            if (converterType != ResolvableType.NONE && converterType.hasGenerics()) {
                ResolvableType[] generics = converterType.getGenerics();
                
                if (generics.length == 2) {
                    Class<?> sourceType = generics[0].toClass();
                    Class<?> targetType = generics[1].toClass();
                    
                    if (Serializable.class.isAssignableFrom(targetType)) {
                        if (serializableTargetConverters.putIfAbsent(sourceType,
                          (Converter<Object, Serializable>) converter) != null) {
                            throw new IllegalStateException(
                              "Multiple converters found for source type: " + sourceType.getName());
                        }

                        LOGGER.info(
                          "Registered converter with a serializable target for source type: {}",
                          sourceType.getName());
                    }
                }
            }
        }
    }

    Optional<Converter<Object, Serializable>> getConverter(Class<?> sourceType) {
        return Optional.ofNullable(serializableTargetConverters.get(sourceType));
    }
}
