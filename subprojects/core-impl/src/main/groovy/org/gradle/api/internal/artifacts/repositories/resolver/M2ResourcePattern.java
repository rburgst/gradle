/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.repositories.resolver;

import org.apache.ivy.core.IvyPatternHelper;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.metadata.IvyArtifactName;
import org.gradle.api.internal.artifacts.metadata.ModuleVersionArtifactMetaData;

import java.util.Map;

public class M2ResourcePattern extends AbstractResourcePattern {
    public M2ResourcePattern(String pattern) {
        super(pattern);
    }

    @Override
    public String toString() {
        return String.format("M2 pattern '%s'", getPattern());
    }

    public String toPath(ModuleVersionArtifactMetaData artifact) {
        Map<String, String> attributes = toAttributes(artifact);
        String pattern = maybeSubstituteTimestamp(artifact, getPattern());
        return substituteTokens(pattern, attributes);
    }

    private String maybeSubstituteTimestamp(ModuleVersionArtifactMetaData artifact, String pattern) {
        if (artifact.getComponentId() instanceof MavenUniqueSnapshotComponentIdentifier) {
            String timestampedVersion = ((MavenUniqueSnapshotComponentIdentifier) artifact.getComponentId()).getTimestampedVersion();
            pattern = pattern.replaceFirst("\\-\\[revision\\]", "-" + timestampedVersion);
        }
        return pattern;
    }

    public String toVersionListPattern(ModuleIdentifier module, IvyArtifactName artifact) {
        Map<String, String> attributes = toAttributes(module, artifact);
        return substituteTokens(pattern, attributes);
    }

    public String toModulePath(ModuleIdentifier module) {
        String pattern = getPattern();
        if (!pattern.endsWith(MavenPattern.M2_PATTERN)) {
            throw new UnsupportedOperationException("Cannot locate module for non-maven layout.");
        }
        String metaDataPattern = pattern.substring(0, pattern.length() - MavenPattern.M2_PER_MODULE_PATTERN.length() - 1);
        return substituteTokens(metaDataPattern, toAttributes(module));
    }

    public String toModuleVersionPath(ModuleComponentIdentifier componentIdentifier) {
        String pattern = getPattern();
        if (!pattern.endsWith(MavenPattern.M2_PATTERN)) {
            throw new UnsupportedOperationException("Cannot locate module version for non-maven layout.");
        }
        String metaDataPattern = pattern.substring(0, pattern.length() - MavenPattern.M2_PER_MODULE_VERSION_PATTERN.length() - 1);
        return substituteTokens(metaDataPattern, toAttributes(componentIdentifier));
    }

    protected String substituteTokens(String pattern, Map<String, String> attributes) {
        String org = attributes.get(IvyPatternHelper.ORGANISATION_KEY);
        if (org != null) {
            attributes.put(IvyPatternHelper.ORGANISATION_KEY, org.replace(".", "/"));
        }
        return super.substituteTokens(pattern, attributes);
    }
}
