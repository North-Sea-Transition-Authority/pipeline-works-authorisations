package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;


import java.util.Set;
import java.util.function.Function;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

public enum ProjectInformationQuestion {

  PROJECT_NAME((applicationType) -> false),
  PROJECT_OVERVIEW((applicationType) -> false),
  METHOD_OF_PIPELINE_DEPLOYMENT((applicationType) ->
      Set.of(PwaApplicationType.OPTIONS_VARIATION, PwaApplicationType.CAT_2_VARIATION).contains(applicationType)),
  PROPOSED_START_DATE((applicationType) -> false),
  MOBILISATION_DATE((applicationType) -> false),
  EARLIEST_COMPLETION_DATE((applicationType) -> false),
  LATEST_COMPLETION_DATE((applicationType) -> false),
  LICENCE_TRANSFER_PLANNED((applicationType) -> false),
  LICENCE_TRANSFER_DATE((applicationType) -> false),
  COMMERCIAL_AGREEMENT_DATE((applicationType) -> false),
  USING_CAMPAIGN_APPROACH((applicationType) -> false),
  PERMANENT_DEPOSITS_BEING_MADE((applicationType) -> false),
  TEMPORARY_DEPOSITS_BEING_MADE((applicationType) -> false),
  FIELD_DEVELOPMENT_PLAN((applicationType) -> false),
  PROJECT_LAYOUT_DIAGRAM((applicationType) -> false);

  private final Function<PwaApplicationType, Boolean> isOptional;

  ProjectInformationQuestion(Function<PwaApplicationType, Boolean> isOptional) {
    this.isOptional = isOptional;
  }

  public boolean isOptionalForType(PwaApplicationType type) {
    return this.isOptional.apply(type);
  }

}