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

package io.github.raedbh.spring.outbox.jpa;

import jakarta.persistence.EntityManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.raedbh.spring.outbox.core.OutboxCoreConfiguration;
import io.github.raedbh.spring.outbox.core.OutboxRepository;

/**
 * Auto-configuration for JPA-based transactional outbox support.
 *
 * <p>Registers necessary beans for managing outbox entries in a relational database, including the
 * {@link OutboxRepository} and optional {@link OutboxTableSchemaInitializer}.</p>
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@AutoConfiguration(before = HibernateJpaAutoConfiguration.class)
@Import(OutboxCoreConfiguration.class)
@EnableConfigurationProperties(RdbmsConfigurationProperties.class)
public class OutboxJpaAutoConfiguration {

		@Bean
		static BeanPostProcessor entityManagerFactoryBeanPostProcessor() {
				return new BeanPostProcessor() {
						@Override
						public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
								if (bean instanceof LocalContainerEntityManagerFactoryBean factoryBean) {
										factoryBean.setPersistenceUnitPostProcessors(pui -> {
												String name = JpaOutboxEntry.class.getName();
												pui.addManagedClassName(name);
										});
								}
								return bean;
						}
				};
		}

		@Bean
		OutboxSchemaAwareExecution outboxSchemaAwareExecution(EntityManager entityManager,
				RdbmsConfigurationProperties rdbmsConfigProperties) {
				return new OutboxSchemaAwareExecution(entityManager, rdbmsConfigProperties.getSchema());
		}

		@Bean
		OutboxRepository outboxRepository(OutboxSchemaAwareExecution outboxSchemaAwareExecution) {
				return new JpaOutboxRepository(outboxSchemaAwareExecution);
		}

		@Bean
		@ConditionalOnProperty(prefix = "spring.outbox.rdbms", name = "auto-create", havingValue = "true")
		OutboxTableSchemaInitializer outboxTableSchemaInitializer(ResourceLoader resourceLoader,
				TransactionTemplate transactionTemplate,
				OutboxSchemaAwareExecution outboxSchemaAwareExecution,
				HibernateProperties hibernateProperties) {

				return new OutboxTableSchemaInitializer(resourceLoader,
						transactionTemplate,
						outboxSchemaAwareExecution,
						hibernateProperties);
		}
}
