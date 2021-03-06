/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.workbench.cm.backend.server;

import java.util.function.Function;

import org.jbpm.workbench.cm.model.CaseRoleAssignmentSummary;
import org.kie.server.api.model.cases.CaseRoleAssignment;

public class RoleAssignmentsMapper implements Function<CaseRoleAssignment, CaseRoleAssignmentSummary> {

    @Override
    public CaseRoleAssignmentSummary apply(CaseRoleAssignment ra) {
        if (ra == null) {
            return null;
        }

        return CaseRoleAssignmentSummary.builder()
                .name(ra.getName())
                .groups(ra.getGroups())
                .users(ra.getUsers())
                .build();
    }
}
