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

package sample.common;

import java.util.UUID;

import org.jmolecules.ddd.types.Identifier;

/**
 * @author Raed Ben Hamouda
 */
public record EntityIdentifier(UUID id) implements Identifier {

		public static EntityIdentifier generate() {
				return new EntityIdentifier(UUID.randomUUID());
		}

		public static EntityIdentifier fromString(String id) {
				if (id == null) {
						throw new IllegalArgumentException("Id cannot be null");
				}
				return new EntityIdentifier(UUID.fromString(id));
		}

		@Override
		public String toString() {
				return id == null ? null : id.toString();
		}
}
