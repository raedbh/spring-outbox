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

package io.github.raedbh.spring.outbox.connector.rabbit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

/**
 * Provides configuration for RabbitMQ integration based on outbox type.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class RabbitConfigProvider {

		private static final String CONFIG_PREFIX = "spring.outbox.connector.rabbit.messages";

		private final Map<String, String> configCache;
		private final Environment environment;


		RabbitConfigProvider(Environment environment) {
				this.environment = environment;
				this.configCache = new HashMap<>();
		}


		private static String toKebabCase(String input) {
				return input.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
		}

		RabbitConfig rabbitConfig(String outboxType) {
				String typeKey = toKebabCase(outboxType);

				String routingKey = getConfigValue(typeKey, "routing-key");
				String exchange = getConfigValue(typeKey, "exchange");

				return new RabbitConfig(routingKey, exchange);
		}

		@Nullable
		private String getConfigValue(String typeKey, String configName) {
				String propertyKey = "%s.%s.%s".formatted(RabbitConfigProvider.CONFIG_PREFIX, typeKey, configName);
				return configCache.computeIfAbsent(propertyKey, environment::getProperty);
		}
}
