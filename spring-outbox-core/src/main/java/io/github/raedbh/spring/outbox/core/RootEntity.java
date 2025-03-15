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

import java.io.Serializable;

import org.springframework.data.annotation.Transient;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Base class for root entities, commonly known as aggregate roots in Domain Driven Design.
 *
 * <p>Enables assigning an {@link EventOutboxed} to this root entity.</p>
 *
 * @author Raed Ben Hamouda
 * @since 1.0
 */
public abstract class RootEntity implements Identifiable, Serializable {

    @Nullable
    @Transient
    transient private EventOutboxed<? extends RootEntity> event;

    public abstract Object getId();

    public void assignEvent(EventOutboxed<? extends RootEntity> event) {
        Assert.notNull(event, "Event must not be null");
        Assert.state(this.event == null, "An event has already been assigned for this entity");

        this.event = event;
    }

    @Nullable
    EventOutboxed<? extends RootEntity> event() {
        return event;
    }

    public boolean withNoEventAssigned() {
        return this.event == null;
    }
}
