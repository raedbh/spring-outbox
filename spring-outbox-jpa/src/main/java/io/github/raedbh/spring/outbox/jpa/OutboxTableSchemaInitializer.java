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

import java.sql.SQLException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Initializes the database schema for the outbox table.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxTableSchemaInitializer implements InitializingBean {

    private final ResourceLoader resourceLoader;
    private final TransactionTemplate transactionTemplate;
    private final OutboxSchemaAwareExecution outboxSchemaAwareExecution;
    private final boolean dropExistentOutboxTable;


    OutboxTableSchemaInitializer(ResourceLoader resourceLoader,
      TransactionTemplate transactionTemplate,
      OutboxSchemaAwareExecution outboxSchemaAwareExecution,
      HibernateProperties hibernateProperties) {

        this.resourceLoader = resourceLoader;
        this.transactionTemplate = transactionTemplate;
        this.outboxSchemaAwareExecution = outboxSchemaAwareExecution;
        this.dropExistentOutboxTable = "create".equals(hibernateProperties.getDdlAuto()) ||
          "create-drop".equals(hibernateProperties.getDdlAuto());
    }


    @Override
    public void afterPropertiesSet() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    outboxSchemaAwareExecution.execute(true, dropExistentOutboxTable, context -> {
                        String location = ResourceLoader.CLASSPATH_URL_PREFIX + "/create-outbox-table-" +
                          context.rdbms().toLowerCase() + ".sql";
                        new ResourceDatabasePopulator(resourceLoader.getResource(location))
                          .execute(context.dataSource());
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
