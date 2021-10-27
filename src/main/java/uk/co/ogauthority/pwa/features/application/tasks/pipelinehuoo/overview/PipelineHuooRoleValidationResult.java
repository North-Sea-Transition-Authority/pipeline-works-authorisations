package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.PickableIdentLocationOption;
import uk.co.ogauthority.pwa.model.view.PipelineAndIdentView;

/**
 * Works out and then materialises all possible errors messages a pipeline and org role group can have.
 */
public class PipelineHuooRoleValidationResult {

  private final String unassignedPipelineErrorMessage;

  private final String unassignedRoleOwnerErrorMessage;

  private final String invalidSplitsErrorMessage;

  private final boolean hasErrors;


  PipelineHuooRoleValidationResult(String unassignedPipelineErrorMessage,
                                   String unassignedRoleOwnerErrorMessage,
                                   String invalidSplitErrorMessage) {
    this.unassignedPipelineErrorMessage = unassignedPipelineErrorMessage;
    this.unassignedRoleOwnerErrorMessage = unassignedRoleOwnerErrorMessage;
    this.invalidSplitsErrorMessage = invalidSplitErrorMessage;

    this.hasErrors = ObjectUtils.anyNotNull(
        this.unassignedPipelineErrorMessage,
        this.unassignedRoleOwnerErrorMessage,
        this.invalidSplitsErrorMessage
    );
  }

  public static PipelineHuooRoleValidationResult generateValidationResult(
      PipelineHuooRoleSummaryView pipelineHuooRoleSummaryView,
      Map<PipelineId, PipelineAndIdentView> pipelineAndIdentLocationsMap) {

    var lowercaseHuooRole = pipelineHuooRoleSummaryView.getHuooRole().getDisplayText().toLowerCase();

    var unassignedPipelineErrorMessage = pipelineHuooRoleSummaryView.getUnassignedPipelineIds().isEmpty()
        ? null
        : String.format(
        "At least one %s must be assigned to every pipeline or defined pipeline section",
        lowercaseHuooRole
    );

    var unassignedRoleOwnerErrorMessage = pipelineHuooRoleSummaryView.getUnassignedRoleOwnerOrganisationIds().isEmpty()
        ? null
        : String.format(
        "Every %s must have at least 1 assigned pipeline or defined pipeline section",
        lowercaseHuooRole
    );

    var pipelineIdToIdentLocationOptions = pipelineAndIdentLocationsMap.entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> PickableIdentLocationOption.createSortedPickableIdentLocationOptionList(
                entry.getValue().getSortedIdentViews()
            )
        ));

    var pipelinesWithInvalidSplits = new ArrayList<String>();

    pipelineHuooRoleSummaryView.getAllPipelineIdentifiers()
        .stream()
        .collect(groupingBy(PipelineIdentifier::getPipelineId, Collectors.toList()))
        .forEach((pipelineId, pipelineIdentifiers) -> {
          // do validation per pipeline and its grouped sections.
          var validationVisitor = new PipelineIdentifierSectionValidatorVisitor(
              pipelineIdToIdentLocationOptions.get(pipelineId),
              pipelineIdentifiers
          );

          if (!validationVisitor.isValid()) {
            pipelinesWithInvalidSplits.add(
                pipelineAndIdentLocationsMap.get(pipelineId).getPipelineOverview().getPipelineNumber());
          }

        });

    String invalidSplitsErrorMessage = null;
    pipelinesWithInvalidSplits.sort(String::compareToIgnoreCase);
    if (!pipelinesWithInvalidSplits.isEmpty()) {
      invalidSplitsErrorMessage = String.format(
          "Invalid %s sections detected. Redefine splits for the following pipelines: %s",
          lowercaseHuooRole,
          String.join(", ", pipelinesWithInvalidSplits)
      );
    }

    return new PipelineHuooRoleValidationResult(
        unassignedPipelineErrorMessage,
        unassignedRoleOwnerErrorMessage,
        invalidSplitsErrorMessage);


  }


  static PipelineHuooRoleValidationResult createValidResult() {
    return new PipelineHuooRoleValidationResult(null, null, null);
  }

  public String getUnassignedPipelineErrorMessage() {
    return unassignedPipelineErrorMessage;
  }

  public String getUnassignedRoleOwnerErrorMessage() {
    return unassignedRoleOwnerErrorMessage;
  }

  public String getInvalidSplitsErrorMessage() {
    return invalidSplitsErrorMessage;
  }

  public boolean hasErrors() {
    return hasErrors;
  }

}
