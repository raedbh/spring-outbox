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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import io.github.raedbh.spring.outbox.core.OutboxManager;
import io.github.raedbh.spring.outbox.core.RootEntity;

/**
 * Factory bean for JPA repositories implementing the outbox pattern.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxJpaRepositoryFactoryBean<T extends Repository<S, I>, S, I> extends
		JpaRepositoryFactoryBean<T, S, I> {

		private static final Logger LOGGER = LoggerFactory.getLogger(OutboxJpaRepositoryFactoryBean.class);

		private OutboxManager outboxManager;

		public OutboxJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
				super(repositoryInterface);
		}

		@Autowired
		void setOutboxManager(OutboxManager outboxManager) {
				this.outboxManager = outboxManager;
		}

		@Override
		public void afterPropertiesSet() {
				addRepositoryFactoryCustomizer(factory ->
						factory.addRepositoryProxyPostProcessor((proxyFactory, repositoryInformation) -> {
								if (ClassUtils.isAssignable(RootEntity.class, repositoryInformation.getDomainType())) {
										proxyFactory.addAdvice(new StateChangingMethodInterceptor(outboxManager));
								}
						}));

				super.afterPropertiesSet();
		}

		static class StateChangingMethodInterceptor implements MethodInterceptor {

				private final OutboxManager outboxManager;

				StateChangingMethodInterceptor(OutboxManager outboxManager) {
						this.outboxManager = outboxManager;
				}

				private static boolean stateChangingMethod(Method method) {
						return method.getParameterCount() == 1 &&
								(method.getName().equals("save") || method.getName().equals("delete"));
				}

				@Override
				@Nullable
				public Object invoke(MethodInvocation invocation) throws Throwable {
						if (!stateChangingMethod(invocation.getMethod())) {
								return invocation.proceed();
						}
						RootEntity rootEntity = (RootEntity) invocation.getArguments()[0];
						if (rootEntity.withNoEventAssigned()) {
								return invocation.proceed();
						}
						return outboxManager.proceedInvocationAndSaveOutboxEntries(rootEntity, () -> {
								try {
										LOGGER.info("Proceeding method invocation: {}", invocation.getMethod().getName());
										return invocation.proceed();
								} catch (Throwable e) {
										throw new RuntimeException(e);
								}
						});
				}
		}
}
