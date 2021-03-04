package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;

/**
 * Package private helper class to perform pipeline section validation for pipeline huoo roles.
 * This class is not multi purpose and should not be reused.
 */
class PipelineIdentifierSectionValidatorVisitor implements PipelineIdentifierVisitor {

  private final List<PickableIdentLocationOption> sortedPipelineLocationOptions;
  private final List<PipelineSection> sortedPipelineSections;
  private final int maxSectionNumber;
  private boolean isValid = true;

  PipelineIdentifierSectionValidatorVisitor(List<PickableIdentLocationOption> sortedPipelineLocationOptions,
                                            List<PipelineIdentifier> groupedPipelineIdentifiers) {
    this.sortedPipelineLocationOptions = sortedPipelineLocationOptions;
    this.sortedPipelineSections = groupedPipelineIdentifiers.stream()
        .filter(PipelineSection.class::isInstance)
        .map(o -> (PipelineSection) o)
        .sorted(Comparator.comparing(PipelineSection::getSectionNumber))
        .collect(Collectors.toList());

    this.maxSectionNumber = this.sortedPipelineSections.stream()
        .max(Comparator.comparing(PipelineSection::getSectionNumber))
        .map(PipelineSection::getSectionNumber)
        .orElse(1);

    this.sortedPipelineSections.forEach(pipelineSection -> pipelineSection.accept(this));
  }

  public boolean isValid() {
    return isValid;
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
