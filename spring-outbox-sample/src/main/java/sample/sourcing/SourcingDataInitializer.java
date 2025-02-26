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
package sample.sourcing;

import java.util.Arrays;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import sample.sourcing.rfp.RequirementLabel;
import sample.sourcing.rfp.RequirementLabelRepository;

/**
 * @author Raed Ben Hamouda
 */
@Component
class SourcingDataInitializer implements ApplicationRunner {

    private final RequirementLabelRepository labels;

    SourcingDataInitializer(RequirementLabelRepository labels) {
        this.labels = labels;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (noRequirementLabelsExist()) {
            labels.saveAll(Arrays.asList(
              new RequirementLabel("Technical Skills"), new RequirementLabel("Project Management"),
              new RequirementLabel("Deliverables"), new RequirementLabel("Time Frame"),
              new RequirementLabel("Experience"), new RequirementLabel("Communication")));
        }
    }

    boolean noRequirementLabelsExist() {
        return labels.count() == 0;
    }
}
