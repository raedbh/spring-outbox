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

package io.github.raedbh.spring.outbox.connector.core;

import java.util.Map;
import java.util.Objects;

/**
 * The data model for transmitting outbox data, representing either an event or a command.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public final class OutboxData {

		private final String id;
		private final String type;
		private final byte[] payload;

		private String relatedTo;
		private Map<String, Object> metadata;


		public OutboxData(String id, String type, byte[] payload) {
				this.id = Objects.requireNonNull(id, "Id must not be null");
				this.type = Objects.requireNonNull(type, "Type must not be null");
				this.payload = Objects.requireNonNull(payload, "Payload must not be null");
		}

		public OutboxData(String id, String type, byte[] payload, String relatedTo, Map<String, Object> metadata) {
				this(id, type, payload);
				this.relatedTo = relatedTo;
				this.metadata = metadata;
		}

		public String getId() {
				return id;
		}

		public String getType() {
				return type;
		}

		public byte[] getPayload() {
				return payload;
		}

		public String getRelatedTo() {
				return relatedTo;
		}

		public Map<String, Object> getMetadata() {
				return metadata;
		}
}
