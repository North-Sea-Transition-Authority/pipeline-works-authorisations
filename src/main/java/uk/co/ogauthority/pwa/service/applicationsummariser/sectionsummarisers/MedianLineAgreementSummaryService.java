package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;

/**
 * Construct summary of MedianLine Agreement for a given application.
 */
@Service
public class MedianLineAgreementSummaryService implements ApplicationSectionSummariser {

  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final TaskListService taskListService;

  @Autowired
  public MedianLineAgreementSummaryService(
      PadMedianLineAgreementService padMedianLineAgreementService,
      TaskListService taskListService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.taskListService = taskListService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.CROSSING_AGREEMENTS);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail)
        && padMedianLineAgreementService.canShowInTaskList(pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var sectionDisplayText = CrossingAgreementTask.MEDIAN_LINE.getDisplayText();
    var view = padMedianLineAgreementService.getMedianLineCrossingView(pwaApplicationDetail);
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("medianLineAgreementView", view);
    summaryModel.put("medianLineUrlFactory", new MedianLineCrossingUrlFactory(pwaApplicationDetail));
    summaryModel.put("medianLineFiles", view.getSortedFileViews());

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#medianLineAgreementDetails"
        )),
        summaryModel
    );
  }


}