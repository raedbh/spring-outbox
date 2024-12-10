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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.common.EntityIdentifier;

/**
 * @author Raed Ben Hamouda
 */
@Service
public class RfpManagement {

    private final RfpRepository rfpRepository;
    private final RequirementLabelRepository labelRepository;


    RfpManagement(RfpRepository repository, RequirementLabelRepository labelRepository) {
        this.rfpRepository = repository;
        this.labelRepository = labelRepository;
    }


    public RequestForProposal find(EntityIdentifier rfpId) {
        return rfpRepository.findById(rfpId)
          .orElseThrow(() -> new IllegalArgumentException("No RFP found with ID: " + rfpId));
    }

    @Transactional
    public void save(RequestForProposal rfp) {
        rfpRepository.save(rfp);
    }

    @Transactional
    public void publish(EntityIdentifier rfpId) {
        rfpRepository.findById(rfpId).ifPresent(rfp -> {
            rfp.markPublished();
            rfpRepository.save(rfp);
        });
    }

    @Transactional
    public void close(EntityIdentifier rfpId) {
        RequestForProposal rfp = find(rfpId);
        rfp.markClosed(true);
    }

    public Map<String, EntityIdentifier> labelIdsFrom(String... labels) {
        List<RequirementLabel> labelList = labelRepository.findByLabelInIgnoreCase(List.of(labels));
        if (labelList.size() != labels.length) {
            throw new IllegalArgumentException("One or more labels are not found");
        }

        return labelList.stream().collect(Collectors.toMap(RequirementLabel::getLabel, RequirementLabel::getId));
    }

    public EntityIdentifier labelIdFrom(String label) {
        return labelRepository.findByLabelIgnoreCase(label)
          .orElseThrow(() -> new IllegalArgumentException("Label Not Found: " + label)).getId();
    }
}
