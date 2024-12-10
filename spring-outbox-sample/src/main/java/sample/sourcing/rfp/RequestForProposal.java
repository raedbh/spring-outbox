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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.github.raedbh.spring.outbox.core.RootEntity;
import sample.common.EntityIdentifier;

/**
 * Request For Proposal Aggregate Root.
 *
 * @author Raed Ben Hamouda
 */
@Table(name = "rfps")
@EntityListeners(AuditingEntityListener.class)
public class RequestForProposal extends RootEntity implements AggregateRoot<RequestForProposal, EntityIdentifier> {

		private final EntityIdentifier id;

		private final String title;

		private final String description;

		private final LocalDateTime submissionDeadline;

		@OrderColumn
		@JoinColumn(name = "rfp_id")
		private final List<Requirement> requirements = new ArrayList<>();

		@CreatedDate
		private LocalDateTime createdAt;

		@LastModifiedDate
		private LocalDateTime lastUpdatedAt;

		private LocalDateTime publishedAt;

		private Status status;

		private boolean proposalAwarded;


		public RequestForProposal(String title, String description, LocalDateTime submissionDeadline,
				List<Requirement> requirements) {

				this.id = EntityIdentifier.generate();
				this.title = title;
				this.description = description;
				this.submissionDeadline = submissionDeadline;
				this.requirements.addAll(requirements);
				this.publishedAt = null;
				this.status = Status.CREATED;
				this.proposalAwarded = false;
		}


		/**
		 * Mark the RFP as published, making it available for vendors to submit proposals.
		 */
		public void markPublished() {

				if (this.status != Status.CREATED) {
						throw new IllegalStateException(String.format(
								"The RFP can only be published if it is in the 'Created' state. " + "Current status: %s", this.status));
				}

				this.status = Status.PUBLISHED;

				this.publishedAt = LocalDateTime.now();
		}

		public boolean isPublished() {
				return this.status == Status.PUBLISHED;
		}

		public boolean isNotPublished() {
				return !this.isPublished();
		}

		/**
		 * Marks the RFP as closed, indicating the process has been completed either after awarding a proposal or by closing
		 * without awarding.
		 */
		public void markClosed(boolean proposalAwarded) {

				if (isNotPublished()) {
						throw new IllegalStateException(
								String.format("The RFP must published first to close it. Current status: %s", this.status));
				}

				this.status = Status.CLOSED;
				this.proposalAwarded = proposalAwarded;
		}

		@Override
		public EntityIdentifier getId() {
				return id;
		}

		public String getTitle() {
				return title;
		}

		public String getDescription() {
				return description;
		}

		public LocalDateTime getSubmissionDeadline() {
				return submissionDeadline;
		}

		public List<Requirement> getRequirements() {
				return requirements;
		}

		public LocalDateTime getCreatedAt() {
				return createdAt;
		}

		public LocalDateTime getLastUpdatedAt() {
				return lastUpdatedAt;
		}

		public LocalDateTime getPublishedAt() {
				return publishedAt;
		}

		public Status getStatus() {
				return status;
		}

		public boolean isProposalAwarded() {
				return proposalAwarded;
		}

		public enum Status {

				/**
				 * The RFP has been created and is in a draft or initial state.
				 */
				CREATED,

				/**
				 * The RFP has been finalized and made public or sent to potential vendors.
				 */
				PUBLISHED,

				/**
				 * The RFP process is completed, either after awarding a proposal or by closing without awarding.
				 */
				CLOSED
		}
}
