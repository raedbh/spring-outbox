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

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acme.eshop.Order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link StateChangingMethodInterceptor}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class StateChangingMethodInterceptorTests {

    @Mock OutboxManager outboxManager;
    @Mock MethodInvocation invocation;

    @Test
    void interceptSaveInvocationForOutboxManagement() throws Throwable {
        Order order = mock(Order.class);
        given(order.withNoEventAssigned()).willReturn(false);

        Method method = OrderRepository.class.getMethod("save", Order.class);
        given(invocation.getMethod()).willReturn(method);
        given(invocation.getArguments()).willReturn(new Object[]{order});

        new StateChangingMethodInterceptor(outboxManager).invoke(invocation);

        verify(outboxManager).proceedInvocationAndSaveOutboxEntries(eq(order), any());
    }

    @Test
    void skipForNonStateChangingMethodInvocation() throws Throwable {

        Method method = OrderRepository.class.getMethod("findById", String.class);
        given(invocation.getMethod()).willReturn(method);

        new StateChangingMethodInterceptor(outboxManager).invoke(invocation);

        verify(invocation).proceed();
        verifyNoInteractions(outboxManager);
    }

    @Test
    void skipWhenRootEntityHasNoAssignedEvents() throws Throwable {

        Order order = new Order();
        Method method = OrderRepository.class.getMethod("save", Order.class);

        given(invocation.getMethod()).willReturn(method);
        given(invocation.getArguments()).willReturn(new Object[]{order});

        new StateChangingMethodInterceptor(outboxManager).invoke(invocation);

        verify(invocation).proceed();
        verifyNoInteractions(outboxManager);
    }

    interface OrderRepository {

        void save(Order order);

        Order findById(String id);
    }
}
