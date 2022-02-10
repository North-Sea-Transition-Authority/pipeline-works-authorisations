package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifierVisitor;

/**
 * Defines the types of pipeline role instance that can exist. Use sparingly to avoid relying on the Tagged class antipattern.
 * Usage of the {@link PipelineIdentifierVisitor} pattern should be preferred when "type" based logic is required.
 */
public enum OrgRoleInstanceType {
  FULL_PIPELINE, SPLIT_PIPELINE;
}
