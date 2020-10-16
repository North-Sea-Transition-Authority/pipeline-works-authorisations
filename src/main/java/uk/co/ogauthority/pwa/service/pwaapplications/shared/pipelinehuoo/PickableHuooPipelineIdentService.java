package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form.DefinePipelineHuooSectionsForm;
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentService;

/**
 * Facilitates the definition of sections along a pipeline and interpretation of user input into pipeline sections.
 */
@Service
public class PickableHuooPipelineIdentService {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;

  private final PipelineDetailIdentService pipelineDetailIdentService;

  @Autowired
  public PickableHuooPipelineIdentService(
      PadPipelineService padPipelineService,
      PadPipelineIdentService padPipelineIdentService,
      PipelineDetailIdentService pipelineDetailIdentService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailIdentService = pipelineDetailIdentService;
  }

  // helper to provide ident views for pipeline from either the current application or consented model if not imported.
  private List<IdentView> getPickablePipelineIdentViews(PwaApplicationDetail pwaApplicationDetail,
                                                        PipelineId pipelineId) {
    var padPipeline = padPipelineService.findByPwaApplicationDetailAndPipelineId(pwaApplicationDetail, pipelineId);
    if (padPipeline.isPresent()) {
      return padPipelineIdentService.getIdentViews(padPipeline.get());
    }

    return pipelineDetailIdentService.getSortedPipelineIdentViewsForPipeline(pipelineId);
  }

  /**
   *  <p>This will return idents from either the application or consented model for a given pipeline ID.
   *  If the application detail does not contain an application version of the pipeline, then the consented model
   *  ident will be returned.</p>
   *
   *  <p>It is not the responsibility of this method to check that the application detail is for the same masterPwa consented pipeline.</p>
   */
  public List<PickableIdentLocationOption> getSortedPickableIdentLocationOptions(PwaApplicationDetail pwaApplicationDetail,
                                                                                 PipelineId pipelineId) {
    return getPickablePipelineIdentViews(pwaApplicationDetail, pipelineId)
        .stream()
        .flatMap(identView -> PickableIdentLocationOption.createIdentLocationOptionsFrom(identView).stream())
        .sorted(Comparator.comparing(PickableIdentLocationOption::getSortKey))
        .collect(Collectors.toList());
  }

  public List<PipelineSection> generatePipelineSectionsFromForm(PwaApplicationDetail pwaApplicationDetail,
                                                                PipelineId pipelineId,
                                                                DefinePipelineHuooSectionsForm form) {

    var sortedIdentLocations = getSortedPickableIdentLocationOptions(pwaApplicationDetail, pipelineId);
    List<PipelineSection> generatedSections = new ArrayList<>();

    var identLocationLookup = sortedIdentLocations.stream()
        .collect(Collectors.toMap(PickableIdentLocationOption::getPickableString,  Function.identity()));

    for (int i = 0; i < form.getPipelineSectionPoints().size(); i++) {
      var sectionNumber = i + 1;
      var nextSectionStartPointOpt = form.getSectionPointFormAtIndex(i + 1);
      var currentSectionStartPoint = form.getPipelineSectionPoints().get(i);

      // assume section is valid and take start point at face value.
      var fromLocationName = identLocationLookup.get(currentSectionStartPoint.getPickedPipelineIdentString()).getLocationName();
      var fromLocationInclusionMode = currentSectionStartPoint.getPointIncludedInSection()
          ? IdentLocationInclusionMode.INCLUSIVE
          : IdentLocationInclusionMode.EXCLUSIVE;

      String toLocationName;
      IdentLocationInclusionMode toLocationInclusionMode;

      if (nextSectionStartPointOpt.isPresent()) {
        var nextSectionStartIdentLocation = identLocationLookup.get(nextSectionStartPointOpt.get().getPickedPipelineIdentString());
        toLocationName = nextSectionStartIdentLocation.getLocationName();

        // if the next section includes the point it starts at, then our "to" location cannot include it
        toLocationInclusionMode = nextSectionStartPointOpt.get().getPointIncludedInSection()
            ?  IdentLocationInclusionMode.EXCLUSIVE
            :  IdentLocationInclusionMode.INCLUSIVE;

      } else {
        // could not find the next section, so get the very last ident location and set "to" to Include it.
        toLocationInclusionMode = IdentLocationInclusionMode.INCLUSIVE;
        toLocationName = sortedIdentLocations.get(sortedIdentLocations.size() - 1).getLocationName();
      }

      generatedSections.add(
          PipelineSection.from(
              pipelineId,
              sectionNumber,
              PipelineIdentPoint.from(fromLocationName, fromLocationInclusionMode),
              PipelineIdentPoint.from(toLocationName, toLocationInclusionMode)

          )
      );
    }

    return generatedSections;
  }


}
