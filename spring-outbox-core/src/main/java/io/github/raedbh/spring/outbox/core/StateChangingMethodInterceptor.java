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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * An AOP method interceptor that intercepts state-changing methods on {@link RootEntity} and saves outbox entries.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class StateChangingMethodInterceptor implements MethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateChangingMethodInterceptor.class);

    private final OutboxManager outboxManager;

    public StateChangingMethodInterceptor(OutboxManager outboxManager) {
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
