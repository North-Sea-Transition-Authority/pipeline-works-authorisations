package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

/**
 * <p>Describes the highest level category of pipelines supported by the application.</p>
 *
 * <p><b>If this list changes you MUST update the following the database API view: api_vw_pwa_pipeline_details.</b></p>
 *
 */
public enum PipelineType implements DiffableAsString {

  // We need this type to support pipelines migrated from the legacy system where we cannot know the type.
  UNKNOWN("Unknown pipeline type", -1, PipelineCoreType.SINGLE_CORE),
  PRODUCTION_FLOWLINE("Production Flowline", 1, PipelineCoreType.SINGLE_CORE),
  PRODUCTION_JUMPER("Production Jumper", 2, PipelineCoreType.SINGLE_CORE),
  GAS_LIFT_PIPELINE("Gas Lift Pipeline", 3, PipelineCoreType.SINGLE_CORE),
  GAS_LIFT_JUMPER("Gas Lift Jumper", 4, PipelineCoreType.SINGLE_CORE),
  WATER_INJECTION_PIPELINE("Water Injection Pipeline", 5, PipelineCoreType.SINGLE_CORE),
  WATER_INJECTION_JUMPER("Water Injection Jumper", 6, PipelineCoreType.SINGLE_CORE),
  METHANOL_PIPELINE("Methanol Pipeline", 7, PipelineCoreType.SINGLE_CORE),
  SERVICES_UMBILICAL("Services Umbilical", 8, PipelineCoreType.MULTI_CORE),
  HYDRAULIC_JUMPER("Hydraulic Jumper", 9, PipelineCoreType.MULTI_CORE),
  HYDRAULIC_JUMPER_SINGLE_CORE("Hydraulic Jumper (single-core)", 10, PipelineCoreType.SINGLE_CORE),
  HYDRAULIC_JUMPER_MULTI_CORE("Hydraulic Jumper (multi-core)", 11, PipelineCoreType.MULTI_CORE),
  CHEMICAL_JUMPER("Chemical Jumper", 12, PipelineCoreType.SINGLE_CORE),
  CONTROL_JUMPER("Control Jumper", 13, PipelineCoreType.SINGLE_CORE),
  CONTROL_JUMPER_SINGLE_CORE("Control Jumper (single-core)", 14, PipelineCoreType.SINGLE_CORE),
  CONTROL_JUMPER_MULTI_CORE("Control Jumper (multi-core)", 15, PipelineCoreType.MULTI_CORE),
  UMBILICAL_JUMPER("Umbilical Jumper", 16, PipelineCoreType.MULTI_CORE),
  CABLE("Cable", 17, PipelineCoreType.SINGLE_CORE);

  private final String displayName;
  private final int displayOrder;
  private final PipelineCoreType coreType;

  PipelineType(String displayName, int displayOrder, PipelineCoreType coreType) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.coreType = coreType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public PipelineCoreType getCoreType() {
    return coreType;
  }

  public static Stream<PipelineType> stream() {
    return Stream.of(PipelineType.values());
  }

  public static Stream<PipelineType> streamDisplayValues() {
    return Stream.of(PipelineType.values())
        .filter(pipelineType -> pipelineType.getDisplayOrder() >= 0);
  }


  @Override
  public String getDiffableString() {
    return this.getDisplayName();
  }
}
