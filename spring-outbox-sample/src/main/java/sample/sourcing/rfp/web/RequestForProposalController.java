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

package sample.sourcing.rfp.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import sample.common.EntityIdentifier;
import sample.sourcing.rfp.RequestForProposal;
import sample.sourcing.rfp.Requirement;
import sample.sourcing.rfp.RequirementLabelRepository;
import sample.sourcing.rfp.RfpManagement;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

/**
 * @author Raed Ben Hamouda
 */
@Controller
@RequestMapping("/rfps")
public class RequestForProposalController {

		final RfpManagement rfpManagement;
		final RequirementLabelRepository labelRepository;


		public RequestForProposalController(RfpManagement rfpManagement, RequirementLabelRepository labelRepository) {
				this.rfpManagement = rfpManagement;
				this.labelRepository = labelRepository;
		}


		@PostMapping
		public ResponseEntity<String> createRfp(@RequestBody CreateRfpRequest request) {

				RequestForProposal rfp = new RequestForProposal(request.title, request.description, request.submissionDeadline,
						request.requirements.stream().map(this::toRequirementEntity).toList());

				rfpManagement.save(rfp);

				return ResponseEntity.created(fromCurrentRequest().path("/{id}").buildAndExpand(rfp.getId()).toUri()).build();
		}

		@GetMapping("/{id}")
		public ResponseEntity<RequestForProposal> getRfp(@PathVariable("id") String rfpId) {
				RequestForProposal rfp = rfpManagement.find(EntityIdentifier.fromString(rfpId));
				return ResponseEntity.ok(rfp);
		}

		@PostMapping("/{id}/publish")
		public ResponseEntity<String> publishRfp(@PathVariable("id") String rfpId) {
				rfpManagement.publish(EntityIdentifier.fromString(rfpId));
				return ResponseEntity.ok().build();
		}

		private Requirement toRequirementEntity(CreateRfpRequest.Requirement requirement) {
				EntityIdentifier id = EntityIdentifier.fromString(requirement.id);
				labelRepository.findById(id)
						.orElseThrow(() -> new IllegalArgumentException("Label with ID: " + id + "Not Found"));
				return new Requirement(id, requirement.description);
		}
}
