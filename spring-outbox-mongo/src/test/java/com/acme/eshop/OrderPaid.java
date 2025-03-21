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

package com.acme.eshop;

import io.github.raedbh.spring.outbox.core.CommandOutboxed;
import io.github.raedbh.spring.outbox.core.EventOutboxed;

/**
 * @author Raed Ben Hamouda
 */
public class OrderPaid extends EventOutboxed<Order> {

    public OrderPaid(Order source) {
        super(source);
    }

    public OrderPaid(Order source, CommandOutboxed cmd) {
        super(source, cmd);
    }

    @Override
    public String getOperation() {
        return "payment";
    }
}
