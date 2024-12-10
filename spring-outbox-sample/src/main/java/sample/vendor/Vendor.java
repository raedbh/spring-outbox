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

package sample.vendor;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

import org.jmolecules.ddd.types.AggregateRoot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import sample.common.EntityIdentifier;

/**
 * @author Raed Ben Hamouda
 */
@Table(name = "vendors")
@JsonIgnoreProperties(value = "new")
public class Vendor implements AggregateRoot<Vendor, EntityIdentifier> {

		private final EntityIdentifier id;

		@Column(unique = true)
		private final String name;


		public Vendor(String name) {
				this.id = EntityIdentifier.generate();
				this.name = name;
		}


		@Override
		public EntityIdentifier getId() {
				return id;
		}

		public String getName() {
				return name;
		}
}
