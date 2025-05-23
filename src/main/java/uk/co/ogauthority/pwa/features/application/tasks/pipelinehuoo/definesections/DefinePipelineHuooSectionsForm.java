package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Form to define entire path of HUOO split for pipeline.
 */
public class DefinePipelineHuooSectionsForm {

  private List<PipelineSectionPointFormInput> pipelineSectionPoints;

  public void resetSectionPoints(int numberOfSections,
                                 PickableIdentLocationOption firstIdentLocation) {
    if (numberOfSections <= 0) {
      throw new IllegalArgumentException("Expected number of sections to be 1 or more. Actual:" + numberOfSections);
    }

    var sectionPoints = new ArrayList<PipelineSectionPointFormInput>();
    sectionPoints.add(
        PipelineSectionPointFormInput.createFirstSectionPoint(firstIdentLocation)
    );

    IntStream.range(1, numberOfSections)
        .forEach(i -> sectionPoints.add(new PipelineSectionPointFormInput()));
    this.pipelineSectionPoints = sectionPoints;

  }

  public List<PipelineSectionPointFormInput> getPipelineSectionPoints() {
    return pipelineSectionPoints;
  }

  public Optional<PipelineSectionPointFormInput> getSectionPointFormAtIndex(int index) {
    try {
      return Optional.of(getPipelineSectionPoints().get(index));
    } catch (IndexOutOfBoundsException e) {
      return Optional.empty();
    }
  }

  public void setPipelineSectionPoints(
      List<PipelineSectionPointFormInput> pipelineSectionPoints) {
    this.pipelineSectionPoints = pipelineSectionPoints;
  }
}
