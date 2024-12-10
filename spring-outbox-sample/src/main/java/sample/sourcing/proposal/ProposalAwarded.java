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

package sample.sourcing.proposal;

import io.github.raedbh.spring.outbox.core.EventOutboxed;

/**
 * @author Raed Ben Hamouda
 */
public class ProposalAwarded extends EventOutboxed<Proposal> {

		public ProposalAwarded(Proposal source) {
				super(source);
		}

		@Override
		public String getOperation() {
				return "award";
		}
}
