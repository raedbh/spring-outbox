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

package sample.sourcing.common;

import java.io.Serializable;

/**
 * @author Raed Ben Hamouda
 */
public class ProposalMessageBody implements Serializable {

		public String id;
		public String rfpId;
		public String vendorId;
		public String status;


		public ProposalMessageBody() {
		}

		public ProposalMessageBody(String id, String rfpId, String vendorId, String status) {
				this.id = id;
				this.rfpId = rfpId;
				this.vendorId = vendorId;
				this.status = status;
		}
}
