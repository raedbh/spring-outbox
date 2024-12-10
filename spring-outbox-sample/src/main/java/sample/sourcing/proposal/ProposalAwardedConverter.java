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

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import sample.sourcing.common.ProposalMessageBody;

/**
 * @author Raed Ben Hamouda
 */
@Component
public class ProposalAwardedConverter implements Converter<Proposal, ProposalMessageBody> {

		@Override
		public ProposalMessageBody convert(Proposal from) {
				return new ProposalMessageBody(from.getId().toString(),
						from.getRfp().getId().toString(),
						from.getVendor().getId().toString(),
						from.getStatus().toString());
		}
}
