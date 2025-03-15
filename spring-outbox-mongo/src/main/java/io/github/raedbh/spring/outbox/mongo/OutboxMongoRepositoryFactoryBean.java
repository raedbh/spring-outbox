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

package io.github.raedbh.spring.outbox.mongo;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.util.ClassUtils;

import io.github.raedbh.spring.outbox.core.OutboxManager;
import io.github.raedbh.spring.outbox.core.RootEntity;
import io.github.raedbh.spring.outbox.core.StateChangingMethodInterceptor;

/**
 * Factory bean for MongoDB repositories implementing the outbox pattern.
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public class OutboxMongoRepositoryFactoryBean<T extends Repository<S, I>, S, I extends Serializable> extends
  MongoRepositoryFactoryBean<T, S, I> {

    private OutboxManager outboxManager;


    public OutboxMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
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
}
