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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.util.Assert;

/**
 * Execution wrapper for running database operations within a schema-aware context.
 *
 * <p>This bean ensures that the specified schema is created (if necessary) and temporarily set as the active schema during
 * the execution of the provided callback. Once the execution completes, it restores the initial schema context.</p>
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
class OutboxSchemaAwareExecution implements InitializingBean {

    private final EntityManager entityManager;
    private final String schema;
    private final boolean schemaSpecified;

    private DataSource dataSource;
    private String rdbms;


    OutboxSchemaAwareExecution(EntityManager entityManager, String schema) {
        this.entityManager = entityManager;
        this.schema = schema;
        this.schemaSpecified = schema != null && !schema.isEmpty();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.dataSource = ((EntityManagerFactoryInfo) entityManager.getEntityManagerFactory()).getDataSource();
        Assert.notNull(dataSource, "DataSource must not be null");

        this.rdbms = JdbcUtils
          .commonDatabaseName(
            JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName));
        Assert.hasText(this.rdbms, "RDBMS must not be null or empty");
    }

    public void execute(boolean withSchemaCreation, boolean dropExistentOutboxTable, OutboxSchemaAwareCallback callback)
      throws SQLException {

        Assert.notNull(callback, "Callback must not be null");

        if (withSchemaCreation && outboxTableExists() && !dropExistentOutboxTable) {
            return;
        }

        Connection connection = null;
        String initialSchema = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            initialSchema = connection.getSchema();

            if (schemaSpecified) {
                if (withSchemaCreation) {
                    entityManager.createNativeQuery("CREATE SCHEMA IF NOT EXISTS " + schema)
                      .executeUpdate();
                }
                entityManager.createNativeQuery(generateSetSchemaStatement(rdbms, schema)).executeUpdate();
            }

            if (dropExistentOutboxTable) {
                entityManager.createNativeQuery("DROP TABLE IF EXISTS outbox;").executeUpdate();
            }

            callback.execute(new OutboxSchemaAwareContext(entityManager, dataSource, rdbms));

        } finally {
            // back to the initial schema.
            if (initialSchema != null && schemaSpecified) {
                String setSchemaStatement = generateSetSchemaStatement(rdbms, initialSchema);
                entityManager.createNativeQuery(setSchemaStatement).executeUpdate();
            }

            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private boolean outboxTableExists() {

        String queryString = """
          SELECT COUNT(*)
          FROM information_schema.tables
          WHERE table_name = 'outbox'
          """ + (schemaSpecified ? " AND table_schema = '" + schema + "'" : "");

        var query = entityManager.createNativeQuery(queryString);

        Number count = (Number) query.getSingleResult();
        return count != null && count.intValue() > 0;
    }

    private String generateSetSchemaStatement(String rdbms, String schema) {
        if (rdbms.equals("PostgreSQL")) {
            return "SET search_path TO " + schema;
        }
        throw new IllegalArgumentException("Schema setting is not supported for the database: " + rdbms);
    }
}
