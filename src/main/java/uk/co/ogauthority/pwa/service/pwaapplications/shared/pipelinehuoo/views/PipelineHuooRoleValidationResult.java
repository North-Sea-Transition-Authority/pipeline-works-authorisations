package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.view.PipelineAndIdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;

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

          if (!validationVisitor.isValid) {
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

    return new PipelineHuooRoleValidationResult(unassignedPipelineErrorMessage, unassignedRoleOwnerErrorMessage, invalidSplitsErrorMessage);


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

  private static class PipelineIdentifierSectionValidatorVisitor implements PipelineIdentifierVisitor {

    private final List<PickableIdentLocationOption> sortedPipelineLocationOptions;
    private final List<PipelineSection> sortedPipelineSections;
    private final int maxSectionNumber;
    private boolean isValid = true;

    public PipelineIdentifierSectionValidatorVisitor(List<PickableIdentLocationOption> sortedPipelineLocationOptions,
                                                     List<PipelineIdentifier> groupedPipelineIdentifiers) {
      this.sortedPipelineLocationOptions = sortedPipelineLocationOptions;
      this.sortedPipelineSections = groupedPipelineIdentifiers.stream()
          // how to get rid of this? revisit if time.
          .filter(o -> o instanceof PipelineSection)
          .map(o -> (PipelineSection) o)
          .sorted(Comparator.comparing(PipelineSection::getSectionNumber))
          .collect(Collectors.toList());

      this.maxSectionNumber = this.sortedPipelineSections.stream()
          .max(Comparator.comparing(PipelineSection::getSectionNumber))
          .map(PipelineSection::getSectionNumber)
          .orElse(1);

      this.sortedPipelineSections.forEach(pipelineSection -> pipelineSection.accept(this));
    }

    @Override
    public void visit(PipelineId pipelineId) {
      // not relevant
      return;
    }

    @Override
    public void visit(PipelineSection pipelineSection) {
      var firstLocation = this.sortedPipelineLocationOptions.get(0);

      // if first section and the from point is not the first idents location and inclusive, error found.
      if (pipelineSection.getSectionNumber() == 1
          && !pipelineSection.getFromPoint().equals(
          PipelineIdentPoint.inclusivePoint(firstLocation.getLocationName()))) {
        this.isValid = false;
        return;
      }

      var lastLocation = this.sortedPipelineLocationOptions.get(this.sortedPipelineLocationOptions.size() - 1);

      // if last section does no end at the last ident point and include it, error found.
      if (pipelineSection.getSectionNumber() == maxSectionNumber
          && !pipelineSection.getToPoint().equals(PipelineIdentPoint.inclusivePoint(lastLocation.getLocationName()))) {
        this.isValid = false;
        return;
      }

      // check that both "to" and "from" location can be found in ident location options
      // and that the section's "to" location is on or after the "from";
      var minMatchingIdentLocationByFromLocation = this.sortedPipelineLocationOptions.stream()
          .filter(o -> o.getLocationName().equals(pipelineSection.getFromPoint().getLocationName()))
          .min(Comparator.comparing(PickableIdentLocationOption::getIdentNumber));

      var maxMatchingIdentLocationByToLocation = this.sortedPipelineLocationOptions.stream()
          .filter(o -> o.getLocationName().equals(pipelineSection.getToPoint().getLocationName()))
          .max(Comparator.comparing(PickableIdentLocationOption::getIdentNumber));

      if (minMatchingIdentLocationByFromLocation.isEmpty() || maxMatchingIdentLocationByToLocation.isEmpty()) {
        this.isValid = false;
        // if both locations found, and the "from" location ident is after the "to" ident location, error found
      } else if (
          minMatchingIdentLocationByFromLocation.get().getIdentNumber() > maxMatchingIdentLocationByToLocation.get().getIdentNumber()
      ) {
        this.isValid = false;
        // if both locations found and the "from" location and "to" location are on the same ident,
        // but the "to" location matches the ident's from location and
      } else if (
          minMatchingIdentLocationByFromLocation.get().getIdentNumber() == maxMatchingIdentLocationByToLocation.get().getIdentNumber()
      ) {
        var sectionFromPoint = pipelineSection.getFromPoint();
        var sectionToPint = pipelineSection.getToPoint();
        // if the found location's "From" is the ident's "to" and the found "to" is the idents "from" then error as this cannot make sense
        if (minMatchingIdentLocationByFromLocation.get().getIdentPoint()
            .equals(PickableIdentLocationOption.IdentPoint.TO_LOCATION)
            && maxMatchingIdentLocationByToLocation.get().getIdentPoint()
            .equals(PickableIdentLocationOption.IdentPoint.FROM_LOCATION)) {
          this.isValid = false;
        }
      }
    }
  }
}
