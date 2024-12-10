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

package sample.sourcing.rfp;

import jakarta.persistence.Table;

import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Entity;

import sample.common.EntityIdentifier;

/**
 * @author Raed Ben Hamouda
 */
@Table(name = "requirements")
public class Requirement implements Entity<RequestForProposal, EntityIdentifier> {

		public final EntityIdentifier id;
		public final Association<RequirementLabel, EntityIdentifier> labelId;
		public final String description;


		public Requirement(EntityIdentifier labelId, String description) {
				this.id = EntityIdentifier.generate();
				this.labelId = Association.forId(labelId);
				this.description = description;
		}


		@Override
		public EntityIdentifier getId() {
				return id;
		}

		public String getDescription() {
				return description;
		}
}
