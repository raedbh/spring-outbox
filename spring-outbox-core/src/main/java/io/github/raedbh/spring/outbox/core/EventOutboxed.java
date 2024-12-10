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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An abstract class representing an 'outboxed' event for a given domain {@link RootEntity}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public abstract class EventOutboxed<S extends RootEntity> {

		private final String name;
		private final long occurredAt;
		private final S source;
		private List<CommandOutboxed> commands = new ArrayList<>();


		/**
		 * Creates a new event object for a specified source entity.
		 *
		 * @param source the entity on which the event initially occurred; must not be {@code null}.
		 * @throws IllegalArgumentException if the source is {@code null}.
		 */
		protected EventOutboxed(S source) {
				if (source == null) {
						throw new IllegalArgumentException("source must not be null");
				}

				this.source = source;
				this.occurredAt = System.currentTimeMillis();
				this.name = getClass().getSimpleName();
		}

		/**
		 * Creates a new event object for a specified source entity.
		 *
		 * @param source the entity on which the event initially occurred; must not be {@code null}.
		 * @param command a post-event-publication commands.
		 * @throws IllegalArgumentException if the source is {@code null}.
		 */
		protected EventOutboxed(S source, CommandOutboxed command) {
				this(source);
				addCommand(command);
		}


		public String getName() {
				return name;
		}

		/**
		 * The object on which the Event initially occurred.
		 *
		 * @return the object on which the Event initially occurred
		 */
		public final S getSource() {
				return source;
		}

		/**
		 * Return the time in milliseconds when the event occurred.
		 */
		public final long getOccurredAt() {
				return this.occurredAt;
		}

		/**
		 * @return the operation which is represented by a verb or a noun indicating the action, such as creation,
		 * 	       update, remove, publish, etc.
		 */
		public abstract String getOperation();

		/**
		 * Register new {@link CommandOutboxed} related to the current domain event.
		 *
		 * @param command the command to be triggered after the event is published; must not be {@code null}.
		 */
		public void addCommand(CommandOutboxed command) {
				if (command == null) {
						throw new IllegalArgumentException("Command must not be null!");
				}
				this.commands.add(command);
		}

		/**
		 * Sets the commands related to this event, replacing any existing command list.
		 *
		 * @param commands the commands to be associated with the event; must not be {@code null}.
		 */
		public void addCommands(CommandOutboxed... commands) {
				if (commands == null) {
						throw new IllegalArgumentException("Commands must not be null!");
				}

				if (Stream.of(commands).anyMatch(Objects::isNull)) {
						throw new IllegalArgumentException("Command must not be null!");
				}

				this.commands = new ArrayList<>(List.of(commands));
		}

		public final List<CommandOutboxed> getCommands() {
				return commands;
		}
}
