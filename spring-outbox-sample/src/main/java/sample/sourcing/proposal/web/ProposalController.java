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

package sample.sourcing.proposal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sample.common.EntityIdentifier;
import sample.sourcing.proposal.Proposal;
import sample.sourcing.proposal.Proposals;
import sample.sourcing.rfp.RequestForProposal;
import sample.sourcing.rfp.RfpRepository;
import sample.vendor.Vendor;
import sample.vendor.Vendors;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

/**
 * @author Raed Ben Hamouda
 */
@Controller
@RequestMapping("/proposals")
public class ProposalController {

    final Proposals proposals;
    final Vendors vendors;
    final RfpRepository rfpRepository;


    public ProposalController(Proposals proposals, Vendors vendors, RfpRepository rfpRepository) {
        this.proposals = proposals;
        this.vendors = vendors;
        this.rfpRepository = rfpRepository;
    }


    @PostMapping
    public ResponseEntity<String> createProposal(@RequestBody ProposalRequest request) {

        Proposal proposal = new Proposal(rfpById(request.rfpId),
          vendorById(request.vendorId),
          request.details,
          request.amount);
        proposals.save(proposal);

        return ResponseEntity.created(fromCurrentRequest().path("/{id}")
          .buildAndExpand(proposal.getId()).toUri()).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proposal> getProposal(@PathVariable EntityIdentifier id) {
        Proposal proposal = proposals.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Proposal: " + id + "Not Found"));
        return ResponseEntity.ok(proposal);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> createProposal(@PathVariable("id") String proposalId) {
        proposals.submit(EntityIdentifier.fromString(proposalId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start-review")
    public ResponseEntity<?> reviewProposal(@PathVariable("id") String proposalId) {
        proposals.startReview(EntityIdentifier.fromString(proposalId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/award")
    public ResponseEntity<?> awardProposal(@PathVariable("id") String proposalId) {
        proposals.award(EntityIdentifier.fromString(proposalId));
        return ResponseEntity.ok().build();
    }

    // The search endpoint may expand to include additional query fields in the future
    @GetMapping("search")
    public ResponseEntity<?> getProposals(@RequestParam("rfpId") RequestForProposal rfp) {
        return ResponseEntity.ok(this.proposals.findByRfp(rfp.getId()));
    }

    private RequestForProposal rfpById(EntityIdentifier rfpId) {
        return rfpRepository.findById(rfpId)
          .orElseThrow(() -> new IllegalArgumentException("Rfp: " + rfpId + " not found"));
    }

    private Vendor vendorById(EntityIdentifier vendorId) {
        return vendors.findById(vendorId)
          .orElseThrow(() -> new IllegalArgumentException("Vendor: " + vendorId + " not found"));
    }
}
