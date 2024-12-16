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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import com.acme.eshop.Application;

import io.github.raedbh.spring.outbox.core.OutboxRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OutboxJpaAutoConfiguration}.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxJpaAutoConfigurationTests {

    @SpringBootTest(classes = Application.class)
    static class TestBase {

        @Autowired
        protected ApplicationContext context;

    }

    @Nested
    class WithDefaults extends TestBase {

        @Test
        void noSchemaInitializer() {
            assertThat(context.getBean(OutboxRepository.class)).isNotNull();
            assertThat(context.getBeansOfType(OutboxTableSchemaInitializer.class)).isEmpty();
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.outbox.rdbms.auto-create=true")
    class WithAutoCreateSchemaEnabled extends TestBase {

        @Test
        void createSchemaInitializer() {
            assertThat(context.getBean(OutboxRepository.class)).isNotNull();
            assertThat(context.getBean(OutboxTableSchemaInitializer.class)).isNotNull();
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.outbox.rdbms.auto-create=false")
    class WithSchemaDisabled extends TestBase {

        @Test
        void noSchemaInitializer() {
            assertThat(context.getBean(OutboxRepository.class)).isNotNull();
            assertThat(context.getBeansOfType(OutboxTableSchemaInitializer.class)).isEmpty();
        }
    }
}
