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

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Raed Ben Hamouda
 */
public class CreateRfpRequest {

    public String title;
    public String description;
    public List<Requirement> requirements;
    public LocalDateTime submissionDeadline;

    public static class Requirement {

        public String id;
        public String description;
    }
}
