package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;

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

  public void setPipelineSectionPoints(
      List<PipelineSectionPointFormInput> pipelineSectionPoints) {
    this.pipelineSectionPoints = pipelineSectionPoints;
  }
}
