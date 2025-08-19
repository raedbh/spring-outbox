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

package sample.sourcing.proposal;

import java.util.List;

import org.jmolecules.ddd.types.Association;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import sample.common.EntityIdentifier;
import sample.email.EmailNotification.Contact;
import sample.sourcing.rfp.RequestForProposal;

/**
 * @author Raed Ben Hamouda
 */
public interface Proposals extends CrudRepository<Proposal, EntityIdentifier> {

    default List<Proposal> findByRfp(EntityIdentifier rfpId) {
        return findByRfpOrderBySubmittedAt(Association.forId(rfpId));
    }

    List<Proposal> findByRfpOrderBySubmittedAt(Association<RequestForProposal, EntityIdentifier> rfp);

    /**
     * Award a proposal for a given RFP and mark it as the winning bid.
     * <p>When a proposal is awarded, all other proposals associated with the same RFP are automatically marked as rejected.</p>
     *
     * @param proposalId the ID of the proposal to award.
     * @param vendorContact the contact information of the vendor whose proposal is being awarded.
     */
    @Transactional
    default void award(EntityIdentifier proposalId, Contact vendorContact) {
        Proposal awardedProposal = proposalById(proposalId);
        List<Proposal> allProposals = findByRfpOrderBySubmittedAt(awardedProposal.getRfp());
        allProposals.forEach(proposal -> {
            if (proposal.equals(awardedProposal)) {
                proposal.markAwarded(vendorContact);
            } else {
                proposal.markRejected();
            }
            save(proposal);
        });
    }

    @Transactional
    default void submit(EntityIdentifier proposalId) {
        proposalById(proposalId).markSubmitted();
    }

    @Transactional
    default void startReview(EntityIdentifier proposalId) {
        proposalById(proposalId).markReviewStarted();
    }

    private Proposal proposalById(EntityIdentifier proposalId) {
        return findById(proposalId).orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
    }
}
