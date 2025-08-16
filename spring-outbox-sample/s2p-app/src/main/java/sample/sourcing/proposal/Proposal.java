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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.money.MonetaryAmount;
import jakarta.persistence.Table;

import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.springframework.util.Assert;

import io.github.raedbh.spring.outbox.core.RootEntity;
import sample.common.Currencies;
import sample.common.EntityIdentifier;
import sample.email.EmailNotification;
import sample.sourcing.rfp.RequestForProposal;
import sample.vendor.Vendor;

/**
 * @author Raed Ben Hamouda
 */
@Table(name = "proposals")
public class Proposal extends RootEntity implements AggregateRoot<Proposal, EntityIdentifier> {

    private final EntityIdentifier id;
    private final Association<RequestForProposal, EntityIdentifier> rfp;
    private final Association<Vendor, EntityIdentifier> vendor;
    private final String details;
    private final MonetaryAmount proposalAmount;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewStartedAt;
    private Status status;


    public Proposal(RequestForProposal rfp, Vendor vendor, String details, BigDecimal proposalAmount) {
        this.id = EntityIdentifier.generate();
        this.rfp = Association.forAggregate(rfp);
        this.vendor = Association.forAggregate(vendor);
        this.details = details;
        this.proposalAmount = Money.of(proposalAmount, Currencies.EURO);
        this.submittedAt = null;
        this.reviewStartedAt = null;
        this.status = Status.CREATED;
    }


    public Proposal markSubmitted() {
        return markSubmittedAt(LocalDateTime.now());
    }

    public Proposal markSubmittedAt(LocalDateTime submittedAt) {
        Assert.state(this.status == Status.CREATED,
          "The proposal can be submitted only after being created. Current status: " + this.status);

        this.status = Status.SUBMITTED;
        this.submittedAt = submittedAt;
        return this;
    }

    public Proposal markReviewStarted() {
        return markReviewStartedAt(LocalDateTime.now());
    }

    public Proposal markReviewStartedAt(LocalDateTime reviewStartedAt) {
        Assert.state(this.status == Status.SUBMITTED,
          "The proposal review can be started only after the proposal is submitted. Current status: " + this.status);

        this.status = Status.UNDER_REVIEW;
        this.reviewStartedAt = reviewStartedAt;
        return this;
    }

    /**
     * Mark the proposal as awarded, indicating it has been selected as the winning bid.
     *
     * @param vendorContact the contact information of the vendor to notify about the award
     */
    public Proposal markAwarded(VendorContact vendorContact) {
        Assert.state(this.status == Status.UNDER_REVIEW,
          "Cannot award a proposal that is not under review! Current status: " + this.status);
        this.status = Status.AWARDED;

        // create email notification command
        Map<String, Serializable> templateParams = new HashMap<>();
        templateParams.put("rfpId", this.rfp.getId().toString());
        templateParams.put("proposalId", this.id.toString());
        templateParams.put("vendorId", this.vendor.getId().toString());
        templateParams.put("proposalAmount", this.proposalAmount.getNumber()
          .numberValueExact(BigDecimal.class)
          .toPlainString());

        EmailNotification emailCommand = new EmailNotification("proposal-awarded", vendorContact, templateParams);

        // assign event with command to current proposal
        assignEvent(new ProposalAwarded(this, emailCommand));

        return this;
    }

    public Proposal markRejected() {
        Assert.state(this.status != Status.AWARDED, "An awarded proposal cannot be rejected!");
        this.status = Status.REJECTED;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Proposal proposal = (Proposal) obj;
        return Objects.equals(id, proposal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public EntityIdentifier getId() {
        return id;
    }

    public Association<RequestForProposal, EntityIdentifier> getRfp() {
        return rfp;
    }

    public Association<Vendor, EntityIdentifier> getVendor() {
        return vendor;
    }

    public String getDetails() {
        return details;
    }

    public MonetaryAmount getProposalAmount() {
        return proposalAmount;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewStartedAt() {
        return reviewStartedAt;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {

        /**
         * The proposal has been created but not yet submitted by the vendor.
         */
        CREATED,

        /**
         * The proposal has been submitted by a vendor and is awaiting review.
         */
        SUBMITTED,

        /**
         * The proposal is under review and being evaluated by the purchasing department.
         */
        UNDER_REVIEW,

        /**
         * The proposal has been awarded as the winning bid.
         */
        AWARDED,

        /**
         * The proposal has been rejected after review.
         */
        REJECTED
    }

    /**
     * Value object representing vendor contact information for email notifications.
     */
    public record VendorContact(String name, String email) implements java.io.Serializable {}
}
