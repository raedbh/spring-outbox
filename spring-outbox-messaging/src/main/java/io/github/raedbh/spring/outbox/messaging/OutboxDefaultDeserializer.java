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

package io.github.raedbh.spring.outbox.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.Deserializer;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxDefaultDeserializer implements Deserializer<Serializable> {

    private final DefaultDeserializer defaultDeserializer = new DefaultDeserializer();

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable deserialize(InputStream inputStream) throws IOException {
        return (Serializable) defaultDeserializer.deserialize(inputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable deserializeFromByteArray(byte[] serialized) throws IOException {
        return (Serializable) defaultDeserializer.deserializeFromByteArray(serialized);
    }
}