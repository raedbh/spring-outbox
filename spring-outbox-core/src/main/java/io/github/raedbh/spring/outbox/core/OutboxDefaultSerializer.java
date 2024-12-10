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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Serializer;

/**
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxDefaultSerializer implements Serializer<Serializable> {

		private final DefaultSerializer defaultSerializer = new DefaultSerializer();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void serialize(Serializable object, OutputStream outputStream) throws IOException {
				defaultSerializer.serialize(object, outputStream);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public byte[] serializeToByteArray(Serializable object) throws IOException {
				try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
						defaultSerializer.serialize(object, outputStream);
						return outputStream.toByteArray();
				}
		}
}
